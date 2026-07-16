package com.koinonia.igreja.domain.usecase

import com.koinonia.igreja.domain.model.Member
import com.koinonia.igreja.domain.repository.MemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetMembersUseCase @Inject constructor(
    private val repository: MemberRepository
) {
    /**
     * Executa o caso de uso. Retorna o fluxo de membros cadastrados.
     * @param query Termo de busca opcional para filtrar membros pelo nome ou e-mail.
     */
    operator fun invoke(query: String = ""): Flow<List<Member>> {
        return repository.getMembersStream().map { members ->
            if (query.isBlank()) {
                members.sortedBy { it.name }
            } else {
                members.filter {
                    it.name.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true)
                }.sortedBy { it.name }
            }
        }
    }
}
