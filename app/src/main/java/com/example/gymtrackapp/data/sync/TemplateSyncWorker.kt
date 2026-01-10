package com.example.gymtrackapp.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gymtrackapp.data.ExerciseDatabase
import com.example.gymtrackapp.data.dao.TemplateDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * MVP worker: wypycha lokalne zmiany templatek (pending) do Firestore.
 */
class TemplateSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result {
        val user = auth.currentUser ?: return Result.success()
        val uid = user.uid

        val templateDao: TemplateDao = ExerciseDatabase
            .getDatabase(applicationContext)
            .templateDao()

        return try {
            // 1) Templates (parent)
            val pendingTemplates = templateDao.getPendingTemplates()
            for (template in pendingTemplates) {
                val ref = TemplateFirestorePaths.templatesCol(db, uid).document(template.remoteId)
                ref.set(template.toDoc(uid), SetOptions.merge()).await()
                templateDao.markTemplateSynced(template.id)
            }

            // 2) Template exercises (child)
            val pendingExercises = templateDao.getPendingTemplateExercises()
            for (te in pendingExercises) {
                val templateRemoteId = templateDao.getTemplateRemoteId(te.templateId)
                if (templateRemoteId.isNullOrBlank()) {
                    Log.w(
                        TAG,
                        "doWork: SKIP TemplateExercise id=${te.id} remoteId=${te.remoteId} because parent templateId=${te.templateId} has no remoteId"
                    )
                    continue
                }

                val ref = TemplateFirestorePaths.exercisesCol(db, uid, templateRemoteId)
                    .document(te.remoteId)

                ref.set(te.toDoc(uid, templateRemoteId), SetOptions.merge()).await()
                templateDao.markTemplateExerciseSynced(te.id)
            }

            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "doWork: FAILED", t)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "TemplateSyncWorker"
    }
}
