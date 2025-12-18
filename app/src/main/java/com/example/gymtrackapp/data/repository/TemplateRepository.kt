package com.example.gymtrackapp.data.repository

import com.example.gymtrackapp.data.dao.TemplateDao
import com.example.gymtrackapp.data.entity.TemplateExercise
import com.example.gymtrackapp.data.entity.WorkoutTemplate

class TemplateRepository(private val templateDao: TemplateDao) {

    suspend fun getAllTemplates(): List<WorkoutTemplate> {
        return templateDao.getAllTemplates()
    }

    // description jest obecnie ignorowane, bo WorkoutTemplate nie ma tego pola
    suspend fun createTemplate(name: String, description: String?) {
        val template = WorkoutTemplate(
            name = name,
            createdAt = System.currentTimeMillis()
        )
        templateDao.insertTemplate(template)
    }

    suspend fun deleteTemplate(template: WorkoutTemplate) {
        templateDao.deleteTemplate(template)
    }

    suspend fun getExercisesForTemplate(templateId: Long): List<TemplateExercise> {
        return templateDao.getExercisesForTemplate(templateId)
    }

    suspend fun addExerciseToTemplate(templateId: Long, exerciseId: String) {
        // Możesz użyć getMaxOrderForTemplate, żeby nie ściągać całej listy
        val maxOrder = templateDao.getMaxOrderForTemplate(templateId) ?: -1
        val newOrder = maxOrder + 1

        val newExercise = TemplateExercise(
            templateId = templateId,
            exerciseId = exerciseId,
            order = newOrder
        )
        templateDao.insertTemplateExercise(newExercise)
    }

    suspend fun deleteTemplateExercise(exercise: TemplateExercise) {
        templateDao.removeExerciseFromTemplate(
            templateId = exercise.templateId,
            exerciseId = exercise.exerciseId
        )
        // Opcjonalnie: można tu później dodać porządkowanie order,
        // ale wymaga to dodania @Update w TemplateDao.
    }

    suspend fun renameTemplate(template: WorkoutTemplate, newName: String) {
        val updated = template.copy(name = newName)
        templateDao.updateTemplate(updated)
    }
}
