package eu.mikko.intervaltraining.di

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import eu.mikko.intervaltraining.MainActivity
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.other.Constants

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ) = FusedLocationProviderClient(app)

    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(@ApplicationContext app: Context) = PendingIntent.getActivity(
        app, 8,
        Intent(app, MainActivity::class.java),
        FLAG_UPDATE_CURRENT)

    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(@ApplicationContext app: Context, pendingIntent: PendingIntent) =
        NotificationCompat.Builder(app, Constants.TRACKING_NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(false)
            .setSmallIcon(R.drawable.ic_round_directions_run_24)
            .setContentTitle(app.getString(R.string.app_name))
            .setContentText(app.getString(R.string.activity_type_walk))
            .setContentIntent(pendingIntent)
}