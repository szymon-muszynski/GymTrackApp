package com.example.gymtrackapp.data.sync

import android.content.Context
import android.util.Log
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
 */
class TrainingSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result {
        val user = auth.currentUser ?: return Result.success()
        val uid = user.uid

        val trainingDao: TrainingDao = ExerciseDatabase
            .getDatabase(applicationContext)
            .trainingDao()

        return try {
            // 1) Sessions (parent)
            val pendingSessions = trainingDao.getPendingSessions()
            for (session in pendingSessions) {
                val sessionRef = TrainingFirestorePaths.sessionsCol(db, uid)
                    .document(session.remoteId)

                sessionRef.set(session.toDoc(uid), SetOptions.merge()).await()
                trainingDao.markSessionSynced(session.id)
            }

            // 2) Exercises (child)
            val pendingExercises = trainingDao.getPendingSessionExercises()
            for (se in pendingExercises) {
                val sessionRemoteId = trainingDao.getSessionRemoteId(se.trainingSessionId)
                if (sessionRemoteId.isNullOrBlank()) {
                    Log.w(
                        TAG,
                        "doWork: SKIP SessionExercise id=${se.id} remoteId=${se.remoteId} because parent sessionId=${se.trainingSessionId} has no remoteId"
                    )
                    continue
                }

                val exerciseRef = TrainingFirestorePaths.exercisesCol(db, uid, sessionRemoteId)
                    .document(se.remoteId)

                exerciseRef.set(se.toDoc(uid, sessionRemoteId), SetOptions.merge()).await()
                trainingDao.markSessionExerciseSynced(se.id)
            }

            // 3) Sets (grand-child)
            val pendingSets = trainingDao.getPendingSessionSets()
            for (set in pendingSets) {
                val parentExercise = trainingDao.getSessionExerciseById(set.sessionExerciseId)
                if (parentExercise == null) {
                    Log.w(
                        TAG,
                        "doWork: SKIP SessionSetDetails id=${set.id} remoteId=${set.remoteId} because parent sessionExerciseId=${set.sessionExerciseId} not found"
                    )
                    continue
                }

                val sessionRemoteId = trainingDao.getSessionRemoteId(parentExercise.trainingSessionId)
                if (sessionRemoteId.isNullOrBlank()) {
                    Log.w(
                        TAG,
                        "doWork: SKIP SessionSetDetails id=${set.id} remoteId=${set.remoteId} because parent sessionId=${parentExercise.trainingSessionId} has no remoteId"
                    )
                    continue
                }

                val setRef = TrainingFirestorePaths.setsCol(
                    db,
                    uid,
                    sessionRemoteId,
                    parentExercise.remoteId
                ).document(set.remoteId)

                setRef.set(set.toDoc(uid, parentExercise.remoteId), SetOptions.merge()).await()
                trainingDao.markSessionSetSynced(set.id)
            }

            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "doWork: FAILED", t)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "TrainingSyncWorker"
    }
}
