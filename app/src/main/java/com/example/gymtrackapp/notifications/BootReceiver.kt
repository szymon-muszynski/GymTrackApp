package com.example.gymtrackapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.gymtrackapp.data.ExerciseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Po restarcie urządzenia AlarmManager nie pamięta alarmów.
 * Odtwarzamy je na podstawie danych z Room.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_TIME_CHANGED &&
            action != Intent.ACTION_TIMEZONE_CHANGED
        ) return

        // Odtwarzanie alarmów robimy w tle.
        val appContext = context.applicationContext
        val db = ExerciseDatabase.getDatabase(appContext)
        val dao = db.plannedWorkoutDao()
        val scheduler = NotificationScheduler(appContext)

        CoroutineScope(Dispatchers.IO).launch {
            val nowEpochDay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDate.now().toEpochDay()
            } else {
                // MinSdk prawdopodobnie i tak jest >= 26 (Compose), ale zostawiamy bezpieczny fallback.
                // Jeśli jednak byłby < 26, reschedule ograniczamy (brak java.time).
                0L
            }

            runCatching {
                val future = dao.getFutureWorkouts(nowEpochDay)
                future.forEach { scheduler.schedule(it) }
            }
        }
    }
}

