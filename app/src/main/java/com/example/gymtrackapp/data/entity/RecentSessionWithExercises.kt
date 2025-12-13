package com.example.gymtrackapp.data.entity

/**
 * Model reprezentujący ostatnią sesję treningową z listą ćwiczeń
 */
data class RecentSessionWithExercises(
    val sessionId: Long,
    val sessionDate: Long,
    val sessionDescription: String,
    val exerciseNames: List<String>
)

