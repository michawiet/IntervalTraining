package eu.mikko.intervaltraining

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.other.Constants.ACTION_SHOW_RUN_FRAGMENT
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigateToRunFragmentIfNeeded(intent)

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