package com.example.gymtrackapp.data.social.mapper

import com.example.gymtrackapp.data.social.local.PostEntity
import com.example.gymtrackapp.data.social.local.UserCacheEntity
import com.example.gymtrackapp.data.social.model.ExerciseSummary
import com.example.gymtrackapp.data.social.model.Post
import com.example.gymtrackapp.data.social.model.SetSummary
import com.example.gymtrackapp.data.social.model.UserProfile
import com.example.gymtrackapp.data.social.remote.ExerciseDoc
import com.example.gymtrackapp.data.social.remote.PostDoc
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val gson = Gson()

fun PostDoc.toEntity(): PostEntity = PostEntity(
    postId = postId,
    authorId = authorId,
    authorDisplayName = authorDisplayName,
    authorAvatarColor = authorAvatarColor,
    originalSessionId = originalSessionId,
    title = title,
    createdAtMs = createdAtMs,
    exercisesJson = gson.toJson(exercises),
    totalVolume = totalVolume,
)

fun PostEntity.toDomain(): Post {
    val type = object : TypeToken<List<ExerciseDoc>>() {}.type
    val exercisesRemote: List<ExerciseDoc> = try {
        @Suppress("UNCHECKED_CAST")
        (gson.fromJson<List<ExerciseDoc>>(exercisesJson, type) ?: emptyList())
    } catch (_: Throwable) {
        emptyList()
    }

    val exercises = exercisesRemote.map { ex ->
        ExerciseSummary(
            name = ex.name,
            sets = ex.sets.map { s -> SetSummary(reps = s.reps, weight = s.weight) }
        )
    }

    return Post(
        postId = postId,
        authorId = authorId,
        authorDisplayName = authorDisplayName,
        authorAvatarColor = authorAvatarColor,
        title = title,
        createdAtMs = createdAtMs,
        originalSessionId = originalSessionId,
        exercises = exercises,
        totalVolume = totalVolume,
    )
}

fun UserCacheEntity.toDomain(): UserProfile = UserProfile(
    userId = userId,
    displayName = displayName,
    avatarColor = avatarColor,
)
