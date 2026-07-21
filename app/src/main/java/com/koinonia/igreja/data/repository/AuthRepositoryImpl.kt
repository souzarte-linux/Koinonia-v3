package com.koinonia.igreja.data.repository

import com.koinonia.igreja.data.remote.dto.UserRoleDto
import com.koinonia.igreja.domain.model.AppRole
import com.koinonia.igreja.domain.model.MinistryDirectorship
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.booleanOrNull

sealed class AuthResolutionState {
    object LOADING : AuthResolutionState()
    data class AUTHENTICATED(val role: AppRole) : AuthResolutionState()
    object UNAUTHENTICATED : AuthResolutionState()
}

/**
 * DUAS CAMADAS DE CONTROLE DE ACESSO (RBAC / OBAC):
 * (a) Papel global (AppRole): Dá poder total (hasFullAccess) ou acesso restrito à Tesouraria (hasTreasuryAccess).
 * (b) Diretoria de ministério (directedMinistries): Dá poder apenas sobre eventos vinculados a ministérios dirigidos.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val memberDao: dagger.Lazy<com.koinonia.igreja.data.local.dao.MemberDao>,
    private val getMinistryDirectorshipsUseCase: dagger.Lazy<com.koinonia.igreja.domain.usecase.GetMinistryDirectorshipsUseCase>,
    @com.koinonia.igreja.core.di.ApplicationScope private val applicationScope: kotlinx.coroutines.CoroutineScope
) {
    // Mantém o estado da Role em memória para o Navigation Compose consultar de forma reativa
    private val _currentUserRole = MutableStateFlow(AppRole.NONE)
    val currentUserRole: StateFlow<AppRole> = _currentUserRole.asStateFlow()

    private val _authResolutionState = MutableStateFlow<AuthResolutionState>(AuthResolutionState.LOADING)
    val authResolutionState: StateFlow<AuthResolutionState> = _authResolutionState.asStateFlow()

    private val _directedMinistries = MutableStateFlow<List<MinistryDirectorship>>(emptyList())
    val directedMinistries: StateFlow<List<MinistryDirectorship>> = _directedMinistries.asStateFlow()

    init {
        val email = getCurrentUserEmail()
        if (email != null) {
            applicationScope.launch {
                _authResolutionState.value = AuthResolutionState.LOADING
                val role = resolveRoleFromMinistries(email)
                _currentUserRole.value = role
                
                // Busca as diretorias ativas do membro
                val directorships = getMinistryDirectorshipsUseCase.get().invoke(email)
                _directedMinistries.value = directorships
                
                _authResolutionState.value = AuthResolutionState.AUTHENTICATED(role)
            }
        } else {
            _authResolutionState.value = AuthResolutionState.UNAUTHENTICATED
        }
    }

    private suspend fun resolveEmailFromInput(input: String): String {
        if (input.contains("@")) {
            return input.trim()
        }
        val digits = input.filter { it.isDigit() }
        if (digits.isNotBlank()) {
            val member = memberDao.get().getMemberByPhone(digits)
            if (member != null && member.email != null) {
                return member.email
            }
            return "$digits@membros.koinonia.app"
        }
        return input.trim()
    }

    suspend fun login(emailOrPhone: String, password: String): Result<AppRole> {
        return try {
            val resolvedEmail = resolveEmailFromInput(emailOrPhone)
            // 1. Autentica no provedor de identidade (Supabase Auth)
            supabaseClient.auth.signInWith(Email) {
                this.email = resolvedEmail
                this.password = password
            }

            // 2. Resolve a role dinamicamente com base no Histórico Ministerial do membro
            val role = resolveRoleFromMinistries(resolvedEmail)
            val directorships = getMinistryDirectorshipsUseCase.get().invoke(resolvedEmail)
            
            // 3. Atualiza o estado global
            _currentUserRole.value = role
            _directedMinistries.value = directorships
            _authResolutionState.value = AuthResolutionState.AUTHENTICATED(role)
            
            Result.success(role)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun logout() {
        supabaseClient.auth.signOut()
        _currentUserRole.value = AppRole.NONE
        _directedMinistries.value = emptyList()
        _authResolutionState.value = AuthResolutionState.UNAUTHENTICATED
    }

    suspend fun resetPassword(emailOrPhone: String): Result<Unit> {
        return try {
            val resolvedEmail = resolveEmailFromInput(emailOrPhone)
            supabaseClient.auth.resetPasswordForEmail(resolvedEmail)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<AppRole> {
        return try {
            // 1. Cadastra no provedor de identidade (Supabase Auth)
            val userInfo = supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val userId = userInfo?.id 
                ?: supabaseClient.auth.currentUserOrNull()?.id
                ?: throw Exception("Falha ao obter ID do usuário")

            // 2. Resolve a role dinamicamente
            val role = resolveRoleFromMinistries(email)
            val directorships = getMinistryDirectorshipsUseCase.get().invoke(email)

            // 3. Salva a role padrão de teste (DIACONO) na tabela remota apenas para manter sync
            try {
                supabaseClient.postgrest["user_roles"].insert(
                    mapOf("user_id" to userId, "role" to role.name)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (supabaseClient.auth.currentSessionOrNull() != null) {
                _currentUserRole.value = role
                _directedMinistries.value = directorships
                _authResolutionState.value = AuthResolutionState.AUTHENTICATED(role)
            } else {
                _authResolutionState.value = AuthResolutionState.UNAUTHENTICATED
            }
            Result.success(role)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun isSessionActive(): Boolean {
        return supabaseClient.auth.currentSessionOrNull() != null
    }

    suspend fun loginWithProvider(provider: io.github.jan.supabase.auth.providers.AuthProvider<*, *>) : Result<AppRole> {
        return try {
            // Realiza login/cadastro social via Supabase Auth
            supabaseClient.auth.signInWith(provider)

            val session = supabaseClient.auth.currentSessionOrNull()
                ?: throw Exception("Sessão não iniciada")
            val userId = session.user?.id ?: throw Exception("Usuário inválido")
            val email = session.user?.email ?: ""

            // Resolve a role dinamicamente
            val role = resolveRoleFromMinistries(email)
            val directorships = getMinistryDirectorshipsUseCase.get().invoke(email)

            try {
                // Cadastra a role resolvida no banco remoto para fins de auditoria
                supabaseClient.postgrest["user_roles"].insert(
                    mapOf("user_id" to userId, "role" to role.name)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            _currentUserRole.value = role
            _directedMinistries.value = directorships
            _authResolutionState.value = AuthResolutionState.AUTHENTICATED(role)
            Result.success(role)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun getCurrentUserEmail(): String? {
        return supabaseClient.auth.currentSessionOrNull()?.user?.email
    }

    suspend fun signUpForMember(email: String, password: String): Result<String> {
        return try {
            val userInfo = supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("must_change_password", true)
                }
            }
            val userId = userInfo?.id 
                ?: supabaseClient.auth.currentUserOrNull()?.id
                ?: throw Exception("Falha ao obter ID do usuário")
            Result.success(userId)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun mustChangePassword(): Boolean {
        val email = getCurrentUserEmail() ?: return false
        val member = memberDao.get().getMemberByEmail(email)
        if (member?.mustChangePassword == true) return true

        // Fallback para metadados remotos do Supabase Auth (caso o banco local esteja vazio no 1º login em novo aparelho)
        val user = supabaseClient.auth.currentSessionOrNull()?.user
        val metaFlag = user?.userMetadata?.get("must_change_password")?.jsonPrimitive?.booleanOrNull
        if (metaFlag == true) {
            if (member != null) {
                memberDao.get().insertMember(member.copy(mustChangePassword = true))
            }
            return true
        }
        return false
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            supabaseClient.auth.updateUser {
                password = newPassword
                data = buildJsonObject {
                    put("must_change_password", false)
                }
            }
            val email = getCurrentUserEmail()
            if (email != null) {
                val member = memberDao.get().getMemberByEmail(email)
                if (member != null) {
                    memberDao.get().insertMember(member.copy(mustChangePassword = false))
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun resolveRoleFromMinistries(email: String): AppRole {
        try {
            val dao = memberDao.get()
            val member = dao.getMemberByEmail(email) ?: return AppRole.VIEWER
            val ministries = dao.getMinistryHistoryByMemberId(member.id)
            
            val activeMinistries = ministries.filter { it.endDate == null }
            
            val hasPastorRole = activeMinistries.any { min ->
                min.role.uppercase().contains("PASTOR")
            }
            if (hasPastorRole) return AppRole.PASTOR

            val hasAnciaoRole = activeMinistries.any { min ->
                val roleUpper = min.role.uppercase()
                roleUpper.contains("ANCIÃO") || roleUpper.contains("ANCIAO")
            }
            if (hasAnciaoRole) return AppRole.ANCIAO

            val hasAdminRole = activeMinistries.any { min ->
                val roleUpper = min.role.uppercase()
                roleUpper.contains("ADMIN") || roleUpper.contains("ADM")
            }
            if (hasAdminRole) return AppRole.ADMIN

            val hasDiaconoRole = activeMinistries.any { min ->
                val roleUpper = min.role.uppercase()
                roleUpper.contains("DIÁCONO") || roleUpper.contains("DIACONO") || roleUpper.contains("LÍDER") || roleUpper.contains("LIDER") || roleUpper.contains("DIRETOR") || roleUpper.contains("COORDENADOR")
            }
            if (hasDiaconoRole) return AppRole.DIACONO

            val hasTesoureiroRole = activeMinistries.any { min ->
                min.role.uppercase().contains("TESOUREIRO")
            }
            if (hasTesoureiroRole) return AppRole.TESOUREIRO
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return AppRole.VIEWER
    }
}
