package com.example.gymtrackapp.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gymtrackapp.data.ExerciseDatabase
import com.example.gymtrackapp.data.dao.TrainingDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * MVP worker: wypycha lokalne zmiany (pending) do Firestore.
 *
 * Założenia:
 * - Room jest źródłem prawdy; zawsze zapisujemy lokalnie.
 * - syncStatus: 0=SYNCED, 1=PENDING_UPSERT, 2=PENDING_DELETE
 * - deletedAtMs != null oznacza soft delete.
 * - soft delete kaskadowy: gdy usuwasz sesję, w Room oznaczasz też exercises/sets.
 */
class TrainingSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result {
        val user = auth.currentUser ?: return Result.retry()
        val uid = user.uid

        val trainingDao: TrainingDao = ExerciseDatabase
            .getDatabase(applicationContext)
            .trainingDao()

        return try {
            // 1) Sessions (parent)
            val pendingSessions = trainingDao.getPendingSessions()
            for (session in pendingSessions) {
                val sessionRef = TrainingFirestorePaths.sessionsCol(db, uid).document(session.remoteId)
                sessionRef.set(session.toDoc(), SetOptions.merge()).await()
                trainingDao.markSessionSynced(session.id)
            }

            // 2) Exercises (child)
            val pendingExercises = trainingDao.getPendingSessionExercises()
            for (se in pendingExercises) {
                val sessionRemoteId = trainingDao.getSessionRemoteId(se.trainingSessionId)
                    ?: return Result.retry()

                val exerciseRef = TrainingFirestorePaths.exercisesCol(db, uid, sessionRemoteId)
                    .document(se.remoteId)

                exerciseRef.set(se.toDoc(sessionRemoteId), SetOptions.merge()).await()
                trainingDao.markSessionExerciseSynced(se.id)
            }

            // 3) Sets (grand-child)
            val pendingSets = trainingDao.getPendingSessionSets()
            for (set in pendingSets) {
                val parentExercise = trainingDao.getSessionExerciseById(set.sessionExerciseId)
                    ?: return Result.retry()

                val sessionRemoteId = trainingDao.getSessionRemoteId(parentExercise.trainingSessionId)
                    ?: return Result.retry()

                val setRef = TrainingFirestorePaths.setsCol(
                    db,
                    uid,
                    sessionRemoteId,
                    parentExercise.remoteId
                ).document(set.remoteId)

                setRef.set(set.toDoc(parentExercise.remoteId), SetOptions.merge()).await()
                trainingDao.markSessionSetSynced(set.id)
            }

            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        }
    }
}
