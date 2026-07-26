package com.koinonia.igreja.presentation.features.calendar

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.koinonia.igreja.data.local.converter.EventType
import com.koinonia.igreja.data.local.converter.LocationType
import com.koinonia.igreja.data.local.dao.EventDao
import com.koinonia.igreja.data.local.dao.MinistryDao
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.EventEntity
import com.koinonia.igreja.data.local.entity.FamilyEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import com.koinonia.igreja.data.local.entity.MinistryRoleEntity
import com.koinonia.igreja.data.repository.AuthRepositoryImpl
import com.koinonia.igreja.domain.usecase.GenerateOrdinaryEventsUseCase
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
import kotlinx.coroutines.flow.Flow
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }
    private val applicationScope = CoroutineScope(testDispatcher + SupervisorJob() + exceptionHandler)

    private lateinit var supabaseClient: SupabaseClient
    private lateinit var fakeEventDao: FakeEventDaoForCalendarVM
    private lateinit var fakeMinistryDao: FakeMinistryDaoForCalendarVM
    private lateinit var realUseCase: GenerateOrdinaryEventsUseCase
    private lateinit var authRepository: AuthRepositoryImpl
    private lateinit var viewModel: CalendarViewModel

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

        fakeEventDao = FakeEventDaoForCalendarVM()
        fakeMinistryDao = FakeMinistryDaoForCalendarVM()
        realUseCase = GenerateOrdinaryEventsUseCase(fakeEventDao)

        val fakeMemberDao = FakeMemberDaoForCalendarAuth()
        val fakeDirectorshipsUseCase = GetMinistryDirectorshipsUseCase(fakeMemberDao)

        authRepository = AuthRepositoryImpl(
            supabaseClient = supabaseClient,
            memberDao = dagger.Lazy { fakeMemberDao },
            getMinistryDirectorshipsUseCase = dagger.Lazy { fakeDirectorshipsUseCase },
            applicationScope = applicationScope
        )

        viewModel = CalendarViewModel(
            eventDao = fakeEventDao,
            ministryDao = fakeMinistryDao,
            generateOrdinaryEventsUseCase = realUseCase,
            authRepository = authRepository
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
    fun init_executesGenerateOrdinaryEventsUseCase_andPopulatesOrdinaryEvents() = runTest {
        testScheduler.advanceUntilIdle()
        assertTrue(fakeEventDao.eventsList.isNotEmpty())
        assertTrue(fakeEventDao.eventsList.all { it.type == EventType.ORDINARIO })
    }

    @Test
    fun selectDate_updatesSelectedDateState() {
        val newDate = LocalDate.of(2026, 8, 15)
        viewModel.selectDate(newDate)

        assertEquals(newDate, viewModel.selectedDate.value)
    }

    @Test
    fun hasOrdinaryConflict_whenOverlappingOrdinaryEventExists_returnsTrue() = runTest {
        val testDate = LocalDate.of(2026, 8, 20)
        val zoneId = ZoneId.of("America/Bahia")
        val startZdt = ZonedDateTime.of(testDate, LocalTime.of(19, 0), zoneId)
        val endZdt = startZdt.plusHours(2)

        val ordinaryEvent = EventEntity(
            id = "ord_100",
            title = "Culto Ordinário",
            type = EventType.ORDINARIO,
            startTime = Date.from(startZdt.toInstant()),
            endTime = Date.from(endZdt.toInstant()),
            locationType = LocationType.IGREJA_LOCAL,
            address = null,
            ministryId = null,
            creatorEmail = null
        )
        fakeEventDao.eventsList.add(ordinaryEvent)

        val hasConflict = viewModel.hasOrdinaryConflict(testDate, "19:30")
        assertTrue(hasConflict)
    }

    @Test
    fun hasOrdinaryConflict_whenNoOverlappingEventExists_returnsFalse() = runTest {
        val testDate = LocalDate.of(2026, 8, 20)
        val zoneId = ZoneId.of("America/Bahia")
        val startZdt = ZonedDateTime.of(testDate, LocalTime.of(8, 0), zoneId)
        val endZdt = startZdt.plusHours(2)

        val ordinaryEvent = EventEntity(
            id = "ord_100",
            title = "Culto Ordinário",
            type = EventType.ORDINARIO,
            startTime = Date.from(startZdt.toInstant()),
            endTime = Date.from(endZdt.toInstant()),
            locationType = LocationType.IGREJA_LOCAL,
            address = null,
            ministryId = null,
            creatorEmail = null
        )
        fakeEventDao.eventsList.add(ordinaryEvent)

        val hasConflict = viewModel.hasOrdinaryConflict(testDate, "15:00")
        assertFalse(hasConflict)
    }

    @Test
    fun hasOrdinaryConflict_whenEventIdToIgnoreMatches_ignoresSelfAndReturnsFalse() = runTest {
        val testDate = LocalDate.of(2026, 8, 20)
        val zoneId = ZoneId.of("America/Bahia")
        val startZdt = ZonedDateTime.of(testDate, LocalTime.of(19, 0), zoneId)
        val endZdt = startZdt.plusHours(2)

        val ordinaryEvent = EventEntity(
            id = "ord_100",
            title = "Culto Ordinário",
            type = EventType.ORDINARIO,
            startTime = Date.from(startZdt.toInstant()),
            endTime = Date.from(endZdt.toInstant()),
            locationType = LocationType.IGREJA_LOCAL,
            address = null,
            ministryId = null,
            creatorEmail = null
        )
        fakeEventDao.eventsList.add(ordinaryEvent)

        val hasConflict = viewModel.hasOrdinaryConflict(testDate, "19:00", eventIdToIgnore = "ord_100")
        assertFalse(hasConflict)
    }

    @Test
    fun addEvent_createsEventEntityAndInsertsIntoDao() = runTest {
        val date = LocalDate.of(2026, 9, 10)
        val time = "19:30"

        viewModel.addEvent(
            title = "Reunião de Jovens",
            date = date,
            time = time,
            type = EventType.REUNIAO,
            locationType = LocationType.IGREJA_LOCAL,
            address = "Rua Principal 123",
            ministryId = "mja"
        )
        testScheduler.advanceUntilIdle()

        val inserted = fakeEventDao.eventsList.find { it.title == "Reunião de Jovens" }
        assertNotNull(inserted)
        assertEquals("Reunião de Jovens", inserted?.title)
        assertEquals(EventType.REUNIAO, inserted?.type)
        assertEquals(LocationType.IGREJA_LOCAL, inserted?.locationType)
        assertEquals("Rua Principal 123", inserted?.address)
        assertEquals("mja", inserted?.ministryId)
        assertTrue(inserted?.syncPending == true)
    }

    @Test
    fun editEvent_updatesExistingEventEntityInDao() = runTest {
        val originalDate = LocalDate.of(2026, 9, 10)
        viewModel.addEvent(
            title = "Evento Antigo",
            date = originalDate,
            time = "18:00",
            type = EventType.EXTRAORDINARIO,
            locationType = LocationType.IGREJA_LOCAL
        )
        testScheduler.advanceUntilIdle()

        val originalEvent = fakeEventDao.eventsList.find { it.title == "Evento Antigo" }!!

        val newDate = LocalDate.of(2026, 9, 15)
        viewModel.editEvent(
            id = originalEvent.id,
            title = "Evento Editado",
            date = newDate,
            time = "20:00",
            type = EventType.EXTRAORDINARIO,
            locationType = LocationType.EXTERNO,
            address = "Praça Central",
            ministryId = "musica"
        )
        testScheduler.advanceUntilIdle()

        val updated = fakeEventDao.eventsList.find { it.id == originalEvent.id }
        assertNotNull(updated)
        assertEquals("Evento Editado", updated?.title)
        assertEquals(LocationType.EXTERNO, updated?.locationType)
        assertEquals("Praça Central", updated?.address)
        assertEquals("musica", updated?.ministryId)
    }

    @Test
    fun deleteEvent_removesEventFromDao() = runTest {
        val date = LocalDate.of(2026, 9, 10)
        viewModel.addEvent(
            title = "Evento Para Deletar",
            date = date,
            time = "18:00",
            type = EventType.ORDINARIO,
            locationType = LocationType.IGREJA_LOCAL
        )
        testScheduler.advanceUntilIdle()

        val eventToDel = fakeEventDao.eventsList.find { it.title == "Evento Para Deletar" }!!
        viewModel.deleteEvent(eventToDel.id)
        testScheduler.advanceUntilIdle()

        assertFalse(fakeEventDao.eventsList.any { it.id == eventToDel.id })
    }

    @Test
    fun timeZone_correctlyConvertsDatesInAmericaBahiaTimeZone() = runTest {
        val testDate = LocalDate.of(2026, 12, 25)
        val timeString = "19:00"

        viewModel.addEvent(
            title = "Culto de Natal Especial",
            date = testDate,
            time = timeString,
            type = EventType.ORDINARIO,
            locationType = LocationType.IGREJA_LOCAL
        )
        testScheduler.advanceUntilIdle()

        val inserted = fakeEventDao.eventsList.find { it.title == "Culto de Natal Especial" }!!
        val zoneBahia = ZoneId.of("America/Bahia")
        val startZdt = inserted.startTime.toInstant().atZone(zoneBahia)

        assertEquals(2026, startZdt.year)
        assertEquals(12, startZdt.monthValue)
        assertEquals(25, startZdt.dayOfMonth)
        assertEquals(19, startZdt.hour)
        assertEquals(0, startZdt.minute)
    }
}

class FakeEventDaoForCalendarVM : EventDao {
    val eventsList = mutableListOf<EventEntity>()

    override suspend fun insertEvents(events: List<EventEntity>) { eventsList.addAll(events) }
    override fun getAllEvents(): Flow<List<EventEntity>> = flowOf(eventsList)
    override suspend fun getEventById(id: String): EventEntity? = eventsList.find { it.id == id }
    override suspend fun insertOrUpdateEvent(event: EventEntity) {
        eventsList.removeAll { it.id == event.id }
        eventsList.add(event)
    }
    override suspend fun deleteById(eventId: String) { eventsList.removeAll { it.id == eventId } }
    override suspend fun getOrdinaryEventsSync(): List<EventEntity> = eventsList.filter { it.type == EventType.ORDINARIO }
}

class FakeMinistryDaoForCalendarVM : MinistryDao {
    val ministriesList = mutableListOf<MinistryEntity>()
    val rolesList = mutableListOf<MinistryRoleEntity>()

    override suspend fun insertMinistry(ministry: MinistryEntity) { ministriesList.add(ministry) }
    override suspend fun insertMinistries(ministries: List<MinistryEntity>) { ministriesList.addAll(ministries) }
    override fun getAllMinistries(): Flow<List<MinistryEntity>> = flowOf(ministriesList)
    override suspend fun getMinistryById(id: String): MinistryEntity? = ministriesList.find { it.id == id }
    override suspend fun deleteMinistry(id: String) { ministriesList.removeAll { it.id == id } }
    override suspend fun deleteAllMinistries() { ministriesList.clear() }

    override suspend fun insertRole(role: MinistryRoleEntity) { rolesList.add(role) }
    override suspend fun insertRoles(roles: List<MinistryRoleEntity>) { rolesList.addAll(roles) }
    override fun getAllRoles(): Flow<List<MinistryRoleEntity>> = flowOf(rolesList)
    override suspend fun deleteRole(id: String) { rolesList.removeAll { it.id == id } }
    override suspend fun deleteAllRoles() { rolesList.clear() }
}

class FakeMemberDaoForCalendarAuth : com.koinonia.igreja.data.local.dao.MemberDao {
    val members = mutableListOf<MemberEntity>()
    val ministryHistories = mutableListOf<MinistryHistoryEntity>()

    override fun getAllMembers(): Flow<List<MemberEntity>> = flowOf(members)
    override suspend fun getMemberById(id: String): MemberEntity? = members.find { it.id == id }
    override suspend fun getMemberByEmail(email: String): MemberEntity? = members.find { it.email == email }
    override suspend fun getMemberByPhone(phone: String): MemberEntity? = members.find { it.phone == phone }
    override suspend fun insertMember(member: MemberEntity) { members.add(member) }
    override suspend fun insertMembers(membersList: List<MemberEntity>) { members.addAll(membersList) }
    override suspend fun deleteById(id: String) { members.removeAll { it.id == id } }
    override suspend fun getChildrenByMemberId(memberId: String): List<ChildEntity> = emptyList()
    override suspend fun getMinistryHistoryByMemberId(memberId: String): List<MinistryHistoryEntity> = ministryHistories.filter { it.memberId == memberId }
    override fun getAllMinistryHistoriesFlow(): Flow<List<MinistryHistoryEntity>> = flowOf(ministryHistories)
    override suspend fun getFamilyMembers(familyId: String): List<MemberEntity> = members.filter { it.familyId == familyId }
    override suspend fun getPendingSyncMembers(): List<MemberEntity> = emptyList()
    override suspend fun markAsSynced(id: String) {}
    override suspend fun insertMinistryHistories(histories: List<MinistryHistoryEntity>) { ministryHistories.addAll(histories) }
}
