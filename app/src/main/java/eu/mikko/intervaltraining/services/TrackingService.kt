package eu.mikko.intervaltraining.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.other.Constants.ACTION_PAUSE_SERVICE
import eu.mikko.intervaltraining.other.Constants.ACTION_START_SERVICE
import eu.mikko.intervaltraining.other.Constants.ACTION_STOP_SERVICE
import eu.mikko.intervaltraining.other.Constants.FASTEST_LOCATION_INTERVAL
import eu.mikko.intervaltraining.other.Constants.LOCATION_UPDATE_INTERVAL
import eu.mikko.intervaltraining.other.Constants.TIMER_UPDATE_INTERVAL
import eu.mikko.intervaltraining.other.Constants.TRACKING_NOTIFICATION_CHANNEL_ID
import eu.mikko.intervaltraining.other.Constants.TRACKING_NOTIFICATION_CHANNEL_NAME
import eu.mikko.intervaltraining.other.Constants.TRACKING_NOTIFICATION_ID
import eu.mikko.intervaltraining.other.TrackingUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

//typealias IntervalChunk = MutableList<Location>
typealias Interval = MutableList<LatLng>
typealias Intervals = MutableList<Interval>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    //Easy access to calculating distance and getting speed
    var lastLocation: Location? = null

    var isFirstRun = true
    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInSeconds = MutableLiveData<Long>()

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var curNotificationBuilder: NotificationCompat.Builder

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPointsOfIntervals = MutableLiveData<Intervals>()
        val currentSpeedMetersPerSecond = MutableLiveData<Float>()
        val distanceInMeters = MutableLiveData<Float>()

        // Toggled between walk and run by the intervals
        val isRunningInterval = MutableLiveData<Boolean>()

        //activity timer
        val timeRunInMillis = MutableLiveData<Long>()
        //interval timer, counting down
        //val intervalTimer = MutableLiveData<Long>(0L)
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPointsOfIntervals.postValue(mutableListOf())
        currentSpeedMetersPerSecond.postValue(0f)
        distanceInMeters.postValue(0f)
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
        isRunningInterval.postValue(false)
    }

    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {
                ACTION_START_SERVICE -> {
                    if(isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service...")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    killService()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    private fun startTimer() {
        addEmptyInterval()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            while(isTracking.value!!) {
                // Time diff of now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted
                // post the new lapTime
                timeRunInMillis.postValue(timeRun + lapTime)
                if(timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if(isTracking) "Pause" else "Resume"
        val pendingIntent = if(isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 8, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            PendingIntent.getService(this, 8, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if(!serviceKilled) {
            curNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_round_pause_24, notificationActionText, pendingIntent)
            notificationManager.notify(TRACKING_NOTIFICATION_ID, curNotificationBuilder.build())
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if(isTracking) {
            if(TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest.create().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            if(isTracking.value!!) {
                p0?.locations?.let { locations ->
                    for(location in locations) {
                        addPathPoint(location)
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }


    // MutableLiveData<List<Location>>
    //Not identical
    private fun addPathPoint(location: Location?) {
        location?.let {
            currentSpeedMetersPerSecond.value = location.speed
            //Get List of Intervals
            pathPointsOfIntervals.value?.apply {
                if(lastLocation != null) {
                    distanceInMeters.postValue(distanceInMeters.value?.plus(lastLocation!!.distanceTo(location)))
                }
                lastLocation = location
                // Most recent interval, add current location
                last().add(LatLng(location.latitude, location.longitude))

                // Make the changes by main thread
                pathPointsOfIntervals.postValue(this)
            }
        }
    }
    //call after interval timer runs out
    private fun addEmptyInterval() = pathPointsOfIntervals.value?.apply {
        add(mutableListOf())
        pathPointsOfIntervals.postValue(this)
    } ?: pathPointsOfIntervals.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService() {
        // grab the interval data of the current step
        startTimer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(notificationManager)

        startForeground(TRACKING_NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this, {
            if(!serviceKilled) {
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000))
                notificationManager.notify(TRACKING_NOTIFICATION_ID, notification.build())
            }
        })
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            TRACKING_NOTIFICATION_CHANNEL_ID,
            TRACKING_NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}