package com.koinonia.igreja.domain.usecase

import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class GetMinistryDirectorshipsUseCaseTest {

    private val fakeMemberDao = FakeMemberDao()
    private val useCase = GetMinistryDirectorshipsUseCase(fakeMemberDao)

    @Test
    fun invoke_whenEmailNotFound_returnsEmptyList() = runTest {
        val result = useCase("notfound@koinonia.org")
        assertTrue(result.isEmpty())
    }

    @Test
    fun invoke_whenMemberHasNoMinistryHistory_returnsEmptyList() = runTest {
        val member = MemberEntity(id = "m1", fullName = "João Silva", email = "joao@koinonia.org")
        fakeMemberDao.members.add(member)

        val result = useCase("joao@koinonia.org")
        assertTrue(result.isEmpty())
    }

    @Test
    fun invoke_whenHistoryIsExpired_ignoresInactiveRoles() = runTest {
        val member = MemberEntity(id = "m2", fullName = "Maria Souza", email = "maria@koinonia.org")
        fakeMemberDao.members.add(member)
        fakeMemberDao.histories.add(
            MinistryHistoryEntity(
                id = "h1",
                memberId = "m2",
                ministryId = "min_louvor",
                ministryName = "Louvor",
                role = "Diretor de Ministério",
                startDate = Date(),
                endDate = Date() // Expirado/Inativo
            )
        )

        val result = useCase("maria@koinonia.org")
        assertTrue(result.isEmpty())
    }

    @Test
    fun invoke_whenRoleIsDirector_returnsActiveDirectorship() = runTest {
        val member = MemberEntity(id = "m3", fullName = "Carlos Pedro", email = "carlos@koinonia.org")
        fakeMemberDao.members.add(member)
        fakeMemberDao.histories.add(
            MinistryHistoryEntity(
                id = "h2",
                memberId = "m3",
                ministryId = "min_louvor",
                ministryName = "Ministério de Louvor",
                role = "Diretor de Ministério",
                startDate = Date(),
                endDate = null
            )
        )

        val result = useCase("carlos@koinonia.org")
        assertEquals(1, result.size)
        assertEquals("min_louvor", result.first().ministryId)
        assertEquals("Ministério de Louvor", result.first().ministryName)
    }

    @Test
    fun invoke_whenRoleIsCoordinatorOrLeader_returnsActiveDirectorships() = runTest {
        val member = MemberEntity(id = "m4", fullName = "Ana Lima", email = "ana@koinonia.org")
        fakeMemberDao.members.add(member)
        fakeMemberDao.histories.addAll(
            listOf(
                MinistryHistoryEntity(
                    id = "h3",
                    memberId = "m4",
                    ministryId = "min_teatro",
                    ministryName = "Teatro",
                    role = "Coordenador Geral",
                    startDate = Date(),
                    endDate = null
                ),
                MinistryHistoryEntity(
                    id = "h4",
                    memberId = "m4",
                    ministryId = "min_jovens",
                    ministryName = "Rede de Jovens",
                    role = "Líder de Jovens",
                    startDate = Date(),
                    endDate = null
                ),
                MinistryHistoryEntity(
                    id = "h5",
                    memberId = "m4",
                    ministryId = "min_kids",
                    ministryName = "Koinonia Kids",
                    role = "Lider Infantil",
                    startDate = Date(),
                    endDate = null
                )
            )
        )

        val result = useCase("ana@koinonia.org")
        assertEquals(3, result.size)
    }

    @Test
    fun invoke_whenRoleIsLowercase_matchesCaseInsensitive() = runTest {
        val member = MemberEntity(id = "m5", fullName = "Pedro Santos", email = "pedro@koinonia.org")
        fakeMemberDao.members.add(member)
        fakeMemberDao.histories.add(
            MinistryHistoryEntity(
                id = "h6",
                memberId = "m5",
                ministryId = "min_midia",
                ministryName = "Mídia",
                role = "diretor técnico",
                startDate = Date(),
                endDate = null
            )
        )

        val result = useCase("pedro@koinonia.org")
        assertEquals(1, result.size)
        assertEquals("min_midia", result.first().ministryId)
    }

    @Test
    fun invoke_whenRoleIsNotLeadership_returnsEmptyList() = runTest {
        val member = MemberEntity(id = "m6", fullName = "Lucas Silva", email = "lucas@koinonia.org")
        fakeMemberDao.members.add(member)
        fakeMemberDao.histories.add(
            MinistryHistoryEntity(
                id = "h7",
                memberId = "m6",
                ministryId = "min_louvor",
                ministryName = "Louvor",
                role = "Instrumentista / Membro",
                startDate = Date(),
                endDate = null
            )
        )

        val result = useCase("lucas@koinonia.org")
        assertTrue(result.isEmpty())
    }

    @Test
    fun invoke_whenMinistryIdIsNull_returnsEmptyStringForMinistryId() = runTest {
        val member = MemberEntity(id = "m7", fullName = "Julia Costa", email = "julia@koinonia.org")
        fakeMemberDao.members.add(member)
        fakeMemberDao.histories.add(
            MinistryHistoryEntity(
                id = "h8",
                memberId = "m7",
                ministryId = null,
                ministryName = "Ministério Geral",
                role = "Diretor",
                startDate = Date(),
                endDate = null
            )
        )

        val result = useCase("julia@koinonia.org")
        assertEquals(1, result.size)
        assertEquals("", result.first().ministryId)
        assertEquals("Ministério Geral", result.first().ministryName)
    }
}

private class FakeMemberDao : MemberDao {
    val members = mutableListOf<MemberEntity>()
    val histories = mutableListOf<MinistryHistoryEntity>()

    override suspend fun insertMember(member: MemberEntity) { members.add(member) }
    override suspend fun insertMembers(members: List<MemberEntity>) { this.members.addAll(members) }
    override fun getAllMembers(): Flow<List<MemberEntity>> = flowOf(members)
    override suspend fun getMemberByEmail(email: String): MemberEntity? {
        return members.find { it.email?.equals(email, ignoreCase = true) == true || it.socialMedia?.equals(email, ignoreCase = true) == true }
    }
    override suspend fun getMemberByPhone(phone: String): MemberEntity? = members.find { it.phone == phone }
    override suspend fun getMemberById(id: String): MemberEntity? = members.find { it.id == id }
    override suspend fun getFamilyMembers(familyId: String): List<MemberEntity> = members.filter { it.familyId == familyId }
    override suspend fun getPendingSyncMembers(): List<MemberEntity> = members.filter { it.syncPending }
    override suspend fun markAsSynced(id: String) {}
    override suspend fun deleteById(id: String) { members.removeAll { it.id == id } }
    override suspend fun getChildrenByMemberId(memberId: String): List<ChildEntity> = emptyList()
    override suspend fun getMinistryHistoryByMemberId(memberId: String): List<MinistryHistoryEntity> {
        return histories.filter { it.memberId == memberId }
    }
    override fun getAllMinistryHistoriesFlow(): Flow<List<MinistryHistoryEntity>> = flowOf(histories)
    override suspend fun insertMinistryHistories(histories: List<MinistryHistoryEntity>) { this.histories.addAll(histories) }
}
