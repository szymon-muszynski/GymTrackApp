package com.example.gymtrackapp.data.social.local

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "social_following",
    primaryKeys = ["myId", "otherUserId"],
    indices = [Index("myId"), Index("otherUserId")]
)
data class FollowingEntity(
    val myId: String,
    val otherUserId: String,
    val createdAtMs: Long,
)

