package eu.mikko.intervaltraining.adapters

import android.app.TimePickerDialog
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout.LayoutParams
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.data.DayOfWeekRecyclerViewItem
import kotlinx.android.synthetic.main.training_notification_item.view.*
import java.time.DayOfWeek

class DayOfWeekNotificationAdapter(private val dayOfWeekRecyclerViewItemList: List<DayOfWeekRecyclerViewItem>) : RecyclerView.Adapter<DayOfWeekNotificationAdapter.DayOfWeekNotificationViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DayOfWeekNotificationViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.training_notification_item, parent, false)

        return DayOfWeekNotificationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DayOfWeekNotificationViewHolder, position: Int) {
        val currentItem = dayOfWeekRecyclerViewItemList[position]

        holder.notificationTimeTextView.text = currentItem.notificationTime
        holder.weekdayNameTextView.text = currentItem.dayOfWeekLocalName
        holder.notificationSwitch.isChecked = currentItem.isNotificationEnabled
        holder.dayOfWeek = currentItem.dayOfWeek
    }

    override fun getItemCount() = dayOfWeekRecyclerViewItemList.size

    class DayOfWeekNotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val weekdayNameTextView: TextView = itemView.training_name_text_view
        val notificationTimeTextView: TextView = itemView.training_notification_time
        val notificationSwitch: SwitchMaterial = itemView.training_notification_switch
        var dayOfWeek: DayOfWeek = DayOfWeek.MONDAY

        private val notificationToast: Toast = Toast.makeText(itemView.context, "", Toast.LENGTH_SHORT)

        private val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            //TODO("make sure that if the ")
            notificationTimeTextView.text = String.format("%02d:%02d", hourOfDay, minute)
            if(notificationSwitch.isChecked) {
                //TODO("update the existing alarm")
            }
            notificationSwitch.isChecked = true
        }

        init {
            notificationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked) {
                    notificationTimeTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.black_active))
                    weekdayNameTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.black_inactive))
                    showToast("Training notification for $dayOfWeek enabled!")
                } else {
                    notificationTimeTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.black_inactive))
                    weekdayNameTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.black_disabled))
                    showToast("Training notification for $dayOfWeek disabled!")
                }
            }

            notificationTimeTextView.setOnClickListener(this)
        }

        private fun showToast(s: String) {
            notificationToast.cancel()
            notificationToast.setText(s)
            notificationToast.show()
        }

        override fun onClick(v: View?) {
            val dialog = TimePickerDialog(
                itemView.context,
                timeSetListener,
                Integer.parseInt(notificationTimeTextView.text.subSequence(0, 2).toString()),
                Integer.parseInt(notificationTimeTextView.text.subSequence(3, 5).toString()),
                true
            )
            val lp = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            val tv = TextView(itemView.context)
            tv.layoutParams = lp
            tv.setPadding(10, 10, 10, 10)
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20F)
            tv.setTextColor(ContextCompat.getColor(itemView.context, R.color.black_active))
            tv.gravity = Gravity.CENTER
            tv.text = weekdayNameTextView.text

            dialog.setCustomTitle(tv)
            dialog.show()
        }
    }
}