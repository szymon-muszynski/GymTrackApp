package com.example.gymtrackapp.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gymtrackapp.data.ExerciseDatabase
import com.example.gymtrackapp.data.dao.ExerciseDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Worker: wypycha lokalne zmiany (pending) customowych ćwiczeń do Firestore.
 *
 * Założenia:
 * - syncStatus: 0=SYNCED, 1=PENDING_UPSERT, 2=PENDING_DELETE
 * - deletedAtMs != null oznacza soft delete (również w Firestore).
 */
class ExerciseSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result {
        val user = auth.currentUser ?: return Result.success()
        val uid = user.uid

        val exerciseDao: ExerciseDao = ExerciseDatabase
            .getDatabase(applicationContext)
            .exerciseDao()

        return try {
            val pending = exerciseDao.getPendingCustomExercises()
            for (exercise in pending) {
                val ref = ExerciseFirestorePaths.customExercisesCol(db, uid)
                    .document(exercise.id)

                ref.set(exercise.toCustomDoc(uid), SetOptions.merge()).await()
                exerciseDao.markExerciseSynced(exercise.id)
            }

            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        }
    }
}
