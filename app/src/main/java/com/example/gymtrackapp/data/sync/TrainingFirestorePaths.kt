package com.example.gymtrackapp.data.sync

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

internal object TrainingFirestorePaths {
    fun sessionsCol(db: FirebaseFirestore, uid: String): CollectionReference =
        db.collection("users").document(uid).collection("trainingSessions")

    fun exercisesCol(db: FirebaseFirestore, uid: String, sessionRemoteId: String): CollectionReference =
        sessionsCol(db, uid).document(sessionRemoteId).collection("exercises")

    fun setsCol(
        db: FirebaseFirestore,
        uid: String,
        sessionRemoteId: String,
        sessionExerciseRemoteId: String
    ): CollectionReference =
        exercisesCol(db, uid, sessionRemoteId)
            .document(sessionExerciseRemoteId)
            .collection("sets")
}
