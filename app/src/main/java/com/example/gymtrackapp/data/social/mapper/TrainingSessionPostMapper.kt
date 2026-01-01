package com.example.gymtrackapp.data.social.mapper

import com.example.gymtrackapp.data.entity.SessionExercise
import com.example.gymtrackapp.data.entity.SessionSetDetails
import com.example.gymtrackapp.data.entity.TrainingSession
import com.example.gymtrackapp.data.social.remote.ExerciseDoc
import com.example.gymtrackapp.data.social.remote.PostDoc
import com.example.gymtrackapp.data.social.remote.SetDoc

object TrainingSessionPostMapper {

    /**
     * @param exerciseNamesById mapa (exerciseId -> name) z Room (w tym custom exercises).
     */
    fun toPostDoc(
        session: TrainingSession,
        sessionExercises: List<SessionExercise>,
        setsBySessionExerciseId: Map<Long, List<SessionSetDetails>>,
        authorId: String,
        authorDisplayName: String,
        authorAvatarColor: String,
        exerciseNamesById: Map<String, String>,
        nowMs: Long = System.currentTimeMillis(),
        schemaVersion: Int = 1,
    ): PostDoc {
        val postId = buildPostId(authorId = authorId, originalSessionId = session.id.toString())

        val exercisesDoc: List<ExerciseDoc> = sessionExercises
            .sortedBy { it.order }
            .map { se ->
                val name = exerciseNamesById[se.exerciseId] ?: se.exerciseId
                val sets: List<SetDoc> = setsBySessionExerciseId[se.id]
                    .orEmpty()
                    .sortedBy { it.order }
                    .map { s -> SetDoc(reps = s.reps, weight = s.weight) }

                ExerciseDoc(name = name, sets = sets)
            }

        val totalVolume: Float? = exercisesDoc
            .flatMap { it.sets }
            .sumOf { (it.reps * it.weight).toDouble() }
            .toFloat()
            .let { if (it == 0f) null else it }

        return PostDoc(
            postId = postId,
            authorId = authorId,
            authorDisplayName = authorDisplayName,
            authorAvatarColor = authorAvatarColor,
            originalSessionId = session.id.toString(),
            title = session.description,
            createdAtMs = nowMs,
            exercises = exercisesDoc,
            totalVolume = totalVolume,
            schemaVersion = schemaVersion,
        )
    }

    fun buildPostId(authorId: String, originalSessionId: String): String =
        "${authorId}_${originalSessionId}"
}

