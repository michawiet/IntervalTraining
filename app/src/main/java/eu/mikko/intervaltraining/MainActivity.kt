package eu.mikko.intervaltraining

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.adapters.ViewPagerAdapter
import eu.mikko.intervaltraining.fragments.HomeFragment
import eu.mikko.intervaltraining.fragments.NotificationsFragment
import eu.mikko.intervaltraining.fragments.ProgressFragment
import eu.mikko.intervaltraining.fragments.RunFragment
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottom_navigation.setupWithNavController(navHostFragment.findNavController())

        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when(destination.id) {
                    R.id.homeFragment, R.id.progressFragment, R.id.runStartFragment, R.id.notificationsFragment ->
                        bottom_navigation.visibility = View.VISIBLE
                    else -> bottom_navigation.visibility = View.GONE
                }
            }
    }
}