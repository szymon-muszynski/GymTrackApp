package com.example.gymtrackapp.data.sync

import android.content.Context
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
        wipeLocalCustomExercises()

        val snap = ExerciseFirestorePaths.customExercisesCol(firestore, uid)
            .get()
            .await()

        for (doc in snap.documents) {
            val remote = doc.toObject(CustomExerciseDoc::class.java) ?: continue

            // Zabezpieczenie: jeśli dokument nie ma id w polu, użyj documentId
            val normalized = if (remote.id.isBlank()) remote.copy(id = doc.id) else remote

            val entity = normalized.toEntity(uid).copy(
                syncStatus = ExerciseSyncStatus.SYNCED
            )

            exerciseDao.upsertExercise(entity)
        }
    }
}

