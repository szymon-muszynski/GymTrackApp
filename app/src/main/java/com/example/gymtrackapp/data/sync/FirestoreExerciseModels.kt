package com.example.gymtrackapp.data.sync

import com.example.gymtrackapp.data.entity.Exercise
import com.example.gymtrackapp.data.entity.SyncStatus

/**
 * DTO dla customowych ćwiczeń w Firestore.
 * DocumentId = exercise.id (np. custom_UUID).
 */
data class CustomExerciseDoc(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val force: String? = null,
    val level: String = "beginner",
    val mechanic: String? = null,
    val equipment: String? = null,
    val primaryMuscles: List<String> = emptyList(),
    val secondaryMuscles: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val category: String = "strength",
    val images: List<String> = emptyList(),
    val isCustom: Boolean = true,
    val createdByUserId: String? = null,
    val createdAt: Long = 0L,
    val updatedAtMs: Long = 0L,
    val deletedAtMs: Long? = null,
)

internal fun Exercise.toCustomDoc(userId: String): CustomExerciseDoc = CustomExerciseDoc(
    id = id,
    userId = userId,
    name = name,
    force = force,
    level = level,
    mechanic = mechanic,
    equipment = equipment,
    primaryMuscles = primaryMuscles,
    secondaryMuscles = secondaryMuscles,
    instructions = instructions,
    category = category,
    images = images,
    isCustom = true,
    createdByUserId = createdByUserId,
    createdAt = createdAt,
    updatedAtMs = updatedAtMs,
    deletedAtMs = deletedAtMs,
)

internal fun CustomExerciseDoc.toEntity(uid: String): Exercise = Exercise(
    id = id,
    name = name,
    force = force,
    level = level,
    mechanic = mechanic,
    equipment = equipment,
    primaryMuscles = primaryMuscles,
    secondaryMuscles = secondaryMuscles,
    instructions = instructions,
    category = category,
    images = images,
    isCustom = true,
    createdByUserId = uid,
    createdAt = createdAt,
    syncStatus = SyncStatus.SYNCED,
    updatedAtMs = updatedAtMs,
    deletedAtMs = deletedAtMs,
)
