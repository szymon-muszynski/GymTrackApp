package com.example.gymtrackapp.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object CleanupScheduler {
    private const val UNIQUE_WORK_NAME = "firestore_room_cleanup"

    /**
     * Uruchamia okresowy cleanup tombstone'ów (Firestore + Room).
     *
     * - 1x dziennie
     * - tylko na Wi‑Fi (UNMETERED)
     *
     * WorkManager sam odtworzy zadanie po restarcie urządzenia.
     */
    fun enqueuePeriodic(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<FirestoreCleanupWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }
}
