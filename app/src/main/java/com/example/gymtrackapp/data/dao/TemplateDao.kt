package com.example.gymtrackapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gymtrackapp.data.entity.TemplateExercise
import com.example.gymtrackapp.data.entity.WorkoutTemplate

@Dao
interface TemplateDao {
    // ===== UI QUERIES (soft delete filtered) =====

    @Query("SELECT * FROM workout_templates WHERE deletedAtMs IS NULL ORDER BY createdAt DESC")
    suspend fun getAllTemplates(): List<WorkoutTemplate>

    @Query("SELECT * FROM template_exercises WHERE templateId = :templateId AND deletedAtMs IS NULL ORDER BY `order` ASC")
    suspend fun getExercisesForTemplate(templateId: Long): List<TemplateExercise>

    @Query("SELECT MAX(`order`) FROM template_exercises WHERE templateId = :templateId AND deletedAtMs IS NULL")
    suspend fun getMaxOrderForTemplate(templateId: Long): Int?

    // ===== BASIC MUTATIONS =====

    @Insert
    suspend fun insertTemplate(template: WorkoutTemplate): Long

    @Update
    suspend fun updateTemplate(template: WorkoutTemplate)

    @Insert
    suspend fun insertTemplateExercise(templateExercise: TemplateExercise): Long

    @Update
    suspend fun updateTemplateExercise(templateExercise: TemplateExercise)

    // ===== SOFT DELETE (kaskadowo) =====

    @Query("UPDATE workout_templates SET deletedAtMs = :deletedAtMs, updatedAtMs = :updatedAtMs, syncStatus = 2 WHERE id = :templateId")
    suspend fun softDeleteTemplate(templateId: Long, deletedAtMs: Long, updatedAtMs: Long)

    @Query("UPDATE template_exercises SET deletedAtMs = :deletedAtMs, updatedAtMs = :updatedAtMs, syncStatus = 2 WHERE templateId = :templateId")
    suspend fun softDeleteExercisesForTemplate(templateId: Long, deletedAtMs: Long, updatedAtMs: Long)

    @Query("UPDATE template_exercises SET deletedAtMs = :deletedAtMs, updatedAtMs = :updatedAtMs, syncStatus = 2 WHERE id = :templateExerciseId")
    suspend fun softDeleteTemplateExercise(templateExerciseId: Long, deletedAtMs: Long, updatedAtMs: Long)

    @Query("UPDATE template_exercises SET `order` = `order` - 1 WHERE templateId = :templateId AND deletedAtMs IS NULL AND `order` > :deletedOrder")
    suspend fun reorderAfterDeletion(templateId: Long, deletedOrder: Int)

    // ===== SYNC QUERIES (pending queue) =====

    @Query("SELECT * FROM workout_templates WHERE syncStatus != 0 ORDER BY updatedAtMs ASC LIMIT :limit")
    suspend fun getPendingTemplates(limit: Int = 100): List<WorkoutTemplate>

    @Query("SELECT * FROM template_exercises WHERE syncStatus != 0 ORDER BY updatedAtMs ASC LIMIT :limit")
    suspend fun getPendingTemplateExercises(limit: Int = 300): List<TemplateExercise>

    @Query("UPDATE workout_templates SET syncStatus = 0 WHERE id = :templateId")
    suspend fun markTemplateSynced(templateId: Long)

    @Query("UPDATE template_exercises SET syncStatus = 0 WHERE id = :templateExerciseId")
    suspend fun markTemplateExerciseSynced(templateExerciseId: Long)

    // ===== HELPERS FOR WORKER (parent -> remoteId) =====

    @Query("SELECT remoteId FROM workout_templates WHERE id = :templateId")
    suspend fun getTemplateRemoteId(templateId: Long): String?

    // ===== HELPERS FOR PULL/WIPE =====

    @Query("DELETE FROM template_exercises")
    suspend fun clearTemplateExercises()

    @Query("DELETE FROM workout_templates")
    suspend fun clearTemplates()

    // ===== ORPHANED PENDING CLEANUP =====

    /**
     * Usuwa pending TemplateExercise, które wskazują na nieistniejącą templatkę.
     * Bezpieczne, bo dotyka tylko syncStatus!=SYNCED.
     */
    @Query(
        """
        DELETE FROM template_exercises
        WHERE syncStatus != 0
          AND templateId NOT IN (SELECT id FROM workout_templates)
        """
    )
    suspend fun deleteOrphanedPendingTemplateExercises(): Int

    // ===== HARD DELETE (retencja) =====

    /** Hard-delete (retencja): usuwa stare soft-deleted ćwiczenia w templatekach, tylko jeśli SYNCED. */
    @Query(
        """
        DELETE FROM template_exercises
        WHERE deletedAtMs IS NOT NULL
          AND deletedAtMs < :cutoffMs
          AND syncStatus = ${com.example.gymtrackapp.data.entity.SyncStatus.SYNCED}
        """
    )
    suspend fun purgeDeletedTemplateExercises(cutoffMs: Long): Int

    /** Hard-delete (retencja): usuwa stare soft-deleted templateki, tylko jeśli SYNCED. */
    @Query(
        """
        DELETE FROM workout_templates
        WHERE deletedAtMs IS NOT NULL
          AND deletedAtMs < :cutoffMs
          AND syncStatus = ${com.example.gymtrackapp.data.entity.SyncStatus.SYNCED}
        """
    )
    suspend fun purgeDeletedWorkoutTemplates(cutoffMs: Long): Int
}