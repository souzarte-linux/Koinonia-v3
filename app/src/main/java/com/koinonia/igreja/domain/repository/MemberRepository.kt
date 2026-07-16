package com.koinonia.igreja.domain.repository

import com.koinonia.igreja.core.util.ResultWrapper
import com.koinonia.igreja.domain.model.Member
import kotlinx.coroutines.flow.Flow

interface MemberRepository {
    /**
     * Retorna a lista de membros do banco de dados local de forma reativa.
     */
    fun getMembersStream(): Flow<List<Member>>

    /**
     * Retorna um membro específico por ID.
     */
    suspend fun getMemberById(id: String): Member?

    /**
     * Insere ou atualiza um membro localmente e agenda sincronização.
     */
    suspend fun saveMember(member: Member)

    /**
     * Remove um membro localmente e agenda exclusão remota.
     */
    suspend fun deleteMember(id: String)

    /**
     * Executa a sincronização imediata bidirecional entre local (Room) e remoto (Supabase).
     */
    suspend fun syncWithRemote(): ResultWrapper<Unit>
}
