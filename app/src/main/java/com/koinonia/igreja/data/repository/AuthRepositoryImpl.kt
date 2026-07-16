package com.koinonia.igreja.data.repository

import com.koinonia.igreja.data.remote.dto.UserRoleDto
import com.koinonia.igreja.domain.model.AppRole
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    // Mantém o estado da Role em memória para o Navigation Compose consultar de forma reativa
    private val _currentUserRole = MutableStateFlow(AppRole.NONE)
    val currentUserRole: StateFlow<AppRole> = _currentUserRole.asStateFlow()

    suspend fun login(email: String, password: String): Result<AppRole> {
        return try {
            // 1. Autentica no provedor de identidade (Supabase Auth)
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            // 2. Recupera o ID do usuário autenticado
            val session = supabaseClient.auth.currentSessionOrNull()
                ?: throw Exception("Falha ao obter sessão")
            val userId = session.user?.id ?: throw Exception("Usuário inválido")

            // 3. Busca a Role associada a este usuário na tabela user_roles
            val roleDto = supabaseClient.postgrest["user_roles"]
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<UserRoleDto>()

            // 4. Mapeia a string do banco para o Enum
            val role = roleDto?.role?.let { AppRole.valueOf(it) } ?: AppRole.VIEWER
            
            // 5. Atualiza o estado global
            _currentUserRole.value = role
            
            Result.success(role)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun logout() {
        supabaseClient.auth.signOut()
        _currentUserRole.value = AppRole.NONE
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            supabaseClient.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<AppRole> {
        return try {
            // 1. Cadastra no provedor de identidade (Supabase Auth)
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            // 2. Recupera o ID do usuário criado
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: throw Exception("Falha ao obter ID do usuário")

            // 3. Atribui uma role padrão de teste (DIACONO) na tabela user_roles
            val defaultRole = AppRole.DIACONO
            try {
                supabaseClient.postgrest["user_roles"].insert(
                    mapOf("user_id" to userId, "role" to defaultRole.name)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            _currentUserRole.value = defaultRole
            Result.success(defaultRole)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun loginWithProvider(provider: io.github.jan.supabase.auth.providers.AuthProvider<*, *>): Result<AppRole> {
        return try {
            // Realiza login/cadastro social via Supabase Auth
            supabaseClient.auth.signInWith(provider)

            val session = supabaseClient.auth.currentSessionOrNull()
                ?: throw Exception("Sessão não iniciada")
            val userId = session.user?.id ?: throw Exception("Usuário inválido")

            // Busca a Role associada ou cria uma nova de Diácono para testes
            var role = AppRole.DIACONO
            try {
                val roleDto = supabaseClient.postgrest["user_roles"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeSingleOrNull<UserRoleDto>()

                if (roleDto != null) {
                    role = AppRole.valueOf(roleDto.role)
                } else {
                    // Cadastra a role de Diácono no banco para ele ter acesso
                    supabaseClient.postgrest["user_roles"].insert(
                        mapOf("user_id" to userId, "role" to role.name)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            _currentUserRole.value = role
            Result.success(role)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
