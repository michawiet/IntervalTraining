package eu.mikko.intervaltraining.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import eu.mikko.intervaltraining.data.DayOfWeekRecyclerViewItem

//creating the database logic, extending the SQLiteOpenHelper base class
class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "IntervalTraining"

        private val TABLE_NOTIFICATIONS = "NotificationsTable"

        private val KEY_ID = "_id"
        private val KEY_DAY_OF_WEEK = "dayOfWeek"
        private val KEY_TIME = "time"
        private val KEY_IS_ENABLED = "isEnabled"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        //creating table with fields
        val CREATE_NOTIFICATIONS_TABLE = ("CREATE TABLE " + TABLE_NOTIFICATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_DAY_OF_WEEK + " TEXT NON NULL UNIQUE,"
                + KEY_TIME + " TEXT NON NULL,"
                + KEY_IS_ENABLED + " BOOLEAN NOT NULL"
                + ")")
        db?.execSQL(CREATE_NOTIFICATIONS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_NOTIFICATIONS")
        onCreate(db)
    }

    fun addTrainingNotification(dayOfWeekRecyclerViewItem: DayOfWeekRecyclerViewItem): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_DAY_OF_WEEK, dayOfWeekRecyclerViewItem.dayOfWeek.name)
        contentValues.put(KEY_TIME, dayOfWeekRecyclerViewItem.notificationTime)
        contentValues.put(KEY_IS_ENABLED, dayOfWeekRecyclerViewItem.isNotificationEnabled)

        val success = db.insert(TABLE_NOTIFICATIONS, null, contentValues)

        db.close()

        return success
    }

    //fun
}