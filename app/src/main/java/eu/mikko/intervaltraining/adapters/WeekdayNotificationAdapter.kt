package eu.mikko.intervaltraining.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.data.WeekdayRecyclerViewItem
import kotlinx.android.synthetic.main.weekday_notification_item.view.*

class WeekdayNotificationAdapter(private val weekdayRecyclerViewItemList: List<WeekdayRecyclerViewItem>) : RecyclerView.Adapter<WeekdayNotificationAdapter.WeekdayNotificationViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WeekdayNotificationViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.weekday_notification_item, parent, false)

        return WeekdayNotificationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WeekdayNotificationViewHolder, position: Int) {
        val currentItem = weekdayRecyclerViewItemList[position]

        holder.imageView.setImageResource(currentItem.imageResource)
        holder.notificationTimeTextView.text = currentItem.notificationTime
        holder.weekdayNameTextView.text = currentItem.weekdayName
        holder.notificationSwitch.isChecked = currentItem.isNotificationEnabled
    }

    override fun getItemCount() = weekdayRecyclerViewItemList.size

    class WeekdayNotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.weekday_alarm_icon
        val weekdayNameTextView: TextView = itemView.weekday_name_text_view
        val notificationTimeTextView: TextView = itemView.notification_time
        val notificationSwitch: SwitchMaterial = itemView.weekday_notification_switch
    }
}