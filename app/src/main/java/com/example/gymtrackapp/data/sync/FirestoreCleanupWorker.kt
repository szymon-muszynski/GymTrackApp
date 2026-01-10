package com.example.gymtrackapp.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gymtrackapp.data.ExerciseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Okresowy cleanup hard-delete w Firestore + Room dla starych rekordów soft-deleted.
 *
 * Zasady:
 * - Retencja: 30 dni (cutoff = now - 30d)
 * - Room hard-delete: tylko gdy deletedAtMs < cutoff ORAZ syncStatus == SYNCED
 * - Firestore hard-delete: usuwa dokumenty (tombstones) z deletedAtMs < cutoff
 *
 * Kolejność Firestore (collectionGroup):
 * 1) sets
 * 2) exercises (training + templates)
 * 3) trainingSessions
 * 4) workoutTemplates
 * 5) planned_workouts
 * 6) customExercises
 */
class FirestoreCleanupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result {
        val user = auth.currentUser ?: return Result.success()
        val uid = user.uid

        val cutoffMs = System.currentTimeMillis() - RETENTION_MS

        return try {
            // Firestore hard-delete
            cleanupFirestore(uid = uid, cutoffMs = cutoffMs)

            // Room hard-delete (leaf -> root)
            cleanupRoom(cutoffMs = cutoffMs)

            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "doWork: FAILED", t)
            Result.retry()
        }
    }

    private suspend fun cleanupFirestore(uid: String, cutoffMs: Long) {
        // 1) sets (training)
        deleteCollectionGroupForUser(uid, collectionId = "sets", cutoffMs = cutoffMs)

        // 2) exercises (training + templates)
        deleteCollectionGroupForUser(uid, collectionId = "exercises", cutoffMs = cutoffMs)

        // 3) training sessions (top-level)
        deleteCollectionWithinUser(uid, collectionPath = "trainingSessions", cutoffMs = cutoffMs)

        // 4) workout templates (top-level)
        deleteCollectionWithinUser(uid, collectionPath = "workoutTemplates", cutoffMs = cutoffMs)

        // 5) planner
        deleteCollectionWithinUser(uid, collectionPath = "planned_workouts", cutoffMs = cutoffMs)

        // 6) custom exercises
        deleteCollectionWithinUser(uid, collectionPath = "customExercises", cutoffMs = cutoffMs)
    }

    private suspend fun deleteCollectionWithinUser(uid: String, collectionPath: String, cutoffMs: Long) {
        deleteByPagedQuery(
            query = db.collection("users").document(uid)
                .collection(collectionPath)
                .whereLessThan(FIELD_DELETED_AT_MS, cutoffMs)
                .orderBy(FIELD_DELETED_AT_MS),
            label = "users/$uid/$collectionPath"
        )
    }

    private suspend fun deleteCollectionGroupForUser(uid: String, collectionId: String, cutoffMs: Long) {
        deleteByPagedQuery(
            query = db.collectionGroup(collectionId)
                .whereEqualTo(FIELD_USER_ID, uid)
                .whereLessThan(FIELD_DELETED_AT_MS, cutoffMs)
                .orderBy(FIELD_DELETED_AT_MS)
                .orderBy(FIELD_DOC_ID),
            label = "collectionGroup($collectionId) userId=$uid"
        )
    }

    private suspend fun deleteByPagedQuery(
        query: com.google.firebase.firestore.Query,
        label: String
    ) {
        var lastDoc: com.google.firebase.firestore.DocumentSnapshot? = null
        var totalDeleted = 0

        while (true) {
            var q = query.limit(PAGE_SIZE.toLong())
            if (lastDoc != null) q = q.startAfter(lastDoc)

            val snap = q.get().await()
            if (snap.isEmpty) break

            val refs = snap.documents.map { it.reference }
            commitDeleteBatches(refs)
            totalDeleted += refs.size

            lastDoc = snap.documents.last()
            if (snap.size() < PAGE_SIZE) break
        }

        if (totalDeleted > 0) {
            Log.i(TAG, "deleteByPagedQuery: deleted=$totalDeleted from $label")
        }
    }

    private suspend fun commitDeleteBatches(refs: List<DocumentReference>) {
        var index = 0
        while (index < refs.size) {
            val end = minOf(index + BATCH_DELETE_LIMIT, refs.size)
            val batch = db.batch()
            for (i in index until end) {
                batch.delete(refs[i])
            }
            batch.commit().await()
            index = end
        }
    }

    private suspend fun cleanupRoom(cutoffMs: Long) {
        val roomDb = ExerciseDatabase.getDatabase(applicationContext)
        val trainingDao = roomDb.trainingDao()
        val templateDao = roomDb.templateDao()
        val plannedWorkoutDao = roomDb.plannedWorkoutDao()
        val exerciseDao = roomDb.exerciseDao()

        // Training leaf -> root
        trainingDao.purgeDeletedSessionSets(cutoffMs)
        trainingDao.purgeDeletedSessionExercises(cutoffMs)
        trainingDao.purgeDeletedTrainingSessions(cutoffMs)

        // Templates leaf -> root
        templateDao.purgeDeletedTemplateExercises(cutoffMs)
        templateDao.purgeDeletedWorkoutTemplates(cutoffMs)

        // Planner
        plannedWorkoutDao.purgeDeletedPlans(cutoffMs)

        // Custom exercises only (seed stays)
        exerciseDao.purgeDeletedCustomExercises(cutoffMs)
    }

    companion object {
        private const val TAG = "FirestoreCleanupWorker"

        private const val FIELD_USER_ID = "userId"
        private const val FIELD_DELETED_AT_MS = "deletedAtMs"
        private const val FIELD_DOC_ID = "__name__"

        private const val RETENTION_MS: Long = 30L * 24L * 60L * 60L * 1000L

        private const val BATCH_DELETE_LIMIT = 450
        private const val PAGE_SIZE = 500

        /**
         * MANUAL TRIGGER (debug): wymusza jednorazowe uruchomienie cleanup "tu i teraz".
         *
         * Przykład użycia (np. przycisk w debug screen):
         * FirestoreCleanupWorker.enqueueOneTime(context)
         */
        fun enqueueOneTime(context: Context) {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()

            val request = androidx.work.OneTimeWorkRequestBuilder<FirestoreCleanupWorker>()
                .setConstraints(constraints)
                .build()

            androidx.work.WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "firestore_room_cleanup_manual",
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    request
                )
        }

        /**
         * WYMAGANE INDEKSY (Firestore) dla docelowych zapytań collectionGroup:
         *
         * 1) collectionGroup("sets"):
         *    - gdzie: userId == <uid>
         *    - oraz: deletedAtMs < <cutoff>
         *    - sort: deletedAtMs ASC, __name__ ASC
         *    => Composite index: userId ASC, deletedAtMs ASC
         *
         * 2) collectionGroup("exercises"):
         *    - gdzie: userId == <uid>
         *    - oraz: deletedAtMs < <cutoff>
         *    - sort: deletedAtMs ASC, __name__ ASC
         *    => Composite index: userId ASC, deletedAtMs ASC
         *
         * Dla top-level kolekcji pod users/{uid}/... zwykle wystarczy pojedynczy indeks po deletedAtMs
         * (Firestore ma single-field indices). Jeśli wyłączysz single-field, dodaj:
         * - trainingSessions: deletedAtMs ASC
         * - workoutTemplates: deletedAtMs ASC
         * - planned_workouts: deletedAtMs ASC
         * - customExercises: deletedAtMs ASC
         */
    }
}
