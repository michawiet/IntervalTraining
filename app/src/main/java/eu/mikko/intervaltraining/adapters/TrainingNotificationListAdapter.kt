package eu.mikko.intervaltraining.adapters

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.ConfigurationCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.data.DayOfWeekRecyclerViewItem
import eu.mikko.intervaltraining.entities.TrainingNotificationEntity
import kotlinx.android.synthetic.main.training_notification_item.view.*
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

class TrainingNotificationListAdapter : ListAdapter<TrainingNotificationEntity, TrainingNotificationListAdapter.TrainingNotificationViewHolder>(TrainingNotificationsComparator()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TrainingNotificationViewHolder {
        return TrainingNotificationViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TrainingNotificationViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class TrainingNotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val weekdayNameTextView: TextView = itemView.training_name_text_view
        private val notificationTimeTextView: TextView = itemView.training_notification_time
        private val notificationSwitch: SwitchMaterial = itemView.training_notification_switch

        fun bind(data: TrainingNotificationEntity) {
            weekdayNameTextView.text = DayOfWeek.valueOf(data.dayOfWeek).getDisplayName(TextStyle.FULL, ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0])
            notificationTimeTextView.text = data.time
            notificationSwitch.isChecked = data.isEnabled
        }

        companion object {
            fun create(parent: ViewGroup): TrainingNotificationViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.training_notification_item, parent, false)
                return TrainingNotificationViewHolder(view)
            }
        }
    }
}

class TrainingNotificationsComparator : DiffUtil.ItemCallback<TrainingNotificationEntity>() {
    override fun areItemsTheSame(
        oldItem: TrainingNotificationEntity,
        newItem: TrainingNotificationEntity
    ): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(
        oldItem: TrainingNotificationEntity,
        newItem: TrainingNotificationEntity
    ): Boolean {
        return (oldItem.isEnabled == newItem.isEnabled
                && oldItem.dayOfWeek == newItem.dayOfWeek
                && oldItem.id == newItem.id
                && oldItem.time == newItem.time)
    }

}