package com.example.gymtrackapp.data.sync

import com.example.gymtrackapp.data.entity.SessionExercise
import com.example.gymtrackapp.data.entity.SessionSetDetails
import com.example.gymtrackapp.data.entity.TrainingSession

/**
 * Minimalne DTO do Firestore.
 * Trzymamy remoteId jako documentId, ale też zapisujemy go w polu, żeby ułatwić debug.
 */

data class TrainingSessionDoc(
    val remoteId: String = "",
    val userId: String = "",
    val date: Long = 0L,
    val description: String = "",
    val note: String? = null,
    val updatedAtMs: Long = 0L,
    val deletedAtMs: Long? = null,
)

data class SessionExerciseDoc(
    val remoteId: String = "",
    val userId: String = "",
    val trainingSessionRemoteId: String = "",
    val exerciseId: String = "",
    val order: Int = 0,
    val updatedAtMs: Long = 0L,
    val deletedAtMs: Long? = null,
)

data class SessionSetDetailsDoc(
    val remoteId: String = "",
    val userId: String = "",
    val sessionExerciseRemoteId: String = "",
    val order: Int = 0,
    val reps: Int = 0,
    val weight: Float = 0f,
    val updatedAtMs: Long = 0L,
    val deletedAtMs: Long? = null,
)

internal fun TrainingSession.toDoc(userId: String): TrainingSessionDoc = TrainingSessionDoc(
    remoteId = remoteId,
    userId = userId,
    date = date,
    description = description,
    note = note,
    updatedAtMs = updatedAtMs,
    deletedAtMs = deletedAtMs,
)

internal fun SessionExercise.toDoc(userId: String, sessionRemoteId: String): SessionExerciseDoc = SessionExerciseDoc(
    remoteId = remoteId,
    userId = userId,
    trainingSessionRemoteId = sessionRemoteId,
    exerciseId = exerciseId,
    order = order,
    updatedAtMs = updatedAtMs,
    deletedAtMs = deletedAtMs,
)

internal fun SessionSetDetails.toDoc(userId: String, sessionExerciseRemoteId: String): SessionSetDetailsDoc =
    SessionSetDetailsDoc(
        remoteId = remoteId,
        userId = userId,
        sessionExerciseRemoteId = sessionExerciseRemoteId,
        order = order,
        reps = reps,
        weight = weight,
        updatedAtMs = updatedAtMs,
        deletedAtMs = deletedAtMs,
    )
