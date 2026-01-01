package com.example.gymtrackapp.data.social.remote

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserDoc(
    val displayName: String = "",
    val displayNameLower: String = "",
    /** HEX np. "#4CAF50" */
    val avatarColor: String = "#4CAF50",
    val createdAtMs: Long = 0L,
    val schemaVersion: Int = 1,
)
