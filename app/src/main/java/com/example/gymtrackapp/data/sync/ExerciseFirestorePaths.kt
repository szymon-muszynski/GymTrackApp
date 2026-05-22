package com.example.gymtrackapp.data.sync

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

internal object ExerciseFirestorePaths {
    fun customExercisesCol(db: FirebaseFirestore, uid: String): CollectionReference =
        db.collection("users").document(uid).collection("customExercises")
}

