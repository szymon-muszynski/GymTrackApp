package com.example.gymtrackapp.data.social.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialDao {

    // ---- posts cache ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPosts(posts: List<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPost(post: PostEntity)

    /**
     * Explore feed (SSOT): pokazuj tylko posty autorów, których aktualnie obserwuję.
     * To zapobiega wyciekom cache między kontami i powoduje natychmiastowe zniknięcie postów po Unfollow.
     */
    @Query(
        """
        SELECT p.*
        FROM social_posts p
        INNER JOIN social_following f
            ON f.otherUserId = p.authorId
        WHERE f.myId = :myId
        ORDER BY p.createdAtMs DESC
        """
    )
    fun observeExploreFeed(myId: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM social_posts WHERE authorId = :authorId ORDER BY createdAtMs DESC")
    fun observePostsByAuthor(authorId: String): Flow<List<PostEntity>>

    @Query("DELETE FROM social_posts")
    suspend fun clearPosts()

    // ---- following cache ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFollowing(entity: FollowingEntity)

    @Query("DELETE FROM social_following WHERE myId = :myId")
    suspend fun clearFollowingForUser(myId: String)

    @Transaction
    suspend fun replaceFollowingForUser(myId: String, newEntities: List<FollowingEntity>) {
        clearFollowingForUser(myId)
        if (newEntities.isNotEmpty()) {
            // Reuse upsertFollowing (REPLACE) in a loop, bo mamy tylko insert pojedynczy.
            // Celowo nie dodaję nowego bulk insertu, żeby zmiany były minimalne.
            newEntities.forEach { upsertFollowing(it) }
        }
    }

    @Query("DELETE FROM social_following WHERE myId = :myId AND otherUserId = :otherUserId")
    suspend fun deleteFollowing(myId: String, otherUserId: String)

    @Query("DELETE FROM social_following")
    suspend fun clearFollowing()

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

    @Query("DELETE FROM social_users")
    suspend fun clearUsers()
}
