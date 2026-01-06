package com.example.gymtrackapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.gymtrackapp.data.ExerciseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_TIME_CHANGED &&
            action != Intent.ACTION_TIMEZONE_CHANGED
        ) return

        val appContext = context.applicationContext
        val db = ExerciseDatabase.getDatabase(appContext)
        val dao = db.plannedWorkoutDao()
        val scheduler = NotificationScheduler(appContext)

        // Informujemy system, że potrzebujemy więcej czasu (ok. 10s) na pracę w tle.
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val nowEpochDay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    LocalDate.now().toEpochDay()
                } else {
                    // Fallback dla starszych systemów (chociaż minSdk jest wysokie)
                    0L
                }

                runCatching {
                    val future = dao.getFutureWorkouts(nowEpochDay)
                    future.forEach { scheduler.schedule(it) }
                }.onFailure { e ->
                    e.printStackTrace()
                }

            } finally {
                pendingResult.finish()
            }
        }
    }
}