package com.example.gymtrackapp.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.gymtrackapp.data.entity.PlannedWorkoutEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Planowanie alarmów dla planowanych treningów.
 *
 * Docelowo: 09:00 czasu lokalnego urządzenia w dniu treningu.
 *
 * UWAGA (Android 12+ / targetSdk 31+): exact alarms wymagają permission SCHEDULE_EXACT_ALARM
 * albo zwolnienia z ograniczeń energii. Na ten etap (backend/dev) używamy trybu inexact,
 * żeby nie crashować aplikacji na starcie.
 */
class NotificationScheduler(
    private val context: Context,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(workout: PlannedWorkoutEntity) {
        if (workout.deletedAtMs != null) return

        val triggerAtMillis = calculateTriggerAtMillis(workout.dateEpochDay)

        // Nie planujemy alarmów w przeszłości (np. po zmianie czasu).
        if (triggerAtMillis <= System.currentTimeMillis()) return

        val pi = createPendingIntent(workout)

        scheduleInexact(triggerAtMillis, pi)
    }

    fun cancel(workout: PlannedWorkoutEntity) {
        val pi = createPendingIntent(workout)
        alarmManager.cancel(pi)
        // Dobra praktyka: zwolnij PI, jeśli możliwe.
        pi.cancel()
    }

    private fun scheduleInexact(triggerAtMillis: Long, pi: PendingIntent) {
        // Inexact, ale działa bez SCHEDULE_EXACT_ALARM.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }
    }

    private fun createPendingIntent(workout: PlannedWorkoutEntity): PendingIntent {
        val intent = Intent(context, WorkoutNotificationReceiver::class.java)
            .setAction(Actions.ACTION_SHOW_WORKOUT_NOTIFICATION)
            .putExtra(Extras.EXTRA_WORKOUT_ID, workout.id)
            .putExtra(Extras.EXTRA_WORKOUT_TITLE, workout.title)
            .putExtra(Extras.EXTRA_WORKOUT_EPOCH_DAY, workout.dateEpochDay)

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        return PendingIntent.getBroadcast(
            context,
            workout.id.hashCode(),
            intent,
            flags
        )
    }

    private fun calculateTriggerAtMillis(dateEpochDay: Long): Long {
        val localDate = LocalDate.ofEpochDay(dateEpochDay)
        val localDateTime = LocalDateTime.of(localDate, LocalTime.of(9, 0))
        val zone = ZoneId.systemDefault()
        return localDateTime.atZone(zone).toInstant().toEpochMilli()
    }

    object Actions {
        const val ACTION_SHOW_WORKOUT_NOTIFICATION = "com.example.gymtrackapp.action.SHOW_WORKOUT_NOTIFICATION"
    }

    object Extras {
        const val EXTRA_WORKOUT_ID = "extra_workout_id"
        const val EXTRA_WORKOUT_TITLE = "extra_workout_title"
        const val EXTRA_WORKOUT_EPOCH_DAY = "extra_workout_epoch_day"
    }
}
