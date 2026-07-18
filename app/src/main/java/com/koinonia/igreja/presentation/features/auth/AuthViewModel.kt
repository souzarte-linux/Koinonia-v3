package com.koinonia.igreja.presentation.features.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koinonia.igreja.data.repository.AuthRepositoryImpl
import com.koinonia.igreja.domain.model.AppRole
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val role: AppRole) : AuthState()
    data class VerificationSent(val email: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepositoryImpl,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    val currentUserRole = authRepository.currentUserRole

    private val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    val savedEmail = MutableStateFlow(sharedPreferences.getString("saved_email", "") ?: "")
    val rememberEmail = MutableStateFlow(sharedPreferences.getBoolean("remember_email", false))

    fun saveRememberedEmail(email: String, remember: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean("remember_email", remember)
            if (remember) {
                putString("saved_email", email)
            } else {
                putString("saved_email", "")
            }
            apply()
        }
        savedEmail.value = if (remember) email else ""
        rememberEmail.value = remember
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.login(email, pass)
            
            result.onSuccess { role ->
                _authState.value = AuthState.Success(role)
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.localizedMessage ?: "Erro desconhecido")
            }
        }
    }

    fun sendPasswordReset(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.resetPassword(email)
            result.onSuccess {
                _authState.value = AuthState.Idle
                onResult(true)
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.localizedMessage ?: "Erro desconhecido")
                onResult(false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Idle
        }
    }

    fun signUp(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signUp(email, pass)
            
            result.onSuccess { role ->
                if (authRepository.isSessionActive()) {
                    _authState.value = AuthState.Success(role)
                } else {
                    _authState.value = AuthState.VerificationSent(email)
                }
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.localizedMessage ?: "Erro desconhecido")
            }
        }
    }

    fun loginWithProvider(provider: io.github.jan.supabase.auth.providers.AuthProvider<*, *>) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.loginWithProvider(provider)
            result.onSuccess { role ->
                _authState.value = AuthState.Success(role)
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.localizedMessage ?: "Erro na autenticação social")
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
