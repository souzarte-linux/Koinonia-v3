package com.koinonia.igreja.presentation.features.members

import com.koinonia.igreja.core.util.ResultWrapper
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.dao.MemberRegistrationDao
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.FamilyEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import com.koinonia.igreja.data.local.seeder.DatabaseSeeder
import com.koinonia.igreja.domain.model.Member
import com.koinonia.igreja.domain.repository.MemberRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class MemberListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var fakeMemberDao: FakeMemberDaoForMemberListVM
    private lateinit var fakeRegistrationDao: FakeRegistrationDaoForMemberListVM
    private lateinit var fakeRepository: FakeMemberRepositoryForMemberListVM
    private lateinit var databaseSeeder: DatabaseSeeder
    private lateinit var viewModel: MemberListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        fakeMemberDao = FakeMemberDaoForMemberListVM()
        fakeRegistrationDao = FakeRegistrationDaoForMemberListVM()
        fakeRepository = FakeMemberRepositoryForMemberListVM()

        databaseSeeder = DatabaseSeeder(
            registrationDao = fakeRegistrationDao,
            memberRepository = fakeRepository
        )

        viewModel = MemberListViewModel(
            memberDao = fakeMemberDao,
            databaseSeeder = databaseSeeder
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialSearchQuery_isEmptyString() {
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun init_executesDatabaseSeeder_andCallsRegistrationDaoAndSync() = runTest {
        testScheduler.advanceUntilIdle()
        assertTrue(fakeRegistrationDao.registerFullMemberCount >= 20)
        assertTrue(fakeRepository.syncCalled)
    }

    @Test
    fun membersList_whenDaoIsEmpty_emitsEmptyList() = runTest {
        val result = viewModel.membersList.first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun membersList_combinesMembersAndMinistryHistories() = runTest {
        val member1 = MemberEntity(
            id = "m1",
            fullName = "João da Silva",
            photoUrl = null,
            email = "joao@koinonia.app",
            phone = "71988880001",
            familyId = "fam_1",
            birthDate = null,
            civilStatus = null,
            rg = null,
            cpf = null,
            spouseId = null,
            spouseName = null,
            isWhatsapp = false,
            socialMedia = null,
            cep = null,
            street = null,
            number = null,
            neighborhood = null,
            city = null,
            state = null,
            complement = null,
            baptismDate = null,
            rebaptismDate = null,
            hasVehicle = false,
            vehicleType = null,
            vehicleModel = null
        )
        val history1 = MinistryHistoryEntity(
            id = "h1",
            memberId = "m1",
            ministryId = "mja",
            ministryName = "Ministério de Jovens",
            role = "Líder",
            startDate = Date(),
            endDate = null
        )

        fakeMemberDao.membersFlow.value = listOf(member1)
        fakeMemberDao.historiesFlow.value = listOf(history1)

        val result = viewModel.membersList.first()

        assertEquals(1, result.size)
        val item = result[0]
        assertEquals("m1", item.member.id)
        assertEquals("João da Silva", item.member.fullName)
        assertEquals("joao@koinonia.app", item.member.email)
        assertEquals("Ministério de Jovens", item.ministry)
        assertEquals("Líder", item.role)
    }

    @Test
    fun searchQueryChange_filtersMembersByNameCaseInsensitive() = runTest {
        val m1 = MemberEntity(id = "m1", fullName = "Maria Santos", photoUrl = null, email = "maria@test.com", phone = "71900000001", familyId = null, birthDate = null, civilStatus = null, rg = null, cpf = null, spouseId = null, spouseName = null, isWhatsapp = false, socialMedia = null, cep = null, street = null, number = null, neighborhood = null, city = null, state = null, complement = null, baptismDate = null, rebaptismDate = null, hasVehicle = false, vehicleType = null, vehicleModel = null)
        val m2 = MemberEntity(id = "m2", fullName = "Carlos Eduardo", photoUrl = null, email = "carlos@test.com", phone = "71900000002", familyId = null, birthDate = null, civilStatus = null, rg = null, cpf = null, spouseId = null, spouseName = null, isWhatsapp = false, socialMedia = null, cep = null, street = null, number = null, neighborhood = null, city = null, state = null, complement = null, baptismDate = null, rebaptismDate = null, hasVehicle = false, vehicleType = null, vehicleModel = null)

        fakeMemberDao.membersFlow.value = listOf(m1, m2)

        // Busca por "MARIA"
        viewModel.searchQuery.value = "MARIA"
        var result = viewModel.membersList.first()
        assertEquals(1, result.size)
        assertEquals("Maria Santos", result[0].member.fullName)

        // Busca por "carlos"
        viewModel.searchQuery.value = "carlos"
        result = viewModel.membersList.first()
        assertEquals(1, result.size)
        assertEquals("Carlos Eduardo", result[0].member.fullName)
    }

    @Test
    fun membersList_updatesReactivelyWhenDaoEmitsNewMembers() = runTest {
        val m1 = MemberEntity(id = "m1", fullName = "Membro Um", photoUrl = null, email = null, phone = null, familyId = null, birthDate = null, civilStatus = null, rg = null, cpf = null, spouseId = null, spouseName = null, isWhatsapp = false, socialMedia = null, cep = null, street = null, number = null, neighborhood = null, city = null, state = null, complement = null, baptismDate = null, rebaptismDate = null, hasVehicle = false, vehicleType = null, vehicleModel = null)
        fakeMemberDao.membersFlow.value = listOf(m1)

        var result = viewModel.membersList.first()
        assertEquals(1, result.size)

        val m2 = MemberEntity(id = "m2", fullName = "Membro Dois", photoUrl = null, email = null, phone = null, familyId = null, birthDate = null, civilStatus = null, rg = null, cpf = null, spouseId = null, spouseName = null, isWhatsapp = false, socialMedia = null, cep = null, street = null, number = null, neighborhood = null, city = null, state = null, complement = null, baptismDate = null, rebaptismDate = null, hasVehicle = false, vehicleType = null, vehicleModel = null)
        fakeMemberDao.membersFlow.value = listOf(m1, m2)

        result = viewModel.membersList.first()
        assertEquals(2, result.size)
    }

    @Test
    fun searchQueryChange_whenQueryIsBlank_returnsAllMembers() = runTest {
        val m1 = MemberEntity(id = "m1", fullName = "Ana Lima", photoUrl = null, email = null, phone = null, familyId = null, birthDate = null, civilStatus = null, rg = null, cpf = null, spouseId = null, spouseName = null, isWhatsapp = false, socialMedia = null, cep = null, street = null, number = null, neighborhood = null, city = null, state = null, complement = null, baptismDate = null, rebaptismDate = null, hasVehicle = false, vehicleType = null, vehicleModel = null)
        val m2 = MemberEntity(id = "m2", fullName = "Bruno Costa", photoUrl = null, email = null, phone = null, familyId = null, birthDate = null, civilStatus = null, rg = null, cpf = null, spouseId = null, spouseName = null, isWhatsapp = false, socialMedia = null, cep = null, street = null, number = null, neighborhood = null, city = null, state = null, complement = null, baptismDate = null, rebaptismDate = null, hasVehicle = false, vehicleType = null, vehicleModel = null)

        fakeMemberDao.membersFlow.value = listOf(m1, m2)

        viewModel.searchQuery.value = "Ana"
        var result = viewModel.membersList.first()
        assertEquals(1, result.size)

        viewModel.searchQuery.value = "   "
        result = viewModel.membersList.first()
        assertEquals(2, result.size)
    }
}

class FakeMemberDaoForMemberListVM : MemberDao {
    val membersFlow = MutableStateFlow<List<MemberEntity>>(emptyList())
    val historiesFlow = MutableStateFlow<List<MinistryHistoryEntity>>(emptyList())

    override fun getAllMembers(): Flow<List<MemberEntity>> = membersFlow
    override fun getAllMinistryHistoriesFlow(): Flow<List<MinistryHistoryEntity>> = historiesFlow
    override suspend fun getMemberById(id: String): MemberEntity? = membersFlow.value.find { it.id == id }
    override suspend fun getMemberByEmail(email: String): MemberEntity? = membersFlow.value.find { it.email == email }
    override suspend fun getMemberByPhone(phone: String): MemberEntity? = membersFlow.value.find { it.phone == phone }
    override suspend fun insertMember(member: MemberEntity) { membersFlow.value = membersFlow.value + member }
    override suspend fun insertMembers(membersList: List<MemberEntity>) { membersFlow.value = membersFlow.value + membersList }
    override suspend fun deleteById(id: String) { membersFlow.value = membersFlow.value.filter { it.id != id } }
    override suspend fun getChildrenByMemberId(memberId: String): List<ChildEntity> = emptyList()
    override suspend fun getMinistryHistoryByMemberId(memberId: String): List<MinistryHistoryEntity> = historiesFlow.value.filter { it.memberId == memberId }
    override suspend fun getFamilyMembers(familyId: String): List<MemberEntity> = membersFlow.value.filter { it.familyId == familyId }
    override suspend fun getPendingSyncMembers(): List<MemberEntity> = emptyList()
    override suspend fun markAsSynced(id: String) {}
    override suspend fun insertMinistryHistories(histories: List<MinistryHistoryEntity>) { historiesFlow.value = historiesFlow.value + histories }
}

class FakeRegistrationDaoForMemberListVM : MemberRegistrationDao {
    val families = mutableListOf<FamilyEntity>()
    var registerFullMemberCount = 0

    override suspend fun getAllFamilies(): List<FamilyEntity> = families
    override suspend fun insertFamily(family: FamilyEntity) { families.add(family) }
    override suspend fun insertMember(member: MemberEntity) {}
    override suspend fun insertChildren(children: List<ChildEntity>) {}
    override suspend fun insertMinistryHistory(history: List<MinistryHistoryEntity>) {}
    override suspend fun deleteChildrenByMemberId(memberId: String) {}
    override suspend fun deleteMinistryHistoryByMemberId(memberId: String) {}
    override suspend fun registerFullMember(newFamily: FamilyEntity?, member: MemberEntity, children: List<ChildEntity>, ministryHistory: List<MinistryHistoryEntity>, isEdit: Boolean) {
        registerFullMemberCount++
    }
}

class FakeMemberRepositoryForMemberListVM : MemberRepository {
    var syncCalled = false
    override fun getMembersStream(): Flow<List<Member>> = flowOf(emptyList())
    override suspend fun getMemberById(id: String): Member? = null
    override suspend fun saveMember(member: Member) {}
    override suspend fun deleteMember(id: String) {}
    override suspend fun syncWithRemote(): ResultWrapper<Unit> {
        syncCalled = true
        return ResultWrapper.Success(Unit)
    }
}
