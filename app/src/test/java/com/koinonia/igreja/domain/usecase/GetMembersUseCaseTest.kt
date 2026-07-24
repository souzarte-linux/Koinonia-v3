package com.koinonia.igreja.domain.usecase

import com.koinonia.igreja.core.util.ResultWrapper
import com.koinonia.igreja.domain.model.Member
import com.koinonia.igreja.domain.repository.MemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZonedDateTime

class GetMembersUseCaseTest {

    private val fakeRepository = FakeMemberRepository()
    private val useCase = GetMembersUseCase(fakeRepository)

    private fun createMember(id: String, name: String, email: String): Member {
        return Member(
            id = id,
            name = name,
            email = email,
            phone = "71999999999",
            role = "MEMBRO",
            joinedAt = ZonedDateTime.now(),
            isActive = true
        )
    }

    @Test
    fun invoke_whenQueryIsBlank_returnsAllMembersSortedByName() = runTest {
        val members = listOf(
            createMember("1", "Carlos Souza", "carlos@test.com"),
            createMember("2", "Ana Lima", "ana@test.com"),
            createMember("3", "Bruno Alves", "bruno@test.com")
        )
        fakeRepository.membersFlow.value = members

        val result = useCase("").first()

        assertEquals(3, result.size)
        assertEquals("Ana Lima", result[0].name)
        assertEquals("Bruno Alves", result[1].name)
        assertEquals("Carlos Souza", result[2].name)
    }

    @Test
    fun invoke_whenQueryMatchesNameCaseInsensitive_returnsFilteredSortedMembers() = runTest {
        val members = listOf(
            createMember("1", "Maria Silva", "maria@test.com"),
            createMember("2", "João Santos", "joao@test.com"),
            createMember("3", "Manoel Costa", "manoel@test.com")
        )
        fakeRepository.membersFlow.value = members

        val result = useCase("ma").first()

        assertEquals(2, result.size)
        assertEquals("Manoel Costa", result[0].name)
        assertEquals("Maria Silva", result[1].name)
    }

    @Test
    fun invoke_whenQueryMatchesEmail_returnsFilteredMembers() = runTest {
        val members = listOf(
            createMember("1", "Pedro Oliveira", "pedro@koinonia.org"),
            createMember("2", "Lucas Ferreira", "lucas@other.org")
        )
        fakeRepository.membersFlow.value = members

        val result = useCase("koinonia.org").first()

        assertEquals(1, result.size)
        assertEquals("Pedro Oliveira", result[0].name)
    }

    @Test
    fun invoke_whenQueryDoesNotMatch_returnsEmptyList() = runTest {
        val members = listOf(
            createMember("1", "Pedro Oliveira", "pedro@koinonia.org")
        )
        fakeRepository.membersFlow.value = members

        val result = useCase("Xyz").first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun invoke_whenRepositoryIsEmpty_returnsEmptyList() = runTest {
        fakeRepository.membersFlow.value = emptyList()

        val result = useCase("").first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun invoke_whenRepositoryEmitsMultipleTimes_updatesStreamCorrectly() = runTest {
        fakeRepository.membersFlow.value = listOf(
            createMember("1", "Carlos Souza", "carlos@test.com"),
            createMember("2", "Ana Lima", "ana@test.com")
        )

        val firstEmission = useCase("").first()
        assertEquals(2, firstEmission.size)
        assertEquals("Ana Lima", firstEmission[0].name)

        fakeRepository.membersFlow.value = listOf(
            createMember("1", "Carlos Souza", "carlos@test.com"),
            createMember("2", "Ana Lima", "ana@test.com"),
            createMember("3", "Bruno Alves", "bruno@test.com")
        )

        val secondEmission = useCase("").first()
        assertEquals(3, secondEmission.size)
        assertEquals("Bruno Alves", secondEmission[1].name)
    }
}

class FakeMemberRepository : MemberRepository {
    val membersFlow = MutableStateFlow<List<Member>>(emptyList())

    override fun getMembersStream(): Flow<List<Member>> = membersFlow

    override suspend fun getMemberById(id: String): Member? {
        return membersFlow.value.find { it.id == id }
    }

    override suspend fun saveMember(member: Member) {
        val current = membersFlow.value.toMutableList()
        current.removeAll { it.id == member.id }
        current.add(member)
        membersFlow.value = current
    }

    override suspend fun deleteMember(id: String) {
        val current = membersFlow.value.toMutableList()
        current.removeAll { it.id == id }
        membersFlow.value = current
    }

    override suspend fun syncWithRemote(): ResultWrapper<Unit> {
        return ResultWrapper.Success(Unit)
    }
}
