package com.example.gymtrackapp.data.social.repository

import com.example.gymtrackapp.data.social.model.Post
import com.example.gymtrackapp.data.social.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface SocialRepository {

    /** Publikuje post dla lokalnej sesji (idempotentnie postId = authorId_sessionId). */
    suspend fun publishPost(sessionId: Long)

    /** SSOT: obserwacja feedu Explore z Room. */
    fun observeExploreFeed(): Flow<List<Post>>

    /** Manual refresh Explore (minimizujemy odczyty Firestore). */
    suspend fun refreshExploreFeed(limit: Long = 20)

    /** SSOT: posty konkretnego usera z Room. */
    fun observeUserPosts(userId: String): Flow<List<Post>>

    /** Manual refresh postów usera (profil). */
    suspend fun refreshUserPosts(userId: String, limit: Long = 20)

    /**
     * Zdalne wyszukiwanie użytkowników po prefixie displayName.
     * Np. wpis "Adam" -> displayName zaczyna się od "Adam".
     */
    suspend fun searchUsersByDisplayNamePrefix(prefix: String, limit: Long = 20): List<UserProfile>

    /** Follow: zapis do Firestore + aktualizacja Room (social_following). */
    suspend fun follow(userId: String)

    /** Unfollow: delete w Firestore + aktualizacja Room (social_following). */
    suspend fun unfollow(userId: String)

    /** Sync: pobiera z Firestore listę following i zapisuje do Room. */
    suspend fun syncFollowing()

    /** Reaktywna lista followingIds z Room (SSOT). */
    fun observeFollowingIds(): Flow<List<String>>
}
