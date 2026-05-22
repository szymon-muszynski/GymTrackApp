package com.example.gymtrackapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.gymtrackapp.R

/**
 * Odbiornik alarmu – pokazuje systemowe powiadomienie.
 *
 * Na tym etapie zakładamy, że app ma uprawnienia do notyfikacji (UI dojdzie później).
 */
class WorkoutNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationScheduler.Actions.ACTION_SHOW_WORKOUT_NOTIFICATION) return

        ensureNotificationChannel(context)

        val title = intent.getStringExtra(NotificationScheduler.Extras.EXTRA_WORKOUT_TITLE)
            ?: context.getString(R.string.app_name)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Zaplanowany trening")
            .setContentText(title)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = intent.getStringExtra(NotificationScheduler.Extras.EXTRA_WORKOUT_ID)?.hashCode()
            ?: title.hashCode()

        manager.notify(notificationId, notification)
    }

    private fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Planner reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Powiadomienia o zaplanowanych treningach"
        }

        nm.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "planner_reminders"
    }
}

