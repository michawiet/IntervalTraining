package eu.mikko.intervaltraining

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.notifications.ProgressReceiver
import eu.mikko.intervaltraining.other.Constants.ACTION_SHOW_RUN_FRAGMENT
import eu.mikko.intervaltraining.other.Constants.PROGRESS_NOTIFICATION_DAY
import eu.mikko.intervaltraining.other.Constants.PROGRESS_NOTIFICATION_HOUR
import eu.mikko.intervaltraining.other.Constants.PROGRESS_NOTIFICATION_ID
import eu.mikko.intervaltraining.other.Constants.PROGRESS_NOTIFICATION_MINUTE
import eu.mikko.intervaltraining.other.CalendarUtility
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigateToRunFragmentIfNeeded(intent)

        bottom_navigation.setupWithNavController(navHostFragment.findNavController())
        bottom_navigation.setOnItemReselectedListener { /* Prevent from reloading */ }

        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when(destination.id) {
                    R.id.progressFragment, R.id.runListFragment, R.id.runStartFragment, R.id.notificationsFragment ->
                        bottom_navigation.visibility = View.VISIBLE
                    else -> bottom_navigation.visibility = View.GONE
                }
            }

        setRepeatableProgressNotification()
    }

    private fun setRepeatableProgressNotification() {
        val c = CalendarUtility.generateCalendar(PROGRESS_NOTIFICATION_HOUR, PROGRESS_NOTIFICATION_MINUTE, PROGRESS_NOTIFICATION_DAY)
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ProgressReceiver::class.java)
        intent.action = Intent.ACTION_DEFAULT
        val pendingIntent = PendingIntent.getBroadcast(this, PROGRESS_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.timeInMillis, AlarmManager.INTERVAL_DAY * 7, pendingIntent)
        Timber.d("Progress notification set!")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToRunFragmentIfNeeded(intent)
    }

    private fun navigateToRunFragmentIfNeeded(intent: Intent?) {
        if(intent?.action == ACTION_SHOW_RUN_FRAGMENT) {
            navHostFragment.findNavController().navigate(R.id.action_global_run_fragment)
        }
    }
}