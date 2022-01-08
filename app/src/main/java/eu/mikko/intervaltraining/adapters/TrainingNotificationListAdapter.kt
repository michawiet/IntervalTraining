package eu.mikko.intervaltraining.adapters

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.ConfigurationCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.model.TrainingNotification
import kotlinx.android.synthetic.main.training_notification_item.view.*
import java.time.DayOfWeek
import java.time.format.TextStyle

class TrainingNotificationListAdapter : RecyclerView.Adapter<TrainingNotificationListAdapter.TrainingNotificationViewHolder>() {

    private var trainingNotificationList = emptyList<TrainingNotification>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TrainingNotificationViewHolder {
        return TrainingNotificationViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TrainingNotificationViewHolder, position: Int) {
        val currentItem = trainingNotificationList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return trainingNotificationList.size
    }

    fun setData(list: List<TrainingNotification>) {
        this.trainingNotificationList = list
        notifyDataSetChanged()
    }

    class TrainingNotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val weekdayNameTextView: TextView = itemView.training_name_text_view
        private val notificationTimeTextView: TextView = itemView.training_notification_time
        private val notificationSwitch: SwitchMaterial = itemView.training_notification_switch

        fun bind(data: TrainingNotification) {
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