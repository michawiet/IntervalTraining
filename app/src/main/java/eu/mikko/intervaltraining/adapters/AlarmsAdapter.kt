package eu.mikko.intervaltraining.adapters

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.ConfigurationCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.model.TrainingNotification
import kotlinx.android.synthetic.main.alarm_item.view.*
import java.time.DayOfWeek
import java.time.format.TextStyle

class AlarmsAdapter(private val clickToChangeTimeListener: (TrainingNotification) -> Unit,
                    private val clickToToggleNotificationListener: (TrainingNotification) -> Unit
) : RecyclerView.Adapter<AlarmsAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(itemView: View, clickAtTimeTextView: (Int) -> Unit,
                          clickAtNotificationSwitch: (Int) -> Unit) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.tvTrainingNotificationTime.setOnClickListener { clickAtTimeTextView(adapterPosition) }
            itemView.switchTrainingNotification.setOnClickListener { clickAtNotificationSwitch(adapterPosition) }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<TrainingNotification>() {
        override fun areItemsTheSame(oldItem: TrainingNotification, newItem: TrainingNotification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TrainingNotification, newItem: TrainingNotification): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<TrainingNotification>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        return AlarmViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.alarm_item, parent, false),
            { clickToChangeTimeListener(differ.currentList[it]) },
            { clickToToggleNotificationListener(differ.currentList[it]) }
        )
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val currentItem = differ.currentList[position]
        holder.itemView.apply {
            tvDayOfWeekTrainingName.text =
                DayOfWeek
                    .valueOf(currentItem.dayOfWeek)
                    .getDisplayName(TextStyle.FULL, ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0])
            tvTrainingNotificationTime.text = String.format("%02d:%02d", currentItem.hour, currentItem.minute)
            switchTrainingNotification.isChecked = currentItem.isEnabled

            //tvTrainingNotificationTime.setTextColor(ContextCompat.getColor(tvTrainingNotificationTime.context, if(currentItem.isEnabled) R.color.primary_500) else R.color.black_inactive)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}