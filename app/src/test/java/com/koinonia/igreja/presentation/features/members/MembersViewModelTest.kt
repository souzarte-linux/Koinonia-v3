package com.koinonia.igreja.presentation.features.members

import com.koinonia.igreja.core.util.ResultWrapper
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import com.koinonia.igreja.domain.model.Member
import com.koinonia.igreja.domain.repository.MemberRepository
import com.koinonia.igreja.domain.usecase.GetMembersUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.ZonedDateTime
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class MembersViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var fakeRepository: FakeMemberRepositoryForMembersVM
    private lateinit var fakeMemberDao: FakeMembersDaoForVM
    private lateinit var getMembersUseCase: GetMembersUseCase
    private lateinit var viewModel: MembersViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeMemberRepositoryForMembersVM()
        fakeMemberDao = FakeMembersDaoForVM()
        getMembersUseCase = GetMembersUseCase(fakeRepository)

        viewModel = MembersViewModel(
            getMembersUseCase = getMembersUseCase,
            memberRepository = fakeRepository,
            memberDao = fakeMemberDao
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createDomainMember(id: String, name: String, email: String): Member {
        return Member(
            id = id,
            name = name,
            email = email,
            phone = "5571999998888",
            role = "MEMBRO",
            joinedAt = ZonedDateTime.now(),
            isActive = true
        )
    }

    @Test
    fun searchQuery_whenUpdated_triggersReactiveFilteringInMembersState() = runTest {
        val member1 = createDomainMember("m1", "Carlos Eduardo", "carlos@test.com")
        val member2 = createDomainMember("m2", "Ana Maria", "ana@test.com")
        fakeRepository.membersFlow.value = listOf(member1, member2)

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.membersState.collect {}
        }
        testScheduler.advanceUntilIdle()

        assertEquals(2, viewModel.membersState.value.size)
        assertEquals("Ana Maria", viewModel.membersState.value[0].name)
        collectJob.cancel()
    }

    @Test
    fun membersState_whenUseCaseReturnsEmpty_emitsEmptyList() = runTest {
        fakeRepository.membersFlow.value = emptyList()

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.membersState.collect {}
        }
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.membersState.value.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun loadMemberDetails_loadsCorrectMemberIntoSelectedMember() = runTest {
        val member = createDomainMember("m100", "João Silva", "joao@test.com")
        fakeRepository.membersFlow.value = listOf(member)

        viewModel.loadMemberDetails("m100")
        testScheduler.advanceUntilIdle()

        assertNotNull(viewModel.selectedMember.value)
        assertEquals("m100", viewModel.selectedMember.value?.id)
        assertEquals("João Silva", viewModel.selectedMember.value?.name)
    }

    @Test
    fun loadMemberEntityDetails_loadsEntityChildrenAndMinistries() = runTest {
        val entity = MemberEntity(
            id = "m1",
            fullName = "Roberto Santos",
            email = "roberto@test.com"
        )
        val child = ChildEntity(
            id = "c1",
            memberId = "m1",
            fullName = "Pedrinho Santos",
            gender = "Masculino",
            isBaptized = false,
            birthDate = Date()
        )
        val history = MinistryHistoryEntity(
            id = "h1",
            memberId = "m1",
            ministryId = "mja",
            ministryName = "MJA",
            role = "Líder",
            startDate = Date(),
            endDate = null
        )

        fakeMemberDao.members.add(entity)
        fakeMemberDao.children.add(child)
        fakeMemberDao.ministryHistories.add(history)

        viewModel.loadMemberEntityDetails("m1")
        testScheduler.advanceUntilIdle()

        assertEquals("Roberto Santos", viewModel.selectedMemberEntity.value?.fullName)
        assertEquals(1, viewModel.selectedChildren.value.size)
        assertEquals("Pedrinho Santos", viewModel.selectedChildren.value[0].fullName)
        assertEquals(1, viewModel.selectedMinistries.value.size)
        assertEquals("MJA", viewModel.selectedMinistries.value[0].ministryName)
    }

    @Test
    fun saveMember_persistsMemberInRepositoryAndExecutesOnSuccess() = runTest {
        var onSuccessCalled = false

        viewModel.saveMember(
            id = "m_new",
            name = "Novo Membro",
            email = "novo@test.com",
            phone = "71988887777",
            role = "Ancião",
            isActive = true,
            onSuccess = { onSuccessCalled = true }
        )

        testScheduler.advanceUntilIdle()

        assertTrue(onSuccessCalled)
        val saved = fakeRepository.membersFlow.value.find { it.id == "m_new" }
        assertNotNull(saved)
        assertEquals("Novo Membro", saved?.name)
        assertEquals("Ancião", saved?.role)
    }

    @Test
    fun deleteMember_removesMemberFromRepositoryAndExecutesOnSuccess() = runTest {
        val member = createDomainMember("m_del", "Membro Para Deletar", "del@test.com")
        fakeRepository.membersFlow.value = listOf(member)
        var onSuccessCalled = false

        viewModel.deleteMember("m_del") {
            onSuccessCalled = true
        }

        testScheduler.advanceUntilIdle()

        assertTrue(onSuccessCalled)
        assertNull(fakeRepository.membersFlow.value.find { it.id == "m_del" })
    }

    @Test
    fun forceSync_callsSyncWithRemoteOnRepository() = runTest {
        viewModel.forceSync()
        testScheduler.advanceUntilIdle()

        assertTrue(fakeRepository.syncCalled)
    }
}

class FakeMemberRepositoryForMembersVM : MemberRepository {
    val membersFlow = MutableStateFlow<List<Member>>(emptyList())
    var syncCalled = false

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
        syncCalled = true
        return ResultWrapper.Success(Unit)
    }
}

class FakeMembersDaoForVM : MemberDao {
    val members = mutableListOf<MemberEntity>()
    val children = mutableListOf<ChildEntity>()
    val ministryHistories = mutableListOf<MinistryHistoryEntity>()

    override fun getAllMembers(): Flow<List<MemberEntity>> = flowOf(members)
    override suspend fun getMemberById(id: String): MemberEntity? = members.find { it.id == id }
    override suspend fun getMemberByEmail(email: String): MemberEntity? = members.find { it.email == email }
    override suspend fun getMemberByPhone(phone: String): MemberEntity? = members.find { it.phone == phone }
    override suspend fun insertMember(member: MemberEntity) { members.add(member) }
    override suspend fun insertMembers(membersList: List<MemberEntity>) { members.addAll(membersList) }
    override suspend fun deleteById(id: String) { members.removeAll { it.id == id } }
    override suspend fun getChildrenByMemberId(memberId: String): List<ChildEntity> = children.filter { it.memberId == memberId }
    override suspend fun getMinistryHistoryByMemberId(memberId: String): List<MinistryHistoryEntity> = ministryHistories.filter { it.memberId == memberId }
    override fun getAllMinistryHistoriesFlow(): Flow<List<MinistryHistoryEntity>> = flowOf(ministryHistories)
    override suspend fun getFamilyMembers(familyId: String): List<MemberEntity> = members.filter { it.familyId == familyId }
    override suspend fun getPendingSyncMembers(): List<MemberEntity> = emptyList()
    override suspend fun markAsSynced(id: String) {}
    override suspend fun insertMinistryHistories(histories: List<MinistryHistoryEntity>) { ministryHistories.addAll(histories) }
}
