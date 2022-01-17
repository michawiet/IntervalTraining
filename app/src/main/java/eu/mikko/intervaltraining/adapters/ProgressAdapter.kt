package eu.mikko.intervaltraining.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.model.Run
import eu.mikko.intervaltraining.other.TrackingUtility.getFormattedStopWatchTime
import eu.mikko.intervaltraining.other.TrackingUtility.getKilometersPerMinuteFromMetersPerSecond
import kotlinx.android.synthetic.main.run_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class ProgressAdapter : RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder>() {

    inner class ProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    val diffCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        return ProgressViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.run_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.itemView.apply {
            tvLength.text = getFormattedStopWatchTime(run.timeInMillis)
            tvDistance.text = run.distanceInMeters.div(1000).toString()
            tvAvgSpeed.text = getKilometersPerMinuteFromMetersPerSecond(run.avgSpeedMetersPerSecond)
            tvRating.text = run.rating.toString()
            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            tvDateRunItem.text = dateFormat.format(calendar.time)
        }

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}