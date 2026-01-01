package com.example.gymtrackapp.data.social.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialDao {

    // ---- posts cache ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPosts(posts: List<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPost(post: PostEntity)

    @Query("SELECT * FROM social_posts ORDER BY createdAtMs DESC")
    fun observeAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM social_posts WHERE authorId = :authorId ORDER BY createdAtMs DESC")
    fun observePostsByAuthor(authorId: String): Flow<List<PostEntity>>

    @Query("DELETE FROM social_posts")
    suspend fun clearPosts()

    // ---- following cache ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFollowing(entity: FollowingEntity)

    @Query("DELETE FROM social_following WHERE myId = :myId AND otherUserId = :otherUserId")
    suspend fun deleteFollowing(myId: String, otherUserId: String)

    @Query("SELECT otherUserId FROM social_following WHERE myId = :myId")
    suspend fun getFollowingIds(myId: String): List<String>

    @Query("SELECT otherUserId FROM social_following WHERE myId = :myId")
    fun observeFollowingIds(myId: String): Flow<List<String>>

    // ---- users cache ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUsers(users: List<UserCacheEntity>)

    @Query("SELECT * FROM social_users WHERE displayName LIKE '%' || :query || '%' ORDER BY displayName COLLATE NOCASE ASC")
    fun observeUsersByQuery(query: String): Flow<List<UserCacheEntity>>

    @Query("SELECT * FROM social_users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserCacheEntity?
}

