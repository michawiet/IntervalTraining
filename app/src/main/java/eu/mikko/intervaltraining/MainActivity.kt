package eu.mikko.intervaltraining

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import eu.mikko.intervaltraining.fragments.HomeFragment
import eu.mikko.intervaltraining.fragments.NotificationsFragment
import eu.mikko.intervaltraining.fragments.ProgressFragment
import eu.mikko.intervaltraining.fragments.TrainingFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_notifications.*
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val homeFragment = HomeFragment()
        val progressFragment = ProgressFragment()
        val notificationsFragment = NotificationsFragment()
        val trainingFragment = TrainingFragment()

        makeCurrentFragment(homeFragment)

        bottom_navigation.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home_nav -> makeCurrentFragment(homeFragment)
                R.id.progress_nav -> makeCurrentFragment(progressFragment)
                R.id.notifications_nav -> makeCurrentFragment(notificationsFragment)
                R.id.train_nav -> makeCurrentFragment(trainingFragment)
            }
            true
        }
    }

    private fun makeCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
}