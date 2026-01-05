package com.example.gymtrackapp.di

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.gymtrackapp.data.ExerciseDatabase
import com.example.gymtrackapp.data.repository.PlanningRepository
import com.example.gymtrackapp.data.social.repository.FirestoreSocialRepository
import com.example.gymtrackapp.notifications.NotificationScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

abstract class AppContainer {
    abstract val database: ExerciseDatabase
    abstract val socialRepository: FirestoreSocialRepository
    abstract val planningRepository: PlanningRepository
}

class GymTrackAppContainer(
    context: Context,
) : AppContainer() {
    // Zawsze trzymaj DB na applicationContext, żeby nie wyciekać Activity.
    override val database: ExerciseDatabase = ExerciseDatabase.getDatabase(context.applicationContext)

    override val socialRepository: FirestoreSocialRepository = FirestoreSocialRepository(
        auth = FirebaseAuth.getInstance(),
        firestore = FirebaseFirestore.getInstance(),
        trainingDao = database.trainingDao(),
        exerciseDao = database.exerciseDao(),
        socialDao = database.socialDao(),
    )

    override val planningRepository: PlanningRepository = PlanningRepository(
        plannedWorkoutDao = database.plannedWorkoutDao(),
        scheduler = NotificationScheduler(context.applicationContext),
        firestore = FirebaseFirestore.getInstance(),
    )
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("Brak AppContainer. Upewnij się, że MainActivity owija UI w CompositionLocalProvider.")
}
