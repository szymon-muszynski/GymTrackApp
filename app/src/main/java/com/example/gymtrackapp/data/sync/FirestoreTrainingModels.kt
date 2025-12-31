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
    val date: Long = 0L,
    val description: String = "",
    val updatedAtMs: Long = 0L,
    val deletedAtMs: Long? = null,
)

data class SessionExerciseDoc(
    val remoteId: String = "",
    val trainingSessionRemoteId: String = "",
    val exerciseId: String = "",
    val order: Int = 0,
    val updatedAtMs: Long = 0L,
    val deletedAtMs: Long? = null,
)

data class SessionSetDetailsDoc(
    val remoteId: String = "",
    val sessionExerciseRemoteId: String = "",
    val order: Int = 0,
    val reps: Int = 0,
    val weight: Float = 0f,
    val updatedAtMs: Long = 0L,
    val deletedAtMs: Long? = null,
)

internal fun TrainingSession.toDoc(): TrainingSessionDoc = TrainingSessionDoc(
    remoteId = remoteId,
    date = date,
    description = description,
    updatedAtMs = updatedAtMs,
    deletedAtMs = deletedAtMs,
)

internal fun SessionExercise.toDoc(sessionRemoteId: String): SessionExerciseDoc = SessionExerciseDoc(
    remoteId = remoteId,
    trainingSessionRemoteId = sessionRemoteId,
    exerciseId = exerciseId,
    order = order,
    updatedAtMs = updatedAtMs,
    deletedAtMs = deletedAtMs,
)

internal fun SessionSetDetails.toDoc(sessionExerciseRemoteId: String): SessionSetDetailsDoc =
    SessionSetDetailsDoc(
        remoteId = remoteId,
        sessionExerciseRemoteId = sessionExerciseRemoteId,
        order = order,
        reps = reps,
        weight = weight,
        updatedAtMs = updatedAtMs,
        deletedAtMs = deletedAtMs,
    )
