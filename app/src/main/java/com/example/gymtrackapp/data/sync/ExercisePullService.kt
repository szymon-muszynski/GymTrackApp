package com.example.gymtrackapp.data.sync

import android.content.Context
import android.util.Log
import com.example.gymtrackapp.data.ExerciseDatabase
import com.example.gymtrackapp.data.dao.ExerciseDao
import com.example.gymtrackapp.data.entity.ExerciseSyncStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Pull customowych ćwiczeń z Firestore do Room. Uruchamiane tylko przy loginie.
 * MVP: wipe customów + pełny pull.
 * Seedowane ćwiczenia zostają lokalnie.
 */
class ExercisePullService(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val exerciseDao: ExerciseDao by lazy {
        ExerciseDatabase.getDatabase(context).exerciseDao()
    }

    suspend fun wipeLocalCustomExercises() {
        exerciseDao.clearCustomExercises()
    }

    suspend fun pullAllCustomForUser(uid: String) {
        val startedAt = System.currentTimeMillis()
        Log.d(TAG, "pullAllCustomForUser: START uid=$uid")

        var upserted = 0
        var skippedNull = 0

        try {
            wipeLocalCustomExercises()
            Log.d(TAG, "pullAllCustomForUser: wipeLocalCustomExercises DONE")
        } catch (t: Throwable) {
            Log.e(TAG, "pullAllCustomForUser: wipeLocalCustomExercises FAILED", t)
            throw t
        }

        val snap = try {
            ExerciseFirestorePaths.customExercisesCol(firestore, uid)
                .get()
                .await()
        } catch (t: Throwable) {
            Log.e(TAG, "pullAllCustomForUser: FAILED at fetch", t)
            throw t
        }

        Log.d(TAG, "pullAllCustomForUser: fetched count=${snap.size()}")

        for (doc in snap.documents) {
            val remote = doc.toObject(CustomExerciseDoc::class.java)
            if (remote == null) {
                skippedNull++
                Log.w(TAG, "pullAllCustomForUser: CustomExerciseDoc null (docId=${doc.id})")
                continue
            }

            // Zabezpieczenie: jeśli dokument nie ma id w polu, użyj documentId
            val normalized = if (remote.id.isBlank()) remote.copy(id = doc.id) else remote

            try {
                val entity = normalized.toEntity(uid).copy(
                    syncStatus = ExerciseSyncStatus.SYNCED
                )
                exerciseDao.upsertExercise(entity)
                upserted++
            } catch (t: Throwable) {
                Log.e(TAG, "pullAllCustomForUser: FAILED upserting docId=${doc.id} normalizedId=${normalized.id}", t)
                throw t
            }
        }

        val elapsedMs = System.currentTimeMillis() - startedAt
        Log.d(TAG, "pullAllCustomForUser: DONE upserted=$upserted skippedNull=$skippedNull elapsedMs=$elapsedMs")
    }

    private companion object {
        private const val TAG = "ExercisePullService"
    }
}
