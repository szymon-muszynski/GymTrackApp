package com.example.gymtrackapp.data.sync

import android.content.Context

/**
 * Jednorazowy trigger cleanup przy starcie aplikacji (dla już zalogowanego usera).
 *
 * Cel:
 * - użytkownik może być zalogowany miesiącami, a periodic work może nie odpalić od razu
 * - chcemy wykonać "best-effort" cleanup po wejściu do aplikacji, bez przycisku debug
 *
 * Zabezpieczenie:
 * - uruchamiamy max raz na 24h (local gate), żeby nie robić tego przy każdym starcie.
 */
object StartupCleanupTrigger {

    private const val PREFS = "startup_cleanup_prefs"
    private const val KEY_LAST_ENQUEUE_MS = "last_enqueue_ms"

    private const val MIN_INTERVAL_MS: Long = 24L * 60L * 60L * 1000L

    fun enqueueIfDue(context: Context) {
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val now = System.currentTimeMillis()
        val last = prefs.getLong(KEY_LAST_ENQUEUE_MS, 0L)

        if (now - last < MIN_INTERVAL_MS) return

        // Ustawiamy timestamp przed enqueue (żeby nie spamować przy crash-loopach)
        prefs.edit().putLong(KEY_LAST_ENQUEUE_MS, now).apply()

        FirestoreCleanupWorker.enqueueOneTime(appContext)
    }
}

