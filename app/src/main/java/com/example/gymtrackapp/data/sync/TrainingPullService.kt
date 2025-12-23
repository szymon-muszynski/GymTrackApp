package com.example.gymtrackapp.data.sync

import android.content.Context
import com.example.gymtrackapp.data.ExerciseDatabase
import com.example.gymtrackapp.data.dao.TrainingDao
import com.example.gymtrackapp.data.entity.SessionExercise
import com.example.gymtrackapp.data.entity.SessionSetDetails
import com.example.gymtrackapp.data.entity.TrainingSession
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Pull danych treningowych z Firestore do Room.
 * Zgodnie z ustaleniami: uruchamiamy tylko przy loginie.
 *
 * MVP: czyścimy lokalne dane treningowe i ściągamy wszystko dla danego uid.
 * Wszystkie rekordy zapisujemy jako SYNCED (syncStatus=0).
 */
class TrainingPullService(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val trainingDao: TrainingDao by lazy {
        ExerciseDatabase.getDatabase(context).trainingDao()
    }

    suspend fun wipeLocalTrainingData() {
        trainingDao.clearSessionSets()
        trainingDao.clearSessionExercises()
        trainingDao.clearTrainingSessions()
    }

    suspend fun pullAllForUser(uid: String) {
        // 1) Wipe lokalnych danych treningowych
        wipeLocalTrainingData()

        // 2) Pobierz sesje
        val sessionsSnap = TrainingFirestorePaths.sessionsCol(firestore, uid)
            .get()
            .await()

        for (sessionDoc in sessionsSnap.documents) {
            val sessionRemoteId = sessionDoc.id
            val session = sessionDoc.toObject(TrainingSessionDoc::class.java)
                ?: continue

            val localSessionId = trainingDao.insertTrainingSession(
                TrainingSession(
                    id = 0,
                    date = session.date,
                    description = session.description,
                    remoteId = sessionRemoteId,
                    updatedAtMs = session.updatedAtMs,
                    syncStatus = 0,
                    deletedAtMs = session.deletedAtMs
                )
            )

            // 3) Pobierz ćwiczenia subkolekcji
            val exercisesSnap = TrainingFirestorePaths.exercisesCol(firestore, uid, sessionRemoteId)
                .get()
                .await()

            for (exerciseDoc in exercisesSnap.documents) {
                val exerciseRemoteId = exerciseDoc.id
                val exercise = exerciseDoc.toObject(SessionExerciseDoc::class.java)
                    ?: continue

                val localSessionExerciseId = trainingDao.insertSessionExercise(
                    SessionExercise(
                        id = 0,
                        trainingSessionId = localSessionId,
                        exerciseId = exercise.exerciseId,
                        order = exercise.order,
                        remoteId = exerciseRemoteId,
                        updatedAtMs = exercise.updatedAtMs,
                        syncStatus = 0,
                        deletedAtMs = exercise.deletedAtMs
                    )
                )

                // 4) Pobierz sety subkolekcji
                val setsSnap = TrainingFirestorePaths.setsCol(
                    firestore,
                    uid,
                    sessionRemoteId,
                    exerciseRemoteId
                )
                    .get()
                    .await()

                for (setDoc in setsSnap.documents) {
                    val setRemoteId = setDoc.id
                    val set = setDoc.toObject(SessionSetDetailsDoc::class.java)
                        ?: continue

                    trainingDao.insertSessionSetDetails(
                        SessionSetDetails(
                            id = 0,
                            sessionExerciseId = localSessionExerciseId,
                            order = set.order,
                            reps = set.reps,
                            weight = set.weight,
                            remoteId = setRemoteId,
                            updatedAtMs = set.updatedAtMs,
                            syncStatus = 0,
                            deletedAtMs = set.deletedAtMs
                        )
                    )
                }
            }
        }
    }
}
