package com.example.gymtrackapp.data.social.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "social_posts",
    indices = [
        Index(value = ["authorId"]),
        Index(value = ["createdAtMs"]),
        Index(value = ["authorId", "createdAtMs"])
    ]
)
data class PostEntity(
    @PrimaryKey val postId: String,
    val authorId: String,
    val authorDisplayName: String,
    val authorAvatarColor: String,
    val originalSessionId: String,
    val title: String,
    val createdAtMs: Long,
    /** JSON listy ćwiczeń + setów (denormalizacja w Room). */
    val exercisesJson: String,
    val totalVolume: Float?,
)

