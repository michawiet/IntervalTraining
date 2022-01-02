package eu.mikko.intervaltraining.adapters

import android.app.TimePickerDialog
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout.LayoutParams
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
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

        private val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            //TODO("add alarm")
            notificationTimeTextView.text = String.format("%02d:%02d", hourOfDay, minute)
            notificationSwitch.isChecked = true
        }

        init {
            notificationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked) {
                    imageView.setImageResource(R.drawable.ic_round_alarm_on_24)
                    imageView.setColorFilter(ContextCompat.getColor(itemView.context, R.color.black_inactive))
                    notificationTimeTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.black_active))
                    weekdayNameTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.black_inactive))

                    Toast.makeText(itemView.context, "Training notification for ${weekdayNameTextView.text} enabled!", Toast.LENGTH_SHORT).show()
                } else {
                    imageView.setImageResource(R.drawable.ic_round_alarm_off_24)
                    imageView.setColorFilter(ContextCompat.getColor(itemView.context, R.color.black_disabled))
                    notificationTimeTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.black_inactive))
                    weekdayNameTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.black_disabled))

                    Toast.makeText(itemView.context, "Training notification for ${weekdayNameTextView.text} disabled!", Toast.LENGTH_SHORT).show()
                }
            }

            imageView.setOnClickListener {
                val dialog = TimePickerDialog(
                    itemView.context,
                    timeSetListener,
                    Integer.parseInt(notificationTimeTextView.text.subSequence(0, 2).toString()),
                    Integer.parseInt(notificationTimeTextView.text.subSequence(3, 5).toString()),
                    true)
                val lp = LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT)
                val tv = TextView(itemView.context)
                tv.layoutParams = lp
                tv.setPadding(10, 10, 10, 10)
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20F)
                tv.setTextColor(Color.parseColor("#000000"))
                tv.gravity = Gravity.CENTER
                tv.text = weekdayNameTextView.text

                dialog.setCustomTitle(tv)
                dialog.show()
            }


        }
    }
}