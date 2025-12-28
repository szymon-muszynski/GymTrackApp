package com.example.gymtrackapp.data.sync

import android.content.Context
import android.util.Log
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
        val startedAt = System.currentTimeMillis()
        Log.d(TAG, "pullAllForUser: START uid=$uid")

        var sessionsInserted = 0
        var exercisesInserted = 0
        var exercisesSkippedMissingExercise = 0
        var setsInserted = 0

        // 1) Wipe lokalnych danych treningowych
        try {
            wipeLocalTrainingData()
            Log.d(TAG, "pullAllForUser: wipeLocalTrainingData DONE")
        } catch (t: Throwable) {
            Log.e(TAG, "pullAllForUser: wipeLocalTrainingData FAILED", t)
            throw t
        }

        // 2) Pobierz sesje
        val sessionsSnap = try {
            TrainingFirestorePaths.sessionsCol(firestore, uid)
                .get()
                .await()
        } catch (t: Throwable) {
            Log.e(TAG, "pullAllForUser: FAILED at sessions fetch", t)
            throw t
        }

        Log.d(TAG, "pullAllForUser: sessions fetched count=${sessionsSnap.size()}")

        for ((sessionIndex, sessionDoc) in sessionsSnap.documents.withIndex()) {
            val sessionRemoteId = sessionDoc.id
            val session = sessionDoc.toObject(TrainingSessionDoc::class.java)

            if (session == null) {
                Log.w(TAG, "pullAllForUser: sessionDoc.toObject == null (remoteId=$sessionRemoteId)")
                continue
            }

            if (sessionIndex % 10 == 0) {
                Log.d(
                    TAG,
                    "pullAllForUser: processing session ${sessionIndex + 1}/${sessionsSnap.size()} remoteId=$sessionRemoteId date=${session.date} deletedAtMs=${session.deletedAtMs}"
                )
            }

            val localSessionId = try {
                trainingDao.insertTrainingSession(
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
            } catch (t: Throwable) {
                Log.e(TAG, "pullAllForUser: FAILED inserting session remoteId=$sessionRemoteId", t)
                throw t
            }
            sessionsInserted++

            // 3) Pobierz ćwiczenia subkolekcji
            val exercisesSnap = try {
                TrainingFirestorePaths.exercisesCol(firestore, uid, sessionRemoteId)
                    .get()
                    .await()
            } catch (t: Throwable) {
                Log.e(TAG, "pullAllForUser: FAILED at exercises fetch sessionRemoteId=$sessionRemoteId", t)
                throw t
            }

            Log.d(TAG, "pullAllForUser: session remoteId=$sessionRemoteId exercises fetched count=${exercisesSnap.size()}")

            for (exerciseDoc in exercisesSnap.documents) {
                val exerciseRemoteId = exerciseDoc.id
                val exercise = exerciseDoc.toObject(SessionExerciseDoc::class.java)

                if (exercise == null) {
                    Log.w(TAG, "pullAllForUser: exerciseDoc.toObject == null (sessionRemoteId=$sessionRemoteId exerciseRemoteId=$exerciseRemoteId)")
                    continue
                }

                // FK w Room: session_exercises.exerciseId -> exercises.id
                // Jeśli w DB nie ma takiego ćwiczenia (seed albo custom), insert poleci SQLiteConstraintException.
                val exerciseExists = try {
                    trainingDao.countExercisesById(exercise.exerciseId) > 0
                } catch (t: Throwable) {
                    Log.e(TAG, "pullAllForUser: FAILED checking existence for exerciseId=${exercise.exerciseId}", t)
                    throw t
                }

                if (!exerciseExists) {
                    exercisesSkippedMissingExercise++
                    Log.e(
                        TAG,
                        "pullAllForUser: SKIP SessionExercise because exercises table has no id='${exercise.exerciseId}'. (sessionRemoteId=$sessionRemoteId exerciseRemoteId=$exerciseRemoteId)"
                    )
                    // Nie pobieramy setów, bo i tak nie mamy parenta w Room.
                    continue
                }

                val localSessionExerciseId = try {
                    trainingDao.insertSessionExercise(
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
                } catch (t: Throwable) {
                    Log.e(
                        TAG,
                        "pullAllForUser: FAILED inserting sessionExercise sessionRemoteId=$sessionRemoteId exerciseRemoteId=$exerciseRemoteId exerciseId=${exercise.exerciseId}",
                        t
                    )
                    throw t
                }
                exercisesInserted++

                // 4) Pobierz sety subkolekcji
                val setsSnap = try {
                    TrainingFirestorePaths.setsCol(
                        firestore,
                        uid,
                        sessionRemoteId,
                        exerciseRemoteId
                    )
                        .get()
                        .await()
                } catch (t: Throwable) {
                    Log.e(
                        TAG,
                        "pullAllForUser: FAILED at sets fetch sessionRemoteId=$sessionRemoteId exerciseRemoteId=$exerciseRemoteId",
                        t
                    )
                    throw t
                }

                if (setsSnap.size() > 0) {
                    Log.d(
                        TAG,
                        "pullAllForUser: sets fetched count=${setsSnap.size()} (sessionRemoteId=$sessionRemoteId exerciseRemoteId=$exerciseRemoteId)"
                    )
                }

                for (setDoc in setsSnap.documents) {
                    val setRemoteId = setDoc.id
                    val set = setDoc.toObject(SessionSetDetailsDoc::class.java)

                    if (set == null) {
                        Log.w(TAG, "pullAllForUser: setDoc.toObject == null (setRemoteId=$setRemoteId)")
                        continue
                    }

                    try {
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
                    } catch (t: Throwable) {
                        Log.e(
                            TAG,
                            "pullAllForUser: FAILED inserting set sessionRemoteId=$sessionRemoteId exerciseRemoteId=$exerciseRemoteId setRemoteId=$setRemoteId",
                            t
                        )
                        throw t
                    }
                    setsInserted++
                }
            }
        }

        val elapsedMs = System.currentTimeMillis() - startedAt
        val roomSessions = try { trainingDao.countTrainingSessionsAll() } catch (_: Throwable) { -1 }
        val roomExercises = try { trainingDao.countSessionExercisesAll() } catch (_: Throwable) { -1 }
        val roomSets = try { trainingDao.countSessionSetsAll() } catch (_: Throwable) { -1 }

        Log.d(
            TAG,
            "pullAllForUser: DONE sessionsInserted=$sessionsInserted exercisesInserted=$exercisesInserted setsInserted=$setsInserted skippedMissingExercise=$exercisesSkippedMissingExercise elapsedMs=$elapsedMs roomCounts(sessions=$roomSessions exercises=$roomExercises sets=$roomSets)"
        )
    }

    private companion object {
        private const val TAG = "TrainingPullService"
    }
}
