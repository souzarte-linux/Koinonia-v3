package com.koinonia.igreja.presentation.features.reception

import androidx.work.TestWorkManager
import androidx.work.WorkManager
import com.koinonia.igreja.data.local.converter.EventType
import com.koinonia.igreja.data.local.converter.LocationType
import com.koinonia.igreja.data.local.dao.AttendanceDao
import com.koinonia.igreja.data.local.dao.EventDao
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.dao.VisitorDao
import com.koinonia.igreja.data.local.entity.AttendanceEntity
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.EventEntity
import com.koinonia.igreja.data.local.entity.FamilyEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import com.koinonia.igreja.data.local.entity.VisitorEntity
import com.koinonia.igreja.data.repository.AttendanceRepositoryImpl
import com.koinonia.igreja.domain.usecase.FinalizeEventUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
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
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class ReceptionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeMemberDao: FakeReceptionMemberDao
    private lateinit var fakeVisitorDao: FakeVisitorDao
    private lateinit var fakeEventDao: FakeEventDao
    private lateinit var fakeAttendanceDao: FakeAttendanceDao
    private lateinit var testWorkManager: WorkManager
    private lateinit var attendanceRepository: AttendanceRepositoryImpl
    private lateinit var finalizeEventUseCase: FinalizeEventUseCase
    private lateinit var viewModel: ReceptionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        fakeMemberDao = FakeReceptionMemberDao()
        fakeVisitorDao = FakeVisitorDao()
        fakeEventDao = FakeEventDao()
        fakeAttendanceDao = FakeAttendanceDao()
        testWorkManager = TestWorkManager()

        attendanceRepository = AttendanceRepositoryImpl(
            attendanceDao = fakeAttendanceDao,
            workManager = testWorkManager
        )

        finalizeEventUseCase = FinalizeEventUseCase(
            memberDao = fakeMemberDao,
            attendanceDao = fakeAttendanceDao
        )

        viewModel = ReceptionViewModel(
            memberDao = fakeMemberDao,
            visitorDao = fakeVisitorDao,
            eventDao = fakeEventDao,
            attendanceRepository = attendanceRepository,
            finalizeEventUseCase = finalizeEventUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initReception_withExplicitEventIdAndStartTime_setsCurrentEventIdAndTitle() = runTest {
        val startTime = ZonedDateTime.now(ZoneId.of("America/Bahia"))
        fakeEventDao.events.add(
            EventEntity(
                id = "event_100",
                title = "Culto de Leitura",
                type = EventType.ORDINARIO,
                startTime = Date(),
                endTime = Date(),
                locationType = LocationType.IGREJA_LOCAL,
                address = null,
                ministryId = null,
                creatorEmail = null
            )
        )

        viewModel.initReception("event_100", startTime)
        testScheduler.advanceUntilIdle()

        assertEquals("event_100", viewModel.currentEventId.value)
        assertEquals("Chamada: Culto de Leitura", viewModel.currentEventTitle.value)
    }

    @Test
    fun initReception_withNullEventId_resolvesTodayEvent() = runTest {
        val today = Date()
        fakeEventDao.events.add(
            EventEntity(
                id = "event_today",
                title = "Culto de Quarta",
                type = EventType.ORDINARIO,
                startTime = today,
                endTime = today,
                locationType = LocationType.IGREJA_LOCAL,
                address = null,
                ministryId = null,
                creatorEmail = null
            )
        )

        viewModel.initReception(null, null)
        testScheduler.advanceUntilIdle()

        assertEquals("event_today", viewModel.currentEventId.value)
    }

    @Test
    fun markPresence_marksMemberAsPresentInDao() = runTest {
        val startTime = ZonedDateTime.now(ZoneId.of("America/Bahia"))
        val member = MemberEntity(id = "m1", fullName = "João Silva")
        fakeMemberDao.members.add(member)

        viewModel.initReception("event_100", startTime)
        testScheduler.advanceUntilIdle()

        viewModel.markPresence(member)
        testScheduler.advanceUntilIdle()

        val savedAtt = fakeAttendanceDao.attendances.find { it.memberId == "m1" && it.eventId == "event_100" }
        assertNotNull(savedAtt)
        assertFalse(savedAtt!!.isAbsent)
    }

    @Test
    fun setAttendanceState_whenAbsent_marksMemberAsAbsentInDao() = runTest {
        val startTime = ZonedDateTime.now(ZoneId.of("America/Bahia"))
        val member = MemberEntity(id = "m1", fullName = "João Silva")
        fakeMemberDao.members.add(member)

        viewModel.initReception("event_100", startTime)
        testScheduler.advanceUntilIdle()

        viewModel.setAttendanceState(member, AttendanceStatus.AUSENTE)
        testScheduler.advanceUntilIdle()

        val savedAtt = fakeAttendanceDao.attendances.find { it.memberId == "m1" && it.eventId == "event_100" }
        assertNotNull(savedAtt)
        assertTrue(savedAtt!!.isAbsent)
    }

    @Test
    fun setAttendanceState_whenLate_calculatesLateMinutesAndMarksLate() = runTest {
        val startTime = ZonedDateTime.now(ZoneId.of("America/Bahia")).minusHours(1)
        val member = MemberEntity(id = "m1", fullName = "João Silva")
        fakeMemberDao.members.add(member)

        viewModel.initReception("event_100", startTime)
        testScheduler.advanceUntilIdle()

        viewModel.setAttendanceState(member, AttendanceStatus.ATRASADO)
        testScheduler.advanceUntilIdle()

        val savedAtt = fakeAttendanceDao.attendances.find { it.memberId == "m1" && it.eventId == "event_100" }
        assertNotNull(savedAtt)
        assertTrue(savedAtt!!.isLate)
        assertTrue(savedAtt.lateDurationMins > 0)
    }

    @Test
    fun setAttendanceState_whenNone_deletesAttendanceRecord() = runTest {
        val startTime = ZonedDateTime.now(ZoneId.of("America/Bahia"))
        val member = MemberEntity(id = "m1", fullName = "João Silva")
        fakeMemberDao.members.add(member)
        fakeAttendanceDao.attendances.add(
            AttendanceEntity(
                id = "att1",
                memberId = "m1",
                eventId = "event_100",
                arrivalTime = Date(),
                isAbsent = false,
                absenceReason = null,
                absenceReasonDetails = null,
                contactResponsible = null,
                contactMethod = null
            )
        )

        viewModel.initReception("event_100", startTime)
        testScheduler.advanceUntilIdle()

        viewModel.setAttendanceState(member, AttendanceStatus.NENHUM)
        testScheduler.advanceUntilIdle()

        val savedAtt = fakeAttendanceDao.attendances.find { it.memberId == "m1" && it.eventId == "event_100" }
        assertNull(savedAtt)
    }

    @Test
    fun setAttendanceState_whenMemberHasFamily_showsFamilyPopup() = runTest {
        val startTime = ZonedDateTime.now(ZoneId.of("America/Bahia"))
        val member1 = MemberEntity(id = "m1", familyId = "fam_1", fullName = "Pai Silva")
        val member2 = MemberEntity(id = "m2", familyId = "fam_1", fullName = "Filho Silva")
        fakeMemberDao.members.addAll(listOf(member1, member2))

        viewModel.initReception("event_100", startTime)
        testScheduler.advanceUntilIdle()

        viewModel.setAttendanceState(member1, AttendanceStatus.PONTUAL)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.showFamilyPopup.value)
        assertEquals(1, viewModel.currentFamilyMembers.value.size)
        assertEquals("Filho Silva", viewModel.currentFamilyMembers.value[0].member.fullName)
    }

    @Test
    fun manuallyFinalizeEvent_afterManualAbsence_doesNotDuplicateAttendance() = runTest {
        val startTime = ZonedDateTime.now(ZoneId.of("America/Bahia"))
        val member1 = MemberEntity(id = "m1", fullName = "João Silva")
        val member2 = MemberEntity(id = "m2", fullName = "Maria Santos")
        fakeMemberDao.members.addAll(listOf(member1, member2))

        viewModel.initReception("event_100", startTime)
        testScheduler.advanceUntilIdle()

        // Marca m1 manualmente como Ausente antes de finalizar
        viewModel.setAttendanceState(member1, AttendanceStatus.AUSENTE)
        testScheduler.advanceUntilIdle()

        // Garante que só existe 1 registro para m1
        val attBeforeFinalize = fakeAttendanceDao.attendances.filter { it.eventId == "event_100" }
        assertEquals(1, attBeforeFinalize.size)
        assertEquals("m1", attBeforeFinalize[0].memberId)

        // Executa a finalização manual do evento
        viewModel.manuallyFinalizeEvent()
        testScheduler.advanceUntilIdle()

        // Garante que o número total de registros de frequência para o evento é exatamente 2 (1 para m1, 1 para m2)
        val attAfterFinalize = fakeAttendanceDao.attendances.filter { it.eventId == "event_100" }
        assertEquals(2, attAfterFinalize.size)

        val m1Records = attAfterFinalize.filter { it.memberId == "m1" }
        assertEquals(1, m1Records.size)
        assertTrue(m1Records[0].isAbsent)

        val m2Records = attAfterFinalize.filter { it.memberId == "m2" }
        assertEquals(1, m2Records.size)
        assertTrue(m2Records[0].isAbsent)
    }

    @Test
    fun saveCustomAttendance_withVariousLateDurations_correctlyCalculatesDelayLevels() = runTest {
        val startTime = ZonedDateTime.now(ZoneId.of("America/Bahia"))
        val member = MemberEntity(id = "m1", fullName = "João Silva")
        fakeMemberDao.members.add(member)

        viewModel.initReception("event_100", startTime)
        testScheduler.advanceUntilIdle()

        // Salva atraso leve (10 min)
        viewModel.saveCustomAttendance(member, AttendanceStatus.ATRASADO, 19, 10, 10)
        testScheduler.advanceUntilIdle()

        var savedAtt = fakeAttendanceDao.attendances.find { it.memberId == "m1" && it.eventId == "event_100" }
        assertNotNull(savedAtt)
        assertEquals(10, savedAtt!!.lateDurationMins)

        // Salva atraso moderado (25 min)
        viewModel.saveCustomAttendance(member, AttendanceStatus.ATRASADO, 19, 25, 25)
        testScheduler.advanceUntilIdle()

        savedAtt = fakeAttendanceDao.attendances.find { it.memberId == "m1" && it.eventId == "event_100" }
        assertNotNull(savedAtt)
        assertEquals(25, savedAtt!!.lateDurationMins)

        // Salva atraso grave (45 min)
        viewModel.saveCustomAttendance(member, AttendanceStatus.ATRASADO, 19, 45, 45)
        testScheduler.advanceUntilIdle()

        savedAtt = fakeAttendanceDao.attendances.find { it.memberId == "m1" && it.eventId == "event_100" }
        assertNotNull(savedAtt)
        assertEquals(45, savedAtt!!.lateDurationMins)
    }

    @Test
    fun saveVisitor_insertsVisitorEntityInDao() = runTest {
        val startTime = ZonedDateTime.now(ZoneId.of("America/Bahia"))
        viewModel.initReception("event_100", startTime)
        testScheduler.advanceUntilIdle()

        viewModel.saveVisitor("Carlos Santos", "71988888888", true, "@carlos")
        testScheduler.advanceUntilIdle()

        assertEquals(1, fakeVisitorDao.visitors.size)
        assertEquals("Carlos Santos", fakeVisitorDao.visitors[0].name)
        assertEquals("event_100", fakeVisitorDao.visitors[0].eventId)
    }

    @Test
    fun startEditing_and_dismissEditing_updatesEditingState() {
        val member = MemberEntity(id = "m1", fullName = "João Silva")
        val state = MemberAttendanceState(member = member, isPresent = true, isAbsent = false, isLate = false)

        viewModel.startEditing(state)
        assertEquals(state, viewModel.editingMemberState.value)

        viewModel.dismissEditing()
        assertNull(viewModel.editingMemberState.value)
    }

    @Test
    fun saveCustomAttendance_savesCustomRecordAndDismissesDialog() = runTest {
        val startTime = ZonedDateTime.now(ZoneId.of("America/Bahia"))
        val member = MemberEntity(id = "m1", fullName = "João Silva")

        viewModel.initReception("event_100", startTime)
        testScheduler.advanceUntilIdle()

        viewModel.saveCustomAttendance(member, AttendanceStatus.ATRASADO, 19, 30, 25)
        testScheduler.advanceUntilIdle()

        val savedAtt = fakeAttendanceDao.attendances.find { it.memberId == "m1" && it.eventId == "event_100" }
        assertNotNull(savedAtt)
        assertTrue(savedAtt!!.isLate)
        assertEquals(25, savedAtt.lateDurationMins)
        assertNull(viewModel.editingMemberState.value)
    }

    @Test
    fun manuallyFinalizeEvent_invokesFinalizeEventUseCase() = runTest {
        val startTime = ZonedDateTime.now(ZoneId.of("America/Bahia"))
        viewModel.initReception("event_100", startTime)
        testScheduler.advanceUntilIdle()

        viewModel.manuallyFinalizeEvent()
        testScheduler.advanceUntilIdle()

        assertEquals("event_100", viewModel.currentEventId.value)
    }
}

class FakeReceptionMemberDao : MemberDao {
    val members = mutableListOf<MemberEntity>()

    override fun getAllMembers(): Flow<List<MemberEntity>> = flowOf(members)
    override suspend fun getMemberById(id: String): MemberEntity? = members.find { it.id == id }
    override suspend fun getMemberByEmail(email: String): MemberEntity? = members.find { it.email == email }
    override suspend fun getMemberByPhone(phone: String): MemberEntity? = members.find { it.phone == phone }
    override suspend fun insertMember(member: MemberEntity) { members.add(member) }
    override suspend fun insertMembers(membersList: List<MemberEntity>) { members.addAll(membersList) }
    override suspend fun deleteById(id: String) { members.removeAll { it.id == id } }
    override suspend fun getChildrenByMemberId(memberId: String): List<ChildEntity> = emptyList()
    override suspend fun getMinistryHistoryByMemberId(memberId: String): List<MinistryHistoryEntity> = emptyList()
    override fun getAllMinistryHistoriesFlow(): Flow<List<MinistryHistoryEntity>> = flowOf(emptyList())
    override suspend fun getFamilyMembers(familyId: String): List<MemberEntity> = members.filter { it.familyId == familyId }
    override suspend fun getPendingSyncMembers(): List<MemberEntity> = emptyList()
    override suspend fun markAsSynced(id: String) {}
    override suspend fun insertMinistryHistories(histories: List<MinistryHistoryEntity>) {}
}

class FakeVisitorDao : VisitorDao {
    val visitors = mutableListOf<VisitorEntity>()

    override suspend fun insertVisitor(visitor: VisitorEntity) {
        visitors.add(visitor)
    }

    override suspend fun getVisitorsForEvent(eventId: String): List<VisitorEntity> {
        return visitors.filter { it.eventId == eventId }
    }
}

class FakeEventDao : EventDao {
    val events = mutableListOf<EventEntity>()

    override suspend fun insertEvents(eventsList: List<EventEntity>) {
        events.addAll(eventsList)
    }

    override fun getAllEvents(): Flow<List<EventEntity>> = flowOf(events)
    override suspend fun getEventById(id: String): EventEntity? = events.find { it.id == id }
    override suspend fun insertOrUpdateEvent(event: EventEntity) {
        events.removeAll { it.id == event.id }
        events.add(event)
    }

    override suspend fun deleteById(eventId: String) {
        events.removeAll { it.id == eventId }
    }

    override suspend fun getOrdinaryEventsSync(): List<EventEntity> {
        return events.filter { it.type == EventType.ORDINARIO }
    }
}

class FakeAttendanceDao : AttendanceDao {
    val attendances = mutableListOf<AttendanceEntity>()

    override suspend fun insertAttendance(attendance: AttendanceEntity) {
        attendances.removeAll { it.memberId == attendance.memberId && it.eventId == attendance.eventId }
        attendances.add(attendance)
    }

    override suspend fun insertAttendances(attendancesList: List<AttendanceEntity>) {
        attendances.addAll(attendancesList)
    }

    override suspend fun updateAttendance(attendance: AttendanceEntity) {
        attendances.removeAll { it.id == attendance.id }
        attendances.add(attendance)
    }

    override fun getAttendanceForEvent(eventId: String): Flow<List<AttendanceEntity>> {
        return flowOf(attendances.filter { it.eventId == eventId })
    }

    override suspend fun getPendingSyncAttendances(): List<AttendanceEntity> = emptyList()
    override suspend fun markAsSynced(id: String) {}

    override suspend fun deleteAttendance(memberId: String, eventId: String) {
        attendances.removeAll { it.memberId == memberId && it.eventId == eventId }
    }
}
