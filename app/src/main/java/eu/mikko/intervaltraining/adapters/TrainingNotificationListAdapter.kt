package eu.mikko.intervaltraining.adapters

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.model.TrainingNotification
import kotlinx.android.synthetic.main.training_notification_item.view.*
import java.time.DayOfWeek
import java.time.format.TextStyle

class TrainingNotificationListAdapter(private val clickToChangeTimeListener: (TrainingNotification) -> Unit,
                                      private val clickToToggleNotificationListener: (TrainingNotification) -> Unit
) : RecyclerView.Adapter<TrainingNotificationListAdapter.TrainingNotificationViewHolder>() {

    private var trainingNotificationList = emptyList<TrainingNotification>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TrainingNotificationViewHolder {
        return TrainingNotificationViewHolder.create(parent, {
            clickToChangeTimeListener(trainingNotificationList[it])
        }, {
            clickToToggleNotificationListener(trainingNotificationList[it])
        })
    }

    override fun onBindViewHolder(holder: TrainingNotificationViewHolder, position: Int) {
        val currentItem = trainingNotificationList[position]
        holder.weekdayNameTextView.text =
            DayOfWeek
            .valueOf(currentItem.dayOfWeek)
            .getDisplayName(TextStyle.FULL, ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0])
        holder.notificationTimeTextView.text = String.format("%02d:%02d", currentItem.hour, currentItem.minute)
        holder.notificationSwitch.isChecked = currentItem.isEnabled

        if(currentItem.isEnabled) {
            holder.notificationTimeTextView.setTextColor(ContextCompat.getColor(holder.notificationTimeTextView.context, R.color.primary_500))
        } else {
            holder.notificationTimeTextView.setTextColor(ContextCompat.getColor(holder.notificationTimeTextView.context, R.color.black_inactive))
        }
    }

    override fun getItemCount(): Int {
        return trainingNotificationList.size
    }

    fun setData(list: List<TrainingNotification>) {
        this.trainingNotificationList = list
        notifyDataSetChanged()
    }

    class TrainingNotificationViewHolder(itemView: View, clickAtTimeTextView: (Int) -> Unit,
                                         clickAtNotificationSwitch: (Int) -> Unit) : RecyclerView.ViewHolder(itemView) {
        val weekdayNameTextView: TextView = itemView.training_name_text_view
        val notificationTimeTextView: TextView = itemView.training_notification_time
        val notificationSwitch: SwitchMaterial = itemView.training_notification_switch

        init {
            notificationTimeTextView.setOnClickListener { clickAtTimeTextView(adapterPosition) }
            notificationSwitch.setOnClickListener { clickAtNotificationSwitch(adapterPosition) }
        }

        companion object {
            fun create(parent: ViewGroup, clickAtTimeTextView: (Int) -> Unit, clickToToggleNotificationListener: (Int) -> Unit): TrainingNotificationViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.training_notification_item, parent, false)
                return TrainingNotificationViewHolder(view, clickAtTimeTextView, clickToToggleNotificationListener)
            }
        }
    }


}