package eu.mikko.intervaltraining.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import eu.mikko.intervaltraining.R
import kotlinx.android.synthetic.main.item_custom_row_weekday.view.*

class ItemAdapter(val context: Context, val items: ArrayList<String>) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_custom_row_weekday,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemAdapter.ViewHolder, position: Int) {
        val item = items.get(position)
        holder.weekday.text = item
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each item to
        val weekday = view.weekday_name_text_view
        //val time = view.notification_time
    }
}