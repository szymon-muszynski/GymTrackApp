package com.example.gymtrackapp.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackapp.data.sync.ExercisePullService
import com.example.gymtrackapp.data.sync.OrphanedPendingCleanup
import com.example.gymtrackapp.data.sync.TemplatePullService
import com.example.gymtrackapp.data.sync.TrainingPullService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class AuthViewModel(
    appContext: Context
) : ViewModel() {
    private val appContext: Context = appContext.applicationContext
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val trainingPullService = TrainingPullService(appContext.applicationContext)
    private val templatePullService = TemplatePullService(appContext.applicationContext)
    private val exercisePullService = ExercisePullService(appContext.applicationContext)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    init {
        _currentUser.value = auth.currentUser
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                Log.d(TAG, "signUp: start")

                val trimmedDisplayName = displayName.trim()
                if (trimmedDisplayName.isBlank()) {
                    _authState.value = AuthState.Error("Podaj nazwę użytkownika")
                    return@launch
                }

                val result = auth.createUserWithEmailAndPassword(email, password).await()
                _currentUser.value = result.user

                val uid = result.user?.uid
                Log.d(TAG, "signUp: firebase success uid=$uid")

                if (uid != null) {
                    // Utwórz profil usera w Firestore (wymagane dla Social)
                    val avatarColor = pickRandomAvatarColorHex(uid)
                    val profile = mapOf(
                        "displayName" to trimmedDisplayName,
                        "displayNameLower" to trimmedDisplayName.lowercase(Locale.ROOT),
                        "avatarColor" to avatarColor,
                        "createdAtMs" to System.currentTimeMillis(),
                    )
                    firestore.collection("users")
                        .document(uid)
                        .set(profile, SetOptions.merge())
                        .await()

                    withContext(Dispatchers.IO) {
                        // 1) Custom exercises first (FK prerequisite for session_exercises.exerciseId)
                        Log.d(TAG, "signUp: pull customExercises START")
                        exercisePullService.pullAllCustomForUser(uid)
                        Log.d(TAG, "signUp: pull customExercises DONE")

                        // 2) Trainings
                        Log.d(TAG, "signUp: pull training START")
                        trainingPullService.pullAllForUser(uid)
                        Log.d(TAG, "signUp: pull training DONE")

                        // 3) Templates
                        Log.d(TAG, "signUp: pull template START")
                        templatePullService.pullAllForUser(uid)
                        Log.d(TAG, "signUp: pull template DONE")

                        // 4) Cleanup orphaned pending (best-effort)
                        OrphanedPendingCleanup.cleanup(appContext.applicationContext)
                    }
                }

                _authState.value = AuthState.Success
                Log.d(TAG, "signUp: success")
            } catch (t: Throwable) {
                Log.e(TAG, "signUp: failed", t)
                _authState.value = AuthState.Error(t.message ?: "Błąd rejestracji")
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                Log.d(TAG, "signIn: start")

                val result = auth.signInWithEmailAndPassword(email, password).await()
                _currentUser.value = result.user

                val uid = result.user?.uid
                Log.d(TAG, "signIn: firebase success uid=$uid")

                if (uid != null) {
                    withContext(Dispatchers.IO) {
                        // 1) Custom exercises first (FK prerequisite for session_exercises.exerciseId)
                        Log.d(TAG, "signIn: pull customExercises START")
                        exercisePullService.pullAllCustomForUser(uid)
                        Log.d(TAG, "signIn: pull customExercises DONE")

                        // 2) Trainings
                        Log.d(TAG, "signIn: pull training START")
                        trainingPullService.pullAllForUser(uid)
                        Log.d(TAG, "signIn: pull training DONE")

                        // 3) Templates
                        Log.d(TAG, "signIn: pull template START")
                        templatePullService.pullAllForUser(uid)
                        Log.d(TAG, "signIn: pull template DONE")

                        // 4) Cleanup orphaned pending (best-effort)
                        OrphanedPendingCleanup.cleanup(appContext.applicationContext)
                    }
                }

                _authState.value = AuthState.Success
                Log.d(TAG, "signIn: success")
            } catch (t: Throwable) {
                Log.e(TAG, "signIn: failed", t)
                _authState.value = AuthState.Error(t.message ?: "Błąd logowania")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            Log.d(TAG, "signOut: start")

            withContext(Dispatchers.IO) {
                try {
                    trainingPullService.wipeLocalTrainingData()
                    Log.d(TAG, "signOut: wipe training OK")
                } catch (t: Throwable) {
                    Log.e(TAG, "signOut: wipe training FAILED", t)
                }
                try {
                    templatePullService.wipeLocalTemplateData()
                    Log.d(TAG, "signOut: wipe template OK")
                } catch (t: Throwable) {
                    Log.e(TAG, "signOut: wipe template FAILED", t)
                }
                try {
                    exercisePullService.wipeLocalCustomExercises()
                    Log.d(TAG, "signOut: wipe customExercises OK")
                } catch (t: Throwable) {
                    Log.e(TAG, "signOut: wipe customExercises FAILED", t)
                }
            }

            auth.signOut()
            _currentUser.value = null
            _authState.value = AuthState.Idle
            Log.d(TAG, "signOut: done")
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    private fun pickRandomAvatarColorHex(seed: String): String {
        // Mała paleta "material-ish". Deterministycznie po seed, żeby testowo nie skakało.
        val palette = listOf(
            "#4CAF50", // green
            "#2196F3", // blue
            "#9C27B0", // purple
            "#FF9800", // orange
            "#F44336", // red
            "#009688", // teal
            "#3F51B5", // indigo
            "#795548", // brown
        )
        val idx = kotlin.math.abs(seed.hashCode()) % palette.size
        return palette[idx]
    }

    private companion object {
        private const val TAG = "AuthViewModel"
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
