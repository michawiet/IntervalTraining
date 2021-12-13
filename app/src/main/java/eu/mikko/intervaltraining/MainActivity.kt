package eu.mikko.intervaltraining

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.adapters.ItemAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //weekday_recycler_view_items.layoutManager = LinearLayoutManager(this)
        //val itemAdapter = ItemAdapter(this, getItemList())
        //weekday_recycler_view_items.adapter = itemAdapter
    }

    //private fun getItemList(): ArrayList<String> {
    //    val list = ArrayList<String>()
    //    for(day in DayOfWeek.values()) {
    //        list.add(day.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
    //    }
//
    //    return list
    //}
}