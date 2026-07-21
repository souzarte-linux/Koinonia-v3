package com.koinonia.igreja.domain.usecase

import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.domain.model.MinistryDirectorship
import javax.inject.Inject

class GetMinistryDirectorshipsUseCase @Inject constructor(
    private val memberDao: MemberDao
) {
    suspend operator fun invoke(email: String): List<MinistryDirectorship> {
        val member = memberDao.getMemberByEmail(email) ?: return emptyList()
        val histories = memberDao.getMinistryHistoryByMemberId(member.id)
        
        val activeDirectors = histories.filter { hist ->
            if (hist.endDate != null) return@filter false
            val roleUpper = hist.role.uppercase()
            roleUpper.contains("DIRETOR") || roleUpper.contains("COORDENADOR") || roleUpper.contains("LÍDER") || roleUpper.contains("LIDER")
        }

        return activeDirectors.map { hist ->
            MinistryDirectorship(
                ministryId = hist.ministryId ?: "",
                ministryName = hist.ministryName
            )
        }
    }
}
