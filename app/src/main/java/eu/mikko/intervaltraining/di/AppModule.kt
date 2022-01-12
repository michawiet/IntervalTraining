package eu.mikko.intervaltraining.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eu.mikko.intervaltraining.data.IntervalTrainingDatabase
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
        "interval_training"
    ).createFromAsset(
        "database/interval_training.db"
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
}