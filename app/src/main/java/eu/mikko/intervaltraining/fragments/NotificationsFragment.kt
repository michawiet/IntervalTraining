package eu.mikko.intervaltraining.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.data.DayOfWeekRecyclerViewItem
import eu.mikko.intervaltraining.adapters.TrainingNotificationListAdapter
import kotlinx.android.synthetic.main.fragment_notifications.*
import java.time.DayOfWeek
import java.time.format.TextStyle
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 * Use the [NotificationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NotificationsFragment : Fragment() {

    private fun getWeekdayNotificationList(): List<DayOfWeekRecyclerViewItem> {
        val list = ArrayList<DayOfWeekRecyclerViewItem>()
        // TODO("read data from local storage/sqlite")

        for(day in DayOfWeek.values()) {
            val item = DayOfWeekRecyclerViewItem(day.getDisplayName(TextStyle.FULL,
                ConfigurationCompat.getLocales(resources.configuration)[0]),
                "12:00",
                false,
                day
            )
            list += item
        }

        return list
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val weekdayNotificationList = getWeekdayNotificationList()
        //val recycleView = findViewById<RecyclerView>(R.id.recycler_view)
        recycler_view.adapter = TrainingNotificationListAdapter()
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.setHasFixedSize(true)
    }
}