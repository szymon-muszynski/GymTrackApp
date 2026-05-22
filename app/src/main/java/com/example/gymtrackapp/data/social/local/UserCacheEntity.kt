package com.example.gymtrackapp.data.social.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "social_users")
data class UserCacheEntity(
    @PrimaryKey val userId: String,
    val displayName: String,
    val avatarColor: String,
    val updatedAtMs: Long,
)

