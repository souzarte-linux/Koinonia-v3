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
    val authResolutionState = authRepository.authResolutionState
    val directedMinistries = authRepository.directedMinistries
    val isBootstrapAdmin = authRepository.isBootstrapAdmin
    val currentMember = authRepository.currentMember

    private val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    val savedEmail = MutableStateFlow(sharedPreferences.getString("saved_email", "") ?: "")
    val rememberEmail = MutableStateFlow(sharedPreferences.getBoolean("remember_email", false))
    val isBiometricEnabled = MutableStateFlow(sharedPreferences.getBoolean("biometric_enabled", false))

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("biometric_enabled", enabled).apply()
        isBiometricEnabled.value = enabled
    }

    fun saveRememberedCredentials(email: String, pass: String, remember: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean("remember_email", remember)
            if (remember || isBiometricEnabled.value) {
                putString("saved_email", email)
                putString("saved_pass", pass)
            } else {
                putString("saved_email", "")
                putString("saved_pass", "")
            }
            apply()
        }
        savedEmail.value = if (remember || isBiometricEnabled.value) email else ""
        rememberEmail.value = remember
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.login(email, pass)
            
            result.onSuccess { role ->
                saveRememberedCredentials(email, pass, rememberEmail.value)
                _authState.value = AuthState.Success(role)
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.localizedMessage ?: "Erro desconhecido")
            }
        }
    }

    fun loginWithBiometrics() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val bioResult = authRepository.authenticateBiometricSession()
            bioResult.onSuccess { role ->
                _authState.value = AuthState.Success(role)
            }.onFailure {
                val pass = sharedPreferences.getString("saved_pass", "") ?: ""
                val email = savedEmail.value
                if (email.isNotBlank() && pass.isNotBlank()) {
                    val loginResult = authRepository.login(email, pass)
                    loginResult.onSuccess { role ->
                        _authState.value = AuthState.Success(role)
                    }.onFailure { err ->
                        _authState.value = AuthState.Error("Sessão expirada. Por favor, entre com sua senha para renovar.")
                    }
                } else {
                    _authState.value = AuthState.Error("Por favor, faça login com sua senha uma vez para registrar a biometria.")
                }
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

    fun updatePassword(newPass: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.updatePassword(newPass)
            result.onSuccess {
                _authState.value = AuthState.Success(authRepository.currentUserRole.value)
                onComplete(true)
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.localizedMessage ?: "Erro ao atualizar senha")
                onComplete(false)
            }
        }
    }

    suspend fun checkIfMustChangePassword(): Boolean {
        return authRepository.mustChangePassword()
    }

    fun updateCurrentMemberProfile(
        updatedMember: com.koinonia.igreja.data.local.entity.MemberEntity,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.updateCurrentMemberProfile(updatedMember)
            onResult(result.isSuccess)
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
