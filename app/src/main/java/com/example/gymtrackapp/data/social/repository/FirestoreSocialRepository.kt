package com.example.gymtrackapp.data.social.repository

import com.example.gymtrackapp.data.dao.ExerciseDao
import com.example.gymtrackapp.data.dao.TrainingDao
import com.example.gymtrackapp.data.social.local.FollowingEntity
import com.example.gymtrackapp.data.social.local.SocialDao
import com.example.gymtrackapp.data.social.mapper.TrainingSessionPostMapper
import com.example.gymtrackapp.data.social.mapper.toDomain
import com.example.gymtrackapp.data.social.mapper.toEntity
import com.example.gymtrackapp.data.social.model.Post
import com.example.gymtrackapp.data.social.model.UserProfile
import com.example.gymtrackapp.data.social.remote.PostDoc
import com.example.gymtrackapp.data.social.remote.UserDoc
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Locale

class FirestoreSocialRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val trainingDao: TrainingDao,
    private val exerciseDao: ExerciseDao,
    private val socialDao: SocialDao,
) : SocialRepository {

    private fun requireUid(): String = requireNotNull(auth.currentUser?.uid) {
        "Brak zalogowanego użytkownika"
    }

    override fun observeFollowingIds(): Flow<List<String>> {
        val uid = requireUid()
        return socialDao.observeFollowingIds(uid)
    }

    override suspend fun syncFollowing() {
        val uid = requireUid()

        val snap = firestore.collection("users").document(uid)
            .collection("following")
            .get()
            .await()

        val now = System.currentTimeMillis()
        val entities: List<FollowingEntity> = snap.documents.map { doc ->
            val createdAt = doc.getLong("createdAtMs") ?: now
            FollowingEntity(myId = uid, otherUserId = doc.id, createdAtMs = createdAt)
        }

        // Remote jest źródłem prawdy dla following (MVP). Po wipe cache na logout musimy
        // odtworzyć pełny stan, a nie tylko "dopisać brakujące".
        // Czyścimy WYŁĄCZNIE rekordy tego użytkownika (bez globalnego wipe).
        socialDao.replaceFollowingForUser(myId = uid, newEntities = entities)
    }

    override suspend fun searchUsersByDisplayNamePrefix(prefix: String, limit: Long): List<UserProfile> {
        val uid = requireUid()
        val trimmed = prefix.trim()
        if (trimmed.length < 2) return emptyList()

        val normalized = trimmed.lowercase(Locale.ROOT)
        val end = normalized + "\uf8ff"

        val snap = firestore.collection("users")
            .orderBy("displayNameLower", Query.Direction.ASCENDING)
            .startAt(normalized)
            .endAt(end)
            .limit(limit)
            .get()
            .await()

        return snap.documents.mapNotNull { doc ->
            // pomijamy samego siebie w wynikach
            if (doc.id == uid) return@mapNotNull null

            val u = doc.toObject(UserDoc::class.java) ?: return@mapNotNull null
            UserProfile(
                userId = doc.id,
                displayName = u.displayName,
                avatarColor = u.avatarColor,
            )
        }
    }

    override suspend fun follow(userId: String) {
        // blokada follow samego siebie
        val uid = requireUid()
        if (userId == uid) return
        val now = System.currentTimeMillis()

        firestore.collection("users").document(uid)
            .collection("following").document(userId)
            .set(mapOf("createdAtMs" to now), SetOptions.merge())
            .await()

        socialDao.upsertFollowing(FollowingEntity(myId = uid, otherUserId = userId, createdAtMs = now))
    }

    override suspend fun unfollow(userId: String) {
        val uid = requireUid()
        if (userId == uid) return

        firestore.collection("users").document(uid)
            .collection("following").document(userId)
            .delete()
            .await()

        socialDao.deleteFollowing(uid, userId)
    }

    override suspend fun publishPost(sessionId: Long) {
        val uid = requireUid()

        // 1) Odczyt z Room
        val session = requireNotNull(trainingDao.getSessionById(sessionId)) {
            "Nie znaleziono sesji: $sessionId"
        }
        val sessionExercises = trainingDao.getExercisesForSession(sessionId)
        val setsByExerciseId = sessionExercises.associate { se ->
            se.id to trainingDao.getSetsForSessionExercise(se.id)
        }

        // 2) Nazwy ćwiczeń (w tym custom) — dociągamy lokalnie
        val exerciseNamesById = buildMap {
            for (se in sessionExercises) {
                val name = exerciseDao.getExerciseById(se.exerciseId)?.name
                if (name != null) put(se.exerciseId, name)
            }
        }

        // 3) Profil autora (users/{uid}) — 1 read, tylko przy publikacji
        val authorDocSnap = firestore.collection("users").document(uid).get().await()
        val authorDoc: UserDoc? = authorDocSnap.toObject(UserDoc::class.java)
        val authorDisplayName = authorDoc?.displayName
            ?.takeIf { it.isNotBlank() }
            ?: (auth.currentUser?.email ?: "User")
        val authorAvatarColor = authorDoc?.avatarColor?.takeIf { it.isNotBlank() } ?: "#4CAF50"

        // 4) Mapowanie do PostDoc
        val postDoc = TrainingSessionPostMapper.toPostDoc(
            session = session,
            sessionExercises = sessionExercises,
            setsBySessionExerciseId = setsByExerciseId,
            authorId = uid,
            authorDisplayName = authorDisplayName,
            authorAvatarColor = authorAvatarColor,
            exerciseNamesById = exerciseNamesById,
            nowMs = System.currentTimeMillis(),
        )

        // 5) Zapis idempotentny do posts/{postId} (bez deduplikacyjnego odczytu)
        firestore.collection("posts")
            .document(postDoc.postId)
            .set(postDoc, SetOptions.merge())
            .await()

        // 6) Update cache
        socialDao.upsertPost(postDoc.toEntity())

        // 7) Local UX flag: oznacz sesję jako już udostępnioną
        trainingDao.setSessionPosted(sessionId = sessionId, isPosted = true, updatedAtMs = System.currentTimeMillis())
    }

    override fun observeExploreFeed(): Flow<List<Post>> =
        socialDao.observeExploreFeed(requireUid()).map { list -> list.map { it.toDomain() } }

    override suspend fun refreshExploreFeed(limit: Long) {
        val uid = requireUid()
        val followingIds = socialDao.getFollowingIds(uid)
        if (followingIds.isEmpty()) return

        // Firestore whereIn ma limit 10 elementów -> batchujemy.
        val chunks: List<List<String>> = followingIds.chunked(10)
        val allDocs = mutableListOf<PostDoc>()

        for (chunk in chunks) {
            val query = firestore.collection("posts")
                .whereIn("authorId", chunk)
                .orderBy("createdAtMs", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit)

            val snap = query.get().await()
            val docs: List<PostDoc> = snap.documents.mapNotNull { doc ->
                doc.toObject(PostDoc::class.java)?.copy(postId = doc.id)
            }
            allDocs += docs
        }

        // Merge: bierzemy najnowsze limit postów po createdAtMs.
        val merged = allDocs
            .distinctBy { it.postId }
            .sortedByDescending { it.createdAtMs }
            .take(limit.toInt())

        socialDao.upsertPosts(merged.map { it.toEntity() })
    }

    override fun observeUserPosts(userId: String): Flow<List<Post>> =
        socialDao.observePostsByAuthor(userId).map { list -> list.map { it.toDomain() } }

    override suspend fun refreshUserPosts(userId: String, limit: Long) {
        val query = firestore.collection("posts")
            .whereEqualTo("authorId", userId)
            .orderBy("createdAtMs", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit)

        val snap = query.get().await()
        val docs: List<PostDoc> = snap.documents.mapNotNull { doc ->
            doc.toObject(PostDoc::class.java)?.copy(postId = doc.id)
        }

        socialDao.upsertPosts(docs.map { it.toEntity() })
    }

    /** Pobiera profil usera do cache (displayName/avatarColor). */
    override suspend fun refreshUserProfile(userId: String) {
        val doc = firestore.collection("users").document(userId).get().await()
        val user: UserDoc = doc.toObject(UserDoc::class.java) ?: return
        socialDao.upsertUsers(
            listOf(
                com.example.gymtrackapp.data.social.local.UserCacheEntity(
                    userId = userId,
                    displayName = user.displayName,
                    avatarColor = user.avatarColor,
                    updatedAtMs = System.currentTimeMillis(),
                )
            )
        )
    }

    override fun observeUserProfile(userId: String): Flow<UserProfile?> =
        socialDao.observeUserById(userId).map { it?.toDomain() }

    override suspend fun deletePost(post: com.example.gymtrackapp.data.social.model.Post) {
        val uid = requireUid()
        require(post.authorId == uid) { "Nie możesz usunąć posta innego użytkownika" }

        // 1) Firestore
        firestore.collection("posts").document(post.postId).delete().await()

        // 2) Local cache: usuń post z Room
        socialDao.deletePostById(post.postId)

        // 3) Reset flagi isPosted dla sesji treningowej (aby można było udostępnić ponownie)
        val sessionId = post.originalSessionId.toLongOrNull()
        if (sessionId != null) {
            trainingDao.setSessionPosted(sessionId = sessionId, isPosted = false, updatedAtMs = System.currentTimeMillis())
        }
    }
}
