package com.example.gymtrackapp.data.sync

import android.content.Context
import com.example.gymtrackapp.data.ExerciseDatabase
import com.example.gymtrackapp.data.dao.TemplateDao
import com.example.gymtrackapp.data.entity.TemplateExercise
import com.example.gymtrackapp.data.entity.WorkoutTemplate
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Pull templatek z Firestore do Room. Uruchamiane tylko przy loginie.
 * MVP: wipe + pełny pull.
 */
class TemplatePullService(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val templateDao: TemplateDao by lazy {
        ExerciseDatabase.getDatabase(context).templateDao()
    }

    suspend fun wipeLocalTemplateData() {
        templateDao.clearTemplateExercises()
        templateDao.clearTemplates()
    }

    suspend fun pullAllForUser(uid: String) {
        wipeLocalTemplateData()

        val templatesSnap = TemplateFirestorePaths.templatesCol(firestore, uid)
            .get()
            .await()

        for (templateDoc in templatesSnap.documents) {
            val templateRemoteId = templateDoc.id
            val template = templateDoc.toObject(WorkoutTemplateDoc::class.java) ?: continue

            val localTemplateId = templateDao.insertTemplate(
                WorkoutTemplate(
                    id = 0,
                    name = template.name,
                    createdAt = template.createdAt,
                    remoteId = templateRemoteId,
                    updatedAtMs = template.updatedAtMs,
                    syncStatus = 0,
                    deletedAtMs = template.deletedAtMs
                )
            )

            val exercisesSnap = TemplateFirestorePaths.exercisesCol(firestore, uid, templateRemoteId)
                .get()
                .await()

            for (exerciseDoc in exercisesSnap.documents) {
                val exerciseRemoteId = exerciseDoc.id
                val exercise = exerciseDoc.toObject(TemplateExerciseDoc::class.java) ?: continue

                templateDao.insertTemplateExercise(
                    TemplateExercise(
                        id = 0,
                        templateId = localTemplateId,
                        exerciseId = exercise.exerciseId,
                        order = exercise.order,
                        remoteId = exerciseRemoteId,
                        updatedAtMs = exercise.updatedAtMs,
                        syncStatus = 0,
                        deletedAtMs = exercise.deletedAtMs
                    )
                )
            }
        }
    }
}

