package com.example.gymtrackapp.data.repository

import android.content.Context
import com.example.gymtrackapp.data.dao.TemplateDao
import com.example.gymtrackapp.data.entity.TemplateExercise
import com.example.gymtrackapp.data.entity.WorkoutTemplate
import com.example.gymtrackapp.data.sync.TemplateSyncScheduler

class TemplateRepository(
    private val templateDao: TemplateDao,
    private val context: Context
) {

    suspend fun getAllTemplates(): List<WorkoutTemplate> = templateDao.getAllTemplates()

    suspend fun createTemplate(name: String, description: String?) {
        // description jest obecnie ignorowane, bo WorkoutTemplate nie ma tego pola
        templateDao.insertTemplate(
            WorkoutTemplate(
                name = name,
                createdAt = System.currentTimeMillis(),
                updatedAtMs = System.currentTimeMillis(),
                syncStatus = 1
            )
        )
        TemplateSyncScheduler.enqueue(context)
    }

    suspend fun deleteTemplate(template: WorkoutTemplate) {
        val now = System.currentTimeMillis()
        templateDao.softDeleteExercisesForTemplate(template.id, deletedAtMs = now, updatedAtMs = now)
        templateDao.softDeleteTemplate(template.id, deletedAtMs = now, updatedAtMs = now)
        TemplateSyncScheduler.enqueue(context)
    }

    suspend fun getExercisesForTemplate(templateId: Long): List<TemplateExercise> =
        templateDao.getExercisesForTemplate(templateId)

    suspend fun addExerciseToTemplate(templateId: Long, exerciseId: String) {
        val maxOrder = templateDao.getMaxOrderForTemplate(templateId) ?: -1
        val newExercise = TemplateExercise(
            templateId = templateId,
            exerciseId = exerciseId,
            order = maxOrder + 1,
            updatedAtMs = System.currentTimeMillis(),
            syncStatus = 1
        )
        templateDao.insertTemplateExercise(newExercise)
        TemplateSyncScheduler.enqueue(context)
    }

    suspend fun deleteTemplateExercise(exercise: TemplateExercise) {
        val now = System.currentTimeMillis()
        templateDao.softDeleteTemplateExercise(exercise.id, deletedAtMs = now, updatedAtMs = now)
        templateDao.reorderAfterDeletion(exercise.templateId, exercise.order)
        TemplateSyncScheduler.enqueue(context)
    }

    suspend fun renameTemplate(template: WorkoutTemplate, newName: String) {
        val updated = template.copy(
            name = newName,
            updatedAtMs = System.currentTimeMillis(),
            syncStatus = 1
        )
        templateDao.updateTemplate(updated)
        TemplateSyncScheduler.enqueue(context)
    }
}
