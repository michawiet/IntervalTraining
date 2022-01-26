package eu.mikko.intervaltraining.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.*
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.location.Location
import android.os.Looper
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.other.Constants.ACTION_INTERVAL_DATA
import eu.mikko.intervaltraining.other.Constants.ACTION_PAUSE_SERVICE
import eu.mikko.intervaltraining.other.Constants.ACTION_START_SERVICE
import eu.mikko.intervaltraining.other.Constants.ACTION_STOP_SERVICE
import eu.mikko.intervaltraining.other.Constants.EXTRAS_INTERVAL_DATA
import eu.mikko.intervaltraining.other.Constants.FASTEST_LOCATION_INTERVAL
import eu.mikko.intervaltraining.other.Constants.LOCATION_UPDATE_INTERVAL
import eu.mikko.intervaltraining.other.Constants.TIMER_UPDATE_INTERVAL
import eu.mikko.intervaltraining.other.Constants.TRACKING_NOTIFICATION_CHANNEL_ID
import eu.mikko.intervaltraining.other.Constants.TRACKING_NOTIFICATION_CHANNEL_NAME
import eu.mikko.intervaltraining.other.Constants.TRACKING_NOTIFICATION_ID
import eu.mikko.intervaltraining.other.ParcelableInterval
import eu.mikko.intervaltraining.other.TrackingUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias IntervalChunk = MutableList<LatLng>
typealias IntervalPathPoints = MutableList<IntervalChunk>
typealias Intervals = MutableList<IntervalPathPoints>

@AndroidEntryPoint
class TrackingService : LifecycleService() {
    //Easy access to calculating distance and getting speed
    private var lastLocation: Location? = null

    private var isFirstRun = true
    private var serviceKilled = false

    private lateinit var runData: TrackingUtility.RunData

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder
    private lateinit var curNotificationBuilder: NotificationCompat.Builder

    private val timeRunInSeconds = MutableLiveData<Long>()

    private lateinit var tts: TextToSpeech

    companion object {
        // tracking
        val isTracking = MutableLiveData<Boolean>()
        val pathPointsOfIntervals = MutableLiveData<Intervals>()
        //tracking stats
        val currentSpeedMetersPerSecond = MutableLiveData<Float>()
        val distanceInMeters = MutableLiveData<Float>()

        // Toggled between walk and run by the intervals
        val isRunningInterval = MutableLiveData<Boolean>()

        //activity timer
        val timeRunInMillis = MutableLiveData<Long>()
        //interval timer, counting down
        val intervalTimer = MutableLiveData<Long>()
        val intervalProgress = MutableLiveData<Float>()
        val activityProgress = MutableLiveData<Float>()
        val maxTimeInInterval = MutableLiveData<Long>()

        val isActivityOver = MutableLiveData<Boolean>()

        val currentLocation = MutableLiveData<LatLng>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPointsOfIntervals.postValue(mutableListOf())

        currentSpeedMetersPerSecond.postValue(0f)
        distanceInMeters.postValue(0f)
        isRunningInterval.postValue(false)

        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
        intervalProgress.postValue(0f)
        activityProgress.postValue(0f)

        isActivityOver.postValue(false)

        currentLocation.postValue(LatLng(0.0, 0.0))
    }

    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this) {
            updateLocationTracking(it)
            updateNotificationTrackingState()
        }

        tts = TextToSpeech(applicationContext) { status ->
            if(status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0])
                if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                    Timber.d("Language not available")
                }
            } else {
                Timber.d("TTS initialization failed")
            }
        }
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        postInitialValues()
        pauseService()
        //kill tts
        tts.stop()
        tts.shutdown()
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
                        if(tts.speak(getString(R.string.activity_type_walk), TextToSpeech.QUEUE_FLUSH, null, null) == TextToSpeech.ERROR) {
                            Timber.d("Voice synth error...")
                        }
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
                ACTION_INTERVAL_DATA -> {
                    if(it.hasExtra(EXTRAS_INTERVAL_DATA)) {
                        it.getParcelableExtra<ParcelableInterval>(EXTRAS_INTERVAL_DATA)
                            .also { it -> if (it != null) { setRunData(it) } }
                    }
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun setRunData(it: ParcelableInterval) {
        runData = TrackingUtility.RunData(it)
        currentInterval = 0
        currentIntervalData = runData.intervals[currentInterval]
        maxTimeInInterval.postValue(runData.intervals[currentInterval].lengthMillis)
        intervalTimer.postValue(runData.intervals[currentInterval].lengthMillis)
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L
    private var currentInterval = 0
    private lateinit var currentIntervalData: TrackingUtility.RunData.IntervalData

    private fun startTimer() {
        if(isFirstRun)
            addEmptyInterval()
        else
            addEmptyIntervalChunk()

        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            var timeLeftInInterval: Long
            while(isTracking.value!!) {
                // Time diff of now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted
                // post the new lapTime
                timeRunInMillis.postValue(timeRun + lapTime)

                activityProgress.postValue((timeRun + lapTime).div(runData.totalWorkoutTime.toFloat()) * 100f)

                if(timeRun + lapTime >= runData.totalWorkoutTime) {
                    isTracking.postValue(false)
                    isActivityOver.postValue(true)
                }

                // update interval timer
                timeLeftInInterval = (currentIntervalData.startMillis + currentIntervalData.lengthMillis) - (timeRun + lapTime)
                intervalTimer.postValue(if(timeLeftInInterval < 0 ) 0 else timeLeftInInterval)
                intervalProgress.postValue(100f - (timeLeftInInterval.div(currentIntervalData.lengthMillis.toFloat()) * 100f))
                if(timeLeftInInterval <= 0L) {
                    nextInterval()
                }

                if(timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun nextInterval() {
        currentInterval++
        if(runData.intervals.size > currentInterval) {
            currentIntervalData = runData.intervals[currentInterval]
            maxTimeInInterval.postValue(currentIntervalData.lengthMillis)
            isRunningInterval.postValue(currentIntervalData.isRunningInterval)
            addEmptyInterval()
            if(currentIntervalData.isRunningInterval) {
                //trigger tts with "Start running!" message
                tts.speak(getString(R.string.activity_type_run), TextToSpeech.QUEUE_FLUSH, null, null)
            }
            else {
                tts.speak(getString(R.string.activity_type_walk), TextToSpeech.QUEUE_FLUSH, null, null)
                //trigger tts with "Start running!" message
            }
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun updateNotificationTrackingState() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(!serviceKilled && isTimerEnabled) {
            notificationManager.notify(TRACKING_NOTIFICATION_ID, baseNotificationBuilder.build())
        } else {
            notificationManager.cancel(TRACKING_NOTIFICATION_ID)
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if(isTracking || !serviceKilled) {
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

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            if(isTracking.value!!) {
                p0.locations.let { locations ->
                    for(location in locations) {
                        addPathPoint(location)
                    }
                }
            } else {
                p0.locations.let {
                    for(location in it) {
                        currentLocation.postValue(LatLng(location.latitude, location.longitude))
                    }
                }
            }
        }
    }

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
                last().last().add(LatLng(location.latitude, location.longitude))

                // Make the changes by main thread
                pathPointsOfIntervals.postValue(this)
            }
        }
    }

    //call after interval timer runs out
    private fun addEmptyIntervalChunk() {
        // lists.intervals
        pathPointsOfIntervals.value?.last()?.add(mutableListOf())
        pathPointsOfIntervals.postValue(pathPointsOfIntervals.value)
    }

    private fun addEmptyInterval() = pathPointsOfIntervals.value?.apply {
        add(mutableListOf(mutableListOf()))
        pathPointsOfIntervals.postValue(this)
    } ?: pathPointsOfIntervals.postValue(mutableListOf(mutableListOf(mutableListOf())))

    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(notificationManager)

        startForeground(TRACKING_NOTIFICATION_ID, baseNotificationBuilder.build())

        isRunningInterval.observe(this) {
            if (!serviceKilled && isTimerEnabled) {
                val notification = curNotificationBuilder
                    .setContentText(if (it) getString(R.string.activity_type_run) else getString(R.string.activity_type_walk))
                notificationManager.notify(TRACKING_NOTIFICATION_ID, notification.build())
            }
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            TRACKING_NOTIFICATION_CHANNEL_ID,
            TRACKING_NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }
}