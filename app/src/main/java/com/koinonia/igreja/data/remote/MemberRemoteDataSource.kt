package com.koinonia.igreja.data.remote

import com.koinonia.igreja.core.util.Constants
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemberRemoteDataSource @Inject constructor(
    private val postgrest: Postgrest
) {
    /**
     * Busca todos os membros remotos do Supabase.
     */
    suspend fun fetchAllMembers(): List<MemberDto> {
        return postgrest[Constants.TABLE_MEMBERS]
            .select()
            .decodeList()
    }

    /**
     * Insere ou atualiza um membro no Supabase.
     */
    suspend fun upsertMember(memberDto: MemberDto) {
        postgrest[Constants.TABLE_MEMBERS]
            .upsert(memberDto)
    }

    /**
     * Exclui um membro remoto no Supabase por ID.
     */
    suspend fun deleteMember(id: String) {
        postgrest[Constants.TABLE_MEMBERS]
            .delete {
                filter {
                    eq("id", id)
                }
            }
    }
}
