package com.example.gymtrackapp.data.sync

import com.example.gymtrackapp.data.entity.TemplateExercise
import com.example.gymtrackapp.data.entity.WorkoutTemplate

data class WorkoutTemplateDoc(
    val remoteId: String = "",
    val name: String = "",
    val createdAt: Long = 0L,
    val updatedAtMs: Long = 0L,
    val deletedAtMs: Long? = null,
)

data class TemplateExerciseDoc(
    val remoteId: String = "",
    val templateRemoteId: String = "",
    val exerciseId: String = "",
    val order: Int = 0,
    val updatedAtMs: Long = 0L,
    val deletedAtMs: Long? = null,
)

internal fun WorkoutTemplate.toDoc(): WorkoutTemplateDoc = WorkoutTemplateDoc(
    remoteId = remoteId,
    name = name,
    createdAt = createdAt,
    updatedAtMs = updatedAtMs,
    deletedAtMs = deletedAtMs,
)

internal fun TemplateExercise.toDoc(templateRemoteId: String): TemplateExerciseDoc = TemplateExerciseDoc(
    remoteId = remoteId,
    templateRemoteId = templateRemoteId,
    exerciseId = exerciseId,
    order = order,
    updatedAtMs = updatedAtMs,
    deletedAtMs = deletedAtMs,
)

