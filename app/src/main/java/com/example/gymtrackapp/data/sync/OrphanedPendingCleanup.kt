package com.example.gymtrackapp.data.sync

import android.content.Context
import android.util.Log
import com.example.gymtrackapp.data.ExerciseDatabase

/**
 * Bezpieczny cleanup osieroconych rekordów w kolejce sync.
 *
 * Cel:
 * - nie dopuścić, żeby pojedyncze "dziecko bez rodzica" blokowało synchronizację (to już rozwiązaliśmy SKIP-em),
 * - oraz aby takie rekordy nie spamowały logów / nie wracały w nieskończoność jako pending.
 *
 * Zasady "bezpiecznego" sprzątania:
 * - dotykamy TYLKO rekordów z syncStatus != SYNCED (czyli i tak są w kolejce)
 * - usuwamy TYLKO rekordy, które są NAPRAWDĘ osierocone (brak rodzica w tabeli parent)
 * - jeśli rekord jest pending delete (syncStatus=2) to jego brak w chmurze nie jest problemem,
 *   a usunięcie go lokalnie nie popsuje UI (UI i tak filtruje deletedAtMs).
 *
 * UWAGA: to nie jest retencja/hard-delete soft-deleted po czasie. To tylko sprzątanie kolejki pending.
 */
object OrphanedPendingCleanup {

    private const val TAG = "OrphanedPendingCleanup"

    /**
     * Sprząta osierocone rekordy. Możesz to odpalać:
     * - na starcie aplikacji
     * - po logowaniu (po pullu)
     * - po wipe/logout (opcjonalnie)
     */
    suspend fun cleanup(context: Context) {
        val db = ExerciseDatabase.getDatabase(context)
        val trainingDao = db.trainingDao()
        val templateDao = db.templateDao()

        // Dzieci treningów:
        val deletedSessionExercises = trainingDao.deleteOrphanedPendingSessionExercises()
        val deletedSessionSets = trainingDao.deleteOrphanedPendingSessionSets()

        // Dzieci templatek:
        val deletedTemplateExercises = templateDao.deleteOrphanedPendingTemplateExercises()

        if (deletedSessionExercises + deletedSessionSets + deletedTemplateExercises > 0) {
            Log.i(
                TAG,
                "cleanup: removed orphaned pending rows: sessionExercises=$deletedSessionExercises sets=$deletedSessionSets templateExercises=$deletedTemplateExercises"
            )
        }
    }
}

