package eu.mikko.intervaltraining.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.model.Interval
import eu.mikko.intervaltraining.other.TrackingUtility.getFormattedTimeFromSeconds
import kotlinx.android.synthetic.main.levels_item.view.*

class LevelsAdapter(private val clickToSelectListener: (Interval) -> Unit) : RecyclerView.Adapter<LevelsAdapter.ProgressViewHolder>() {

    inner class ProgressViewHolder(itemView: View, clickAtItem: (Int) -> Unit) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.cardParent.setOnClickListener {
                if(currentlyChecked != adapterPosition) {
                    clickAtItem(adapterPosition)
                    notifyItemChanged(currentlyChecked)
                    notifyItemChanged(adapterPosition)
                    currentlyChecked = adapterPosition
                }
            }
        }
    }

    private var currentlyChecked = 1

    private val diffCallback = object : DiffUtil.ItemCallback<Interval>() {
        override fun areItemsTheSame(oldItem: Interval, newItem: Interval): Boolean {
            return oldItem.workoutLevel == newItem.workoutLevel
        }

        override fun areContentsTheSame(oldItem: Interval, newItem: Interval): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Interval>) = differ.submitList(list)

    fun setCurrentlyChecked(it: Int) {
        notifyItemChanged(currentlyChecked)
        currentlyChecked = it
        notifyItemChanged(currentlyChecked)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        return ProgressViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.levels_item, parent, false)
        ) { clickToSelectListener(differ.currentList[it]) }
    }

    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        val interval = differ.currentList[position]
        holder.itemView.apply {
            tvTrainingPlanNumber.text = interval.workoutLevel.toString()
            tvRunTime.text = getFormattedTimeFromSeconds(interval.runSeconds)
            tvWalkTime.text = getFormattedTimeFromSeconds(interval.walkSeconds)
            tvTotalTime.text = getFormattedTimeFromSeconds(interval.totalWorkoutTime)
            cardParent.isChecked = currentlyChecked == position
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}