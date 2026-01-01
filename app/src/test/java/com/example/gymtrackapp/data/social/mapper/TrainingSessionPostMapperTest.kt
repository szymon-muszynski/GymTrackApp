package com.example.gymtrackapp.data.social.mapper

import com.example.gymtrackapp.data.entity.SessionExercise
import com.example.gymtrackapp.data.entity.SessionSetDetails
import com.example.gymtrackapp.data.entity.TrainingSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TrainingSessionPostMapperTest {

    @Test
    fun `toPostDoc builds idempotent postId and copies exercise names`() {
        val session = TrainingSession(id = 42L, date = 123L, description = "Push Day")
        val ex1 = SessionExercise(id = 1L, trainingSessionId = 42L, exerciseId = "bench_press", order = 0)
        val ex2 = SessionExercise(id = 2L, trainingSessionId = 42L, exerciseId = "custom_123", order = 1)

        val setsBy = mapOf(
            1L to listOf(
                SessionSetDetails(id = 10L, sessionExerciseId = 1L, order = 0, reps = 5, weight = 100f),
                SessionSetDetails(id = 11L, sessionExerciseId = 1L, order = 1, reps = 5, weight = 100f),
            ),
            2L to listOf(
                SessionSetDetails(id = 20L, sessionExerciseId = 2L, order = 0, reps = 8, weight = 30f),
            )
        )

        val doc = TrainingSessionPostMapper.toPostDoc(
            session = session,
            sessionExercises = listOf(ex1, ex2),
            setsBySessionExerciseId = setsBy,
            authorId = "uid123",
            authorDisplayName = "Jan",
            authorAvatarColor = "#FF0000",
            exerciseNamesById = mapOf(
                "bench_press" to "Bench Press",
                "custom_123" to "My Custom"
            ),
            nowMs = 999L
        )

        assertEquals("uid123_42", doc.postId)
        assertEquals("Push Day", doc.title)
        assertEquals(999L, doc.createdAtMs)
        assertEquals(2, doc.exercises.size)
        assertEquals("Bench Press", doc.exercises[0].name)
        assertEquals("My Custom", doc.exercises[1].name)
        assertNotNull(doc.totalVolume)
        assertEquals(5*100f + 5*100f + 8*30f, doc.totalVolume!!, 0.0001f)
    }
}

