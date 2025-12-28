package com.example.gymtrackapp.data.sync

import android.content.Context
import android.util.Log
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
        val startedAt = System.currentTimeMillis()
        Log.d(TAG, "pullAllForUser: START uid=$uid")

        var templatesInserted = 0
        var templateExercisesInserted = 0

        try {
            wipeLocalTemplateData()
            Log.d(TAG, "pullAllForUser: wipeLocalTemplateData DONE")
        } catch (t: Throwable) {
            Log.e(TAG, "pullAllForUser: wipeLocalTemplateData FAILED", t)
            throw t
        }

        val templatesSnap = try {
            TemplateFirestorePaths.templatesCol(firestore, uid)
                .get()
                .await()
        } catch (t: Throwable) {
            Log.e(TAG, "pullAllForUser: FAILED at templates fetch", t)
            throw t
        }

        Log.d(TAG, "pullAllForUser: templates fetched count=${templatesSnap.size()}")

        for ((index, templateDoc) in templatesSnap.documents.withIndex()) {
            val templateRemoteId = templateDoc.id
            val template = templateDoc.toObject(WorkoutTemplateDoc::class.java)

            if (template == null) {
                Log.w(TAG, "pullAllForUser: templateDoc.toObject == null (remoteId=$templateRemoteId)")
                continue
            }

            if (index % 10 == 0) {
                Log.d(TAG, "pullAllForUser: processing template ${index + 1}/${templatesSnap.size()} remoteId=$templateRemoteId name=${template.name} deletedAtMs=${template.deletedAtMs}")
            }

            val localTemplateId = try {
                templateDao.insertTemplate(
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
            } catch (t: Throwable) {
                Log.e(TAG, "pullAllForUser: FAILED inserting template remoteId=$templateRemoteId", t)
                throw t
            }
            templatesInserted++

            val exercisesSnap = try {
                TemplateFirestorePaths.exercisesCol(firestore, uid, templateRemoteId)
                    .get()
                    .await()
            } catch (t: Throwable) {
                Log.e(TAG, "pullAllForUser: FAILED at template exercises fetch templateRemoteId=$templateRemoteId", t)
                throw t
            }

            Log.d(TAG, "pullAllForUser: template remoteId=$templateRemoteId exercises fetched count=${exercisesSnap.size()}")

            for (exerciseDoc in exercisesSnap.documents) {
                val exerciseRemoteId = exerciseDoc.id
                val exercise = exerciseDoc.toObject(TemplateExerciseDoc::class.java)

                if (exercise == null) {
                    Log.w(TAG, "pullAllForUser: TemplateExerciseDoc null (templateRemoteId=$templateRemoteId exerciseRemoteId=$exerciseRemoteId)")
                    continue
                }

                try {
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
                } catch (t: Throwable) {
                    Log.e(
                        TAG,
                        "pullAllForUser: FAILED inserting templateExercise templateRemoteId=$templateRemoteId exerciseRemoteId=$exerciseRemoteId",
                        t
                    )
                    throw t
                }
                templateExercisesInserted++
            }
        }

        val elapsedMs = System.currentTimeMillis() - startedAt
        Log.d(
            TAG,
            "pullAllForUser: DONE templatesInserted=$templatesInserted templateExercisesInserted=$templateExercisesInserted elapsedMs=$elapsedMs"
        )
    }

    private companion object {
        private const val TAG = "TemplatePullService"
    }
}
