package com.example.gymtrackapp.data.social.model

/** Model domenowy pod UI (z Room - cache). */
data class Post(
    val postId: String,
    val authorId: String,
    val authorDisplayName: String,
    val authorAvatarColor: String,
    val title: String,
    val createdAtMs: Long,
    val originalSessionId: String,
    val exercises: List<ExerciseSummary>,
    val totalVolume: Float?
)

data class ExerciseSummary(
    val name: String,
    val sets: List<SetSummary>
)

data class SetSummary(
    val reps: Int,
    val weight: Float
)

