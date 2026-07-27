package com.koinonia.igreja.presentation.features.members

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.work.TestWorkManager
import com.koinonia.igreja.core.util.ResultWrapper
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.dao.MemberRegistrationDao
import com.koinonia.igreja.data.local.dao.MinistryDao
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.FamilyEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import com.koinonia.igreja.data.local.entity.MinistryRoleEntity
import com.koinonia.igreja.data.repository.AuthRepositoryImpl
import com.koinonia.igreja.domain.model.Member
import com.koinonia.igreja.domain.model.MinistryPositionTier
import com.koinonia.igreja.domain.repository.MemberRepository
import com.koinonia.igreja.domain.usecase.GetMinistryDirectorshipsUseCase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class MemberRegistrationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }
    private val applicationScope = CoroutineScope(testDispatcher + SupervisorJob() + exceptionHandler)

    private lateinit var supabaseClient: SupabaseClient
    private lateinit var fakeRegistrationDao: FakeMemberRegistrationDao
    private lateinit var fakeMemberDao: FakeMemberDaoForRegVM
    private lateinit var fakeMinistryDao: FakeMinistryDaoForRegVM
    private lateinit var fakeRepository: FakeMemberRepositoryForRegVM
    private lateinit var testWorkManager: TestWorkManager
    private lateinit var authRepository: AuthRepositoryImpl
    private lateinit var viewModel: MemberRegistrationViewModel

    @Before
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) { runnable.run() }
            override fun postToMainThread(runnable: Runnable) { runnable.run() }
            override fun isMainThread(): Boolean = true
        })

        Dispatchers.setMain(testDispatcher)

        supabaseClient = createSupabaseClient(
            supabaseUrl = "https://wpgplnsopcqoldqalhrq.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummy"
        ) {
            coroutineDispatcher = testDispatcher
            install(Auth) {
                sessionManager = MemorySessionManager()
                codeVerifierCache = MemoryCodeVerifierCache()
                autoLoadFromStorage = false
                alwaysAutoRefresh = false
            }
        }

        fakeRegistrationDao = FakeMemberRegistrationDao()
        fakeMemberDao = FakeMemberDaoForRegVM()
        fakeMinistryDao = FakeMinistryDaoForRegVM()
        fakeRepository = FakeMemberRepositoryForRegVM()
        testWorkManager = TestWorkManager()

        val fakeDirectorshipsUseCase = GetMinistryDirectorshipsUseCase(fakeMemberDao)

        authRepository = AuthRepositoryImpl(
            supabaseClient = supabaseClient,
            memberDao = dagger.Lazy { fakeMemberDao },
            getMinistryDirectorshipsUseCase = dagger.Lazy { fakeDirectorshipsUseCase },
            applicationScope = applicationScope
        )

        viewModel = MemberRegistrationViewModel(
            registrationDao = fakeRegistrationDao,
            memberDao = fakeMemberDao,
            ministryDao = fakeMinistryDao,
            authRepository = authRepository,
            memberRepository = fakeRepository,
            workManager = testWorkManager
        )
    }

    @After
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
        applicationScope.coroutineContext.cancelChildren()
        runBlocking {
            supabaseClient.close()
        }
        Dispatchers.resetMain()
    }

    @Test
    fun loadMemberToEdit_populatesAllFormFieldsChildrenAndMinistries() = runTest {
        val member = MemberEntity(
            id = "m1",
            fullName = "Carlos Eduardo Santos",
            photoUrl = "https://photo.jpg",
            birthDate = Date(),
            civilStatus = "Casado(a)",
            rg = "12345678",
            cpf = "98765432100",
            spouseId = "m2",
            spouseName = "Fernanda Santos",
            phone = "5571988880001",
            isWhatsapp = true,
            socialMedia = "@carlos",
            email = "carlos@koinonia.app",
            cep = "40000-000",
            street = "Avenida Paralela",
            number = "100",
            neighborhood = "Alphaville",
            city = "Salvador",
            state = "BA",
            complement = "Apto 101",
            baptismDate = Date(),
            rebaptismDate = null,
            hasVehicle = true,
            vehicleType = "CARRO",
            vehicleModel = "Civic",
            familyId = "fam_1"
        )
        val child = ChildEntity(
            id = "c1",
            memberId = "m1",
            fullName = "Lucas Santos",
            gender = "Masculino",
            isBaptized = false,
            birthDate = Date()
        )
        val history = MinistryHistoryEntity(
            id = "h1",
            memberId = "m1",
            ministryId = "mja",
            ministryName = "MJA",
            role = "Diretor(a)",
            startDate = Date(),
            endDate = null
        )

        fakeMemberDao.members.add(member)
        fakeMemberDao.children.add(child)
        fakeMemberDao.ministryHistories.add(history)

        viewModel.loadMemberToEdit("m1")
        testScheduler.advanceUntilIdle()

        assertEquals("m1", viewModel.editingMemberId.value)
        assertEquals("Carlos Eduardo Santos", viewModel.fullName.value)
        assertEquals("carlos@koinonia.app", viewModel.email.value)
        assertEquals("5571988880001", viewModel.phone.value)
        assertEquals("40000-000", viewModel.cep.value)
        assertEquals("Avenida Paralela", viewModel.street.value)
        assertEquals("Salvador", viewModel.city.value)
        assertEquals("BA", viewModel.state.value)
        assertFalse(viewModel.isNewFamily.value)
        assertEquals("fam_1", viewModel.selectedFamilyId.value)
        assertEquals(1, viewModel.children.value.size)
        assertEquals("Lucas Santos", viewModel.children.value[0].fullName)
        assertEquals(1, viewModel.ministryRoles.value.size)
        assertEquals("MJA", viewModel.ministryRoles.value[0].ministryName)
    }

    @Test
    fun addChild_incrementsChildrenListSize() {
        assertEquals(0, viewModel.children.value.size)
        viewModel.addChild()
        assertEquals(1, viewModel.children.value.size)
        viewModel.addChild()
        assertEquals(2, viewModel.children.value.size)
    }

    @Test
    fun removeChild_removesChildAtSpecifiedIndex() {
        viewModel.addChild()
        viewModel.addChild()
        assertEquals(2, viewModel.children.value.size)

        viewModel.removeChild(0)
        assertEquals(1, viewModel.children.value.size)
    }

    @Test
    fun updateChild_replacesChildStateAtSpecifiedIndex() {
        viewModel.addChild()
        val originalChild = viewModel.children.value[0]
        val updatedChild = originalChild.copy(fullName = "Gabriel Santos", gender = "Masculino")

        viewModel.updateChild(0, updatedChild)
        assertEquals("Gabriel Santos", viewModel.children.value[0].fullName)
    }

    @Test
    fun addMinistryRole_incrementsMinistryRolesListSize() {
        assertEquals(0, viewModel.ministryRoles.value.size)
        viewModel.addMinistryRole()
        assertEquals(1, viewModel.ministryRoles.value.size)
    }

    @Test
    fun removeMinistryRole_removesRoleAtSpecifiedIndex() {
        viewModel.addMinistryRole()
        viewModel.addMinistryRole()
        assertEquals(2, viewModel.ministryRoles.value.size)

        viewModel.removeMinistryRole(0)
        assertEquals(1, viewModel.ministryRoles.value.size)
    }

    @Test
    fun updateMinistryRole_replacesRoleStateAtSpecifiedIndex() {
        viewModel.addMinistryRole()
        val original = viewModel.ministryRoles.value[0]
        val updated = original.copy(ministryName = "Desbravadores", role = "Conselheiro(a)")

        viewModel.updateMinistryRole(0, updated)
        assertEquals("Desbravadores", viewModel.ministryRoles.value[0].ministryName)
        assertEquals("Conselheiro(a)", viewModel.ministryRoles.value[0].role)
    }

    @Test
    fun addMinistry_and_deleteMinistry_interactWithMinistryDao() = runTest {
        viewModel.addMinistry(
            name = "Novo Ministerio",
            parentId = null,
            minAge = 12,
            maxAge = 18,
            minMembershipMonths = null,
            notes = "Test"
        )

        repeat(50) {
            if (fakeMinistryDao.ministriesList.any { it.name == "Novo Ministerio" }) return@repeat
            delay(10)
        }

        assertTrue(fakeMinistryDao.ministriesList.any { it.name == "Novo Ministerio" })

        val added = fakeMinistryDao.ministriesList.find { it.name == "Novo Ministerio" }!!
        viewModel.deleteMinistry(added.id)

        repeat(50) {
            if (!fakeMinistryDao.ministriesList.any { it.id == added.id }) return@repeat
            delay(10)
        }

        assertFalse(fakeMinistryDao.ministriesList.any { it.id == added.id })
    }

    @Test
    fun addRole_and_deleteRole_interactWithMinistryDao() = runTest {
        viewModel.addRole("Novo Cargo", MinistryPositionTier.SUPPORT)

        repeat(50) {
            if (fakeMinistryDao.rolesList.any { it.title == "Novo Cargo" }) return@repeat
            delay(10)
        }

        assertTrue(fakeMinistryDao.rolesList.any { it.title == "Novo Cargo" })

        val addedRole = fakeMinistryDao.rolesList.find { it.title == "Novo Cargo" }!!
        viewModel.deleteRole(addedRole.id)

        repeat(50) {
            if (!fakeMinistryDao.rolesList.any { it.id == addedRole.id }) return@repeat
            delay(10)
        }

        assertFalse(fakeMinistryDao.rolesList.any { it.id == addedRole.id })
    }

    @Test
    fun resetToDefaultMinistriesAndRoles_populatesDefaultMinistriesAndRoles() = runTest {
        viewModel.resetToDefaultMinistriesAndRoles()

        repeat(50) {
            if (fakeMinistryDao.ministriesList.isNotEmpty() && fakeMinistryDao.rolesList.isNotEmpty()) return@repeat
            delay(10)
        }

        assertTrue(fakeMinistryDao.ministriesList.isNotEmpty())
        assertTrue(fakeMinistryDao.rolesList.isNotEmpty())
        assertTrue(fakeMinistryDao.ministriesList.any { it.id == "desbravadores" })
        assertTrue(fakeMinistryDao.rolesList.any { it.id == "role_anciao" })
    }

    @Test
    fun loadFamilies_fetchesFamiliesFromRegistrationDao() = runTest {
        val family1 = FamilyEntity(id = "fam_1", name = "Família Silva")
        val family2 = FamilyEntity(id = "fam_2", name = "Família Costa")
        fakeRegistrationDao.families.addAll(listOf(family1, family2))

        viewModel.loadFamilies()
        testScheduler.advanceUntilIdle()

        assertEquals(2, viewModel.families.value.size)
        assertEquals("Família Silva", viewModel.families.value[0].name)
    }

    @Test
    fun saveMember_persistsFullMemberAndUpdatesIsSavedState() = runTest {
        viewModel.fullName.value = "Mariana Castro"
        viewModel.email.value = "mariana@test.com"
        viewModel.phone.value = "71988889999"
        viewModel.isNewFamily.value = true
        viewModel.familyNameInput.value = "Família Castro"

        viewModel.saveMember()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.isSaved.value)
        assertTrue(fakeRegistrationDao.registerFullMemberCalled)
        assertNotNull(fakeRegistrationDao.lastSavedMember)
        assertEquals("Mariana Castro", fakeRegistrationDao.lastSavedMember?.fullName)
        assertEquals("mariana@test.com", fakeRegistrationDao.lastSavedMember?.email)
        assertNotNull(fakeRegistrationDao.lastSavedFamily)
        assertEquals("Família Castro", fakeRegistrationDao.lastSavedFamily?.name)
    }

    @Test
    fun saveMember_withChildrenAndMinistryRoles_persistsEntitiesInDao() = runTest {
        viewModel.fullName.value = "Patricia Gomes"
        viewModel.isNewFamily.value = false
        viewModel.selectedFamilyId.value = "fam_10"

        viewModel.addChild()
        viewModel.updateChild(0, ChildUiState(fullName = "Filho 1", gender = "Masculino", isBaptized = false, birthDate = Date()))

        viewModel.addMinistryRole()
        viewModel.updateMinistryRole(0, MinistryHistoryUiState(ministryId = "mja", ministryName = "MJA", role = "Líder"))

        viewModel.saveMember()
        testScheduler.advanceUntilIdle()

        assertTrue(fakeRegistrationDao.registerFullMemberCalled)
        assertEquals(1, fakeRegistrationDao.lastSavedChildren.size)
        assertEquals("Filho 1", fakeRegistrationDao.lastSavedChildren[0].fullName)
        assertEquals(1, fakeRegistrationDao.lastSavedMinistryHistory.size)
        assertEquals("MJA", fakeRegistrationDao.lastSavedMinistryHistory[0].ministryName)
    }

    @Test
    fun resetState_resetsFormFieldsToDefaultInitialValues() = runTest {
        viewModel.fullName.value = "Nome Alterado"
        viewModel.email.value = "alterado@test.com"
        viewModel.phone.value = "71999998888"
        viewModel.addChild()
        viewModel.addMinistryRole()

        viewModel.resetState()
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.editingMemberId.value)
        assertEquals("", viewModel.fullName.value)
        assertEquals("", viewModel.email.value)
        assertEquals("", viewModel.phone.value)
        assertTrue(viewModel.children.value.isEmpty())
        assertTrue(viewModel.ministryRoles.value.isEmpty())
        assertFalse(viewModel.isSaved.value)
    }
}

class FakeMemberRegistrationDao : MemberRegistrationDao {
    val families = mutableListOf<FamilyEntity>()
    var registerFullMemberCalled = false
    var lastSavedFamily: FamilyEntity? = null
    var lastSavedMember: MemberEntity? = null
    var lastSavedChildren: List<ChildEntity> = emptyList()
    var lastSavedMinistryHistory: List<MinistryHistoryEntity> = emptyList()

    override suspend fun getAllFamilies(): List<FamilyEntity> = families

    override suspend fun insertFamily(family: FamilyEntity) {
        families.add(family)
    }

    override suspend fun insertMember(member: MemberEntity) {}
    override suspend fun insertChildren(children: List<ChildEntity>) {}
    override suspend fun insertMinistryHistory(history: List<MinistryHistoryEntity>) {}
    override suspend fun deleteChildrenByMemberId(memberId: String) {}
    override suspend fun deleteMinistryHistoryByMemberId(memberId: String) {}

    override suspend fun registerFullMember(
        newFamily: FamilyEntity?,
        member: MemberEntity,
        children: List<ChildEntity>,
        ministryHistory: List<MinistryHistoryEntity>,
        isEdit: Boolean
    ) {
        registerFullMemberCalled = true
        lastSavedFamily = newFamily
        lastSavedMember = member
        lastSavedChildren = children
        lastSavedMinistryHistory = ministryHistory
    }
}

class FakeMemberDaoForRegVM : MemberDao {
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

class FakeMinistryDaoForRegVM : MinistryDao {
    val ministriesList = java.util.concurrent.CopyOnWriteArrayList<MinistryEntity>()
    val rolesList = java.util.concurrent.CopyOnWriteArrayList<MinistryRoleEntity>()

    override suspend fun insertMinistry(ministry: MinistryEntity) { ministriesList.add(ministry) }
    override suspend fun insertMinistries(ministries: List<MinistryEntity>) { ministriesList.addAll(ministries) }
    override fun getAllMinistries(): Flow<List<MinistryEntity>> = flowOf(ministriesList.toList())
    override suspend fun getMinistryById(id: String): MinistryEntity? = ministriesList.find { it.id == id }
    override suspend fun deleteMinistry(id: String) { ministriesList.removeAll { it.id == id } }
    override suspend fun deleteAllMinistries() { ministriesList.clear() }

    override suspend fun insertRole(role: MinistryRoleEntity) { rolesList.add(role) }
    override suspend fun insertRoles(roles: List<MinistryRoleEntity>) { rolesList.addAll(roles) }
    override fun getAllRoles(): Flow<List<MinistryRoleEntity>> = flowOf(rolesList.toList())
    override suspend fun deleteRole(id: String) { rolesList.removeAll { it.id == id } }
    override suspend fun deleteAllRoles() { rolesList.clear() }
}

class FakeMemberRepositoryForRegVM : MemberRepository {
    val membersFlow = MutableStateFlow<List<Member>>(emptyList())

    override fun getMembersStream(): Flow<List<Member>> = membersFlow
    override suspend fun getMemberById(id: String): Member? = membersFlow.value.find { it.id == id }
    override suspend fun saveMember(member: Member) {}
    override suspend fun deleteMember(id: String) {}
    override suspend fun syncWithRemote(): ResultWrapper<Unit> = ResultWrapper.Success(Unit)
}
