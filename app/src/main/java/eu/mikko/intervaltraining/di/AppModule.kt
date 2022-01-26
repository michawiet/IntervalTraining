package eu.mikko.intervaltraining.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eu.mikko.intervaltraining.data.IntervalTrainingDatabase
import eu.mikko.intervaltraining.other.Constants.DATABASE_ASSET_PATH
import eu.mikko.intervaltraining.other.Constants.DATABASE_NAME
import eu.mikko.intervaltraining.other.Constants.KEY_HOUR_PROGRESS_NOTIFICATION
import eu.mikko.intervaltraining.other.Constants.KEY_WORKOUT_LEVEL
import eu.mikko.intervaltraining.other.Constants.SHARED_PREFERENCES_NAME
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext app: Context) =
        Room.databaseBuilder(
        app,
        IntervalTrainingDatabase::class.java,
            DATABASE_NAME
        ).createFromAsset(
            DATABASE_ASSET_PATH
        ).build()

    @Singleton
    @Provides
    fun provideRunDao(db: IntervalTrainingDatabase) = db.getRunDao()

    @Singleton
    @Provides
    fun provideIntervalDao(db: IntervalTrainingDatabase) = db.getIntervalDao()

    @Singleton
    @Provides
    fun provideTrainingNotificationDao(db: IntervalTrainingDatabase) = db.getTrainingNotificationDao()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideWorkoutLevel(sharedPref: SharedPreferences) = sharedPref.getInt(KEY_WORKOUT_LEVEL, 1)

    //@Singleton
    //@Provides
    //fun provideSelectedDayForProgressNotification(sharedPref: SharedPreferences) = sharedPref.getString(KEY_SELECTED_DAY_PROGRESS_NOTIFICATION, "MONDAY")
//
    //@Singleton
    //@Provides
    //fun provideHourForProgressNotification(sharedPref: SharedPreferences) = sharedPref.getInt(KEY_HOUR_PROGRESS_NOTIFICATION, 12)
//
    //@Singleton
    //@Provides
    //fun provideMinuteForProgressNotification(sharedPref: SharedPreferences) = sharedPref.getInt(KEY_MINUTE_PROGRESS_NOTIFICATION, 0)
}