package com.example.gymtrackapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gymtrackapp.data.entity.TemplateExercise
import com.example.gymtrackapp.data.entity.WorkoutTemplate

@Dao
interface TemplateDao {
    @Query("SELECT * FROM workout_templates ORDER BY createdAt DESC")
    suspend fun getAllTemplates(): List<WorkoutTemplate>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkoutTemplate): Long

    @Update
    suspend fun updateTemplate(template: WorkoutTemplate)

    @Delete
    suspend fun deleteTemplate(template: WorkoutTemplate)

    @Query("SELECT * FROM template_exercises WHERE templateId = :templateId ORDER BY `order` ASC")
    suspend fun getExercisesForTemplate(templateId: Long): List<TemplateExercise>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateExercise(templateExercise: TemplateExercise)

    @Query("DELETE FROM template_exercises WHERE templateId = :templateId AND exerciseId = :exerciseId")
    suspend fun removeExerciseFromTemplate(templateId: Long, exerciseId: String)

    @Query("SELECT MAX(`order`) FROM template_exercises WHERE templateId = :templateId")
    suspend fun getMaxOrderForTemplate(templateId: Long): Int?
}