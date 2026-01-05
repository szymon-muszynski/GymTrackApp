package com.example.gymtrackapp.data.repository

import android.util.Log
import com.example.gymtrackapp.data.dao.PlannedWorkoutDao
import com.example.gymtrackapp.data.entity.PlannedWorkoutEntity
import com.example.gymtrackapp.data.entity.SyncStatus
import com.example.gymtrackapp.notifications.NotificationScheduler
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.UUID

class PlanningRepository(
    private val plannedWorkoutDao: PlannedWorkoutDao,
    private val scheduler: NotificationScheduler,
    private val firestore: FirebaseFirestore,
) {

    /**
     * Stream planów z Room dla zakresu dni (inclusive).
     *
     * UI (Weekly Planner) może się na to podpiąć, a zmiany po add/delete odświeżą się automatycznie.
     */
    fun plansBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<PlannedWorkoutEntity>> {
        return plannedWorkoutDao.getPlansBetween(startEpochDay, endEpochDay)
    }

    /**
     * Pull: pobiera users/{uid}/planned_workouts, upsertuje lokalnie, potem odświeża alarmy.
     *
     * MVP: nie robimy merge; Firestore jest źródłem prawdy przy loginie.
     */
    suspend fun pull(uid: String) = withContext(Dispatchers.IO) {
        Log.d(TAG, "pull: START uid=$uid")

        val snapshot = firestore
            .collection("users")
            .document(uid)
            .collection(COLLECTION_PLANNED_WORKOUTS)
            .get()
            .await()

        val entities = snapshot.documents.mapNotNull { doc ->
            val map = doc.data ?: emptyMap<String, Any?>()

            val dateEpochDay = (map[Fields.DATE_EPOCH_DAY] as? Number)?.toLong()
            val title = map[Fields.TITLE] as? String
            val updatedAtMs = (map[Fields.UPDATED_AT_MS] as? Number)?.toLong()
            val deletedAtMs = (map[Fields.DELETED_AT_MS] as? Number)?.toLong()

            // Minimalna walidacja
            if (dateEpochDay == null || title == null || updatedAtMs == null) {
                Log.w(TAG, "pull: skip docId=${doc.id} because missing fields")
                return@mapNotNull null
            }

            PlannedWorkoutEntity(
                id = doc.id,
                dateEpochDay = dateEpochDay,
                title = title,
                updatedAtMs = updatedAtMs,
                syncStatus = SyncStatus.SYNCED,
                deletedAtMs = deletedAtMs,
            )
        }

        plannedWorkoutDao.upsertAll(entities)

        Log.d(TAG, "pull: upserted=${entities.size}")
        refreshAlarms()
        Log.d(TAG, "pull: DONE uid=$uid")
    }

    /**
     * INSERT/UPDATE planu.
     *
     * "Explicit ID":
     * - id != null  -> UPDATE (nadpisujemy istniejący dokument/encję o tym id)
     * - id == null  -> INSERT (generujemy nowe UUID)
     */
    suspend fun addPlan(
        dateEpochDay: Long,
        title: String,
        id: String? = null,
        uid: String? = null,
    ): PlannedWorkoutEntity = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val planId = id ?: UUID.randomUUID().toString()

        val entity = PlannedWorkoutEntity(
            id = planId,
            dateEpochDay = dateEpochDay,
            title = title,
            updatedAtMs = now,
            syncStatus = SyncStatus.PENDING_UPSERT,
            deletedAtMs = null,
        )

        plannedWorkoutDao.upsert(entity)

        // Scheduler jest idempotentny (requestCode = id.hashCode())
        scheduler.schedule(entity)

        // Best-effort push
        if (uid != null) {
            runCatching { upsertRemote(uid, entity) }
                .onFailure { Log.w(TAG, "addPlan: remote upsert failed", it) }
        }

        entity
    }

    suspend fun deletePlan(planId: String, uid: String? = null) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()

        val all = plannedWorkoutDao.getAllWorkouts()
        val existing = all.firstOrNull { it.id == planId }

        if (existing == null) {
            Log.w(TAG, "deletePlan: planId=$planId not found locally")
            return@withContext
        }

        val deleted = existing.copy(
            deletedAtMs = now,
            updatedAtMs = now,
            syncStatus = SyncStatus.PENDING_DELETE
        )

        plannedWorkoutDao.upsert(deleted)
        scheduler.cancel(deleted)

        if (uid != null) {
            runCatching { upsertRemote(uid, deleted) }
                .onFailure { Log.w(TAG, "deletePlan: remote upsert failed", it) }
        }
    }

    /**
     * Clean-up przy wylogowaniu.
     */
    suspend fun clearLocalData() = withContext(Dispatchers.IO) {
        val nowEpochDay = LocalDate.now().toEpochDay()
        val future = plannedWorkoutDao.getFutureWorkouts(nowEpochDay)
        future.forEach { scheduler.cancel(it) }
        plannedWorkoutDao.clearAll()
    }

    private suspend fun refreshAlarms() {
        val nowEpochDay = LocalDate.now().toEpochDay()
        val future = plannedWorkoutDao.getFutureWorkouts(nowEpochDay)
        future.forEach { scheduler.schedule(it) }
    }

    private suspend fun upsertRemote(uid: String, entity: PlannedWorkoutEntity) {
        val docRef = firestore
            .collection("users")
            .document(uid)
            .collection(COLLECTION_PLANNED_WORKOUTS)
            .document(entity.id)

        val payload = hashMapOf<String, Any?>(
            Fields.DATE_EPOCH_DAY to entity.dateEpochDay,
            Fields.TITLE to entity.title,
            Fields.UPDATED_AT_MS to entity.updatedAtMs,
            Fields.DELETED_AT_MS to entity.deletedAtMs,
        )

        docRef.set(payload).await()
    }

    companion object {
        private const val TAG = "PlanningRepository"
        private const val COLLECTION_PLANNED_WORKOUTS = "planned_workouts"

        private object Fields {
            const val DATE_EPOCH_DAY = "dateEpochDay"
            const val TITLE = "title"
            const val UPDATED_AT_MS = "updatedAtMs"
            const val DELETED_AT_MS = "deletedAtMs"
        }
    }
}
