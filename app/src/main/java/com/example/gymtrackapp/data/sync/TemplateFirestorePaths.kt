package com.example.gymtrackapp.data.sync

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

internal object TemplateFirestorePaths {
    fun templatesCol(db: FirebaseFirestore, uid: String): CollectionReference =
        db.collection("users").document(uid).collection("workoutTemplates")

    fun exercisesCol(db: FirebaseFirestore, uid: String, templateRemoteId: String): CollectionReference =
        templatesCol(db, uid).document(templateRemoteId).collection("exercises")
}

