package com.example.gymtrackapp.data.social.remote

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Denormalizowany dokument posta w kolekcji top-level `posts`.
 * Trzymamy tu wszystko potrzebne do renderu karty bez dodatkowych dociągnięć.
 */
@IgnoreExtraProperties
data class PostDoc(
    val postId: String = "",
    val authorId: String = "",
    val authorDisplayName: String = "",
    val authorAvatarColor: String = "",
    val originalSessionId: String = "",
    val title: String = "",
    val createdAtMs: Long = 0L,
    val exercises: List<ExerciseDoc> = emptyList(),
    val totalVolume: Float? = null,
    val schemaVersion: Int = 1,
)

@IgnoreExtraProperties
data class ExerciseDoc(
    val name: String = "",
    val sets: List<SetDoc> = emptyList(),
)

@IgnoreExtraProperties
data class SetDoc(
    val reps: Int = 0,
    val weight: Float = 0f,
)

