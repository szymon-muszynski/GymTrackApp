package com.example.gymtrackapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackapp.data.sync.TemplatePullService
import com.example.gymtrackapp.data.sync.TrainingPullService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    appContext: Context
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val trainingPullService = TrainingPullService(appContext.applicationContext)
    private val templatePullService = TemplatePullService(appContext.applicationContext)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    init {
        _currentUser.value = auth.currentUser
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                _currentUser.value = result.user

                val uid = result.user?.uid
                if (uid != null) {
                    trainingPullService.pullAllForUser(uid)
                    templatePullService.pullAllForUser(uid)
                }

                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Błąd rejestracji")
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.signInWithEmailAndPassword(email, password).await()
                _currentUser.value = result.user

                val uid = result.user?.uid
                if (uid != null) {
                    trainingPullService.pullAllForUser(uid)
                    templatePullService.pullAllForUser(uid)
                }

                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Błąd logowania")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                trainingPullService.wipeLocalTrainingData()
            } catch (_: Throwable) {
                // ignore
            }
            try {
                templatePullService.wipeLocalTemplateData()
            } catch (_: Throwable) {
                // ignore
            }

            auth.signOut()
            _currentUser.value = null
            _authState.value = AuthState.Idle
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
