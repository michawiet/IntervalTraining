package eu.mikko.intervaltraining

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import eu.mikko.intervaltraining.adapters.ViewPagerAdapter
import eu.mikko.intervaltraining.fragments.HomeFragment
import eu.mikko.intervaltraining.fragments.NotificationsFragment
import eu.mikko.intervaltraining.fragments.ProgressFragment
import eu.mikko.intervaltraining.fragments.RunFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewPager2: ViewPager2 = findViewById(R.id.viewpager2)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottom_navigation.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home_nav -> viewPager2.setCurrentItem(0, false)
                R.id.progress_nav -> viewPager2.setCurrentItem(1, false)
                R.id.notifications_nav -> viewPager2.setCurrentItem(2, false)
                R.id.train_nav -> viewPager2.setCurrentItem(3, false)
            }
            true
        }

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                when (position) {
                    0 -> bottomNavigationView.menu.findItem(R.id.home_nav).isChecked = true
                    1 -> bottomNavigationView.menu.findItem(R.id.progress_nav).isChecked = true
                    2 -> bottomNavigationView.menu.findItem(R.id.notifications_nav).isChecked = true
                    3 -> bottomNavigationView.menu.findItem(R.id.train_nav).isChecked = true
                }
            }
        })

        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle)

        val homeFragment = HomeFragment()
        val progressFragment = ProgressFragment()
        val notificationsFragment = NotificationsFragment()
        val trainingFragment = RunFragment()

        viewPagerAdapter.addFragment(homeFragment)
        viewPagerAdapter.addFragment(progressFragment)
        viewPagerAdapter.addFragment(notificationsFragment)
        viewPagerAdapter.addFragment(trainingFragment)

        viewPager2.adapter = viewPagerAdapter
        viewPager2.isUserInputEnabled = false
    }
}