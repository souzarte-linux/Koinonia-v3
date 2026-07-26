package com.koinonia.igreja.presentation.features.reports

import com.koinonia.igreja.data.local.dao.AttendanceDao
import com.koinonia.igreja.data.local.dao.AttendanceWithMemberInfo
import com.koinonia.igreja.data.local.dao.MemberAbsenceCount
import com.koinonia.igreja.data.local.dao.ReportsDao
import com.koinonia.igreja.data.local.entity.AttendanceEntity
import com.koinonia.igreja.domain.usecase.AnalyzeArrivalPeaksUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var fakeReportsDao: FakeReportsDaoForVM
    private lateinit var fakeAttendanceDao: FakeAttendanceDaoForReports
    private lateinit var analyzeArrivalPeaksUseCase: AnalyzeArrivalPeaksUseCase
    private lateinit var viewModel: ReportsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeReportsDao = FakeReportsDaoForVM()
        fakeAttendanceDao = FakeAttendanceDaoForReports()
        analyzeArrivalPeaksUseCase = AnalyzeArrivalPeaksUseCase(fakeAttendanceDao)

        viewModel = ReportsViewModel(
            reportsDao = fakeReportsDao,
            analyzeArrivalPeaksUseCase = analyzeArrivalPeaksUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasDefaultDashboardValuesAndEmptyPeaksAndContacts() {
        assertEquals(85.5, viewModel.attendanceRate.value, 0.001)
        assertEquals(120, viewModel.totalMembers.value)
        assertEquals(18, viewModel.absentCount.value)
        assertEquals(7, viewModel.visitorCountThisMonth.value)
        assertTrue(viewModel.arrivalPeaks.value.isEmpty())
        assertTrue(viewModel.pendingContacts.value.isEmpty())
    }

    @Test
    fun topAbsentMembers_exposesFlowFromReportsDao() = runTest {
        val absenceList = listOf(
            MemberAbsenceCount("m1", "João Silva", 10),
            MemberAbsenceCount("m2", "Maria Santos", 8)
        )
        fakeReportsDao.topAbsentFlow.value = absenceList

        val result = viewModel.topAbsentMembers.first()

        assertEquals(2, result.size)
        assertEquals("m1", result[0].memberId)
        assertEquals("João Silva", result[0].fullName)
        assertEquals(10, result[0].absenceCount)
    }

    @Test
    fun loadEventAnalytics_invokesUseCaseAndUpdateArrivalPeaksAndPendingContacts() = runTest {
        val zoneBahia = ZoneId.of("America/Bahia")
        val date1 = Date.from(ZonedDateTime.of(2026, 8, 20, 19, 5, 0, 0, zoneBahia).toInstant())
        val date2 = Date.from(ZonedDateTime.of(2026, 8, 20, 19, 12, 0, 0, zoneBahia).toInstant())

        fakeAttendanceDao.attendancesList.addAll(listOf(
            AttendanceEntity(id = "att1", memberId = "m1", eventId = "ev100", arrivalTime = date1, isAbsent = false, absenceReason = null, absenceReasonDetails = null, contactResponsible = null, contactMethod = null),
            AttendanceEntity(id = "att2", memberId = "m2", eventId = "ev100", arrivalTime = date2, isAbsent = false, absenceReason = null, absenceReasonDetails = null, contactResponsible = null, contactMethod = null)
        ))

        val pending = listOf(
            AttendanceWithMemberInfo("att3", "m3", "Carlos Souza", "71988889999", true, "Viagem")
        )
        fakeReportsDao.pendingContactsFlow.value = pending

        viewModel.loadEventAnalytics("ev100")
        testScheduler.advanceUntilIdle()

        assertEquals(2, viewModel.arrivalPeaks.value.size)
        assertTrue(viewModel.arrivalPeaks.value.containsKey("19:00"))
        assertTrue(viewModel.arrivalPeaks.value.containsKey("19:10"))

        assertEquals(1, viewModel.pendingContacts.value.size)
        assertEquals("Carlos Souza", viewModel.pendingContacts.value[0].fullName)
    }

    @Test
    fun loadEventAnalytics_whenNoPeaksOrPendingContacts_setsEmptyMapAndEmptyList() = runTest {
        fakeAttendanceDao.attendancesList.clear()
        fakeReportsDao.pendingContactsFlow.value = emptyList()

        viewModel.loadEventAnalytics("ev_empty")
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.arrivalPeaks.value.isEmpty())
        assertTrue(viewModel.pendingContacts.value.isEmpty())
    }

    @Test
    fun saveContactFollowUp_invokesUpdateAbsenceFollowUpOnReportsDao() = runTest {
        viewModel.saveContactFollowUp(
            attendanceId = "att_10",
            reason = "Doença",
            details = "Gripe forte",
            contactMethod = "WHATSAPP",
            responsibleId = "resp_1"
        )
        testScheduler.advanceUntilIdle()

        assertTrue(fakeReportsDao.updateFollowUpCalled)
        assertEquals("att_10", fakeReportsDao.lastAttendanceId)
        assertEquals("Doença", fakeReportsDao.lastReason)
        assertEquals("Gripe forte", fakeReportsDao.lastDetails)
        assertEquals("WHATSAPP", fakeReportsDao.lastContactMethod)
        assertEquals("resp_1", fakeReportsDao.lastResponsibleId)
    }

    @Test
    fun topAbsentMembers_propagatesMultipleEmissionsFromDao() = runTest {
        val list1 = listOf(MemberAbsenceCount("m1", "Membro 1", 5))
        fakeReportsDao.topAbsentFlow.value = list1

        var currentList = viewModel.topAbsentMembers.first()
        assertEquals(1, currentList.size)
        assertEquals("Membro 1", currentList[0].fullName)

        val list2 = listOf(
            MemberAbsenceCount("m1", "Membro 1", 5),
            MemberAbsenceCount("m2", "Membro 2", 3)
        )
        fakeReportsDao.topAbsentFlow.value = list2

        currentList = viewModel.topAbsentMembers.first()
        assertEquals(2, currentList.size)
    }

    @Test
    fun loadEventAnalytics_handlesMultipleEmissionsFromPendingContacts() = runTest {
        val initialPending = listOf(
            AttendanceWithMemberInfo("att1", "m1", "Ana Lima", "71999990000", true, null)
        )
        fakeReportsDao.pendingContactsFlow.value = initialPending

        viewModel.loadEventAnalytics("ev1")
        testScheduler.advanceUntilIdle()

        assertEquals(1, viewModel.pendingContacts.value.size)
        assertEquals("Ana Lima", viewModel.pendingContacts.value[0].fullName)

        fakeReportsDao.pendingContactsFlow.value = emptyList()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.pendingContacts.value.isEmpty())
    }
}

class FakeReportsDaoForVM : ReportsDao {
    val topAbsentFlow = MutableStateFlow<List<MemberAbsenceCount>>(emptyList())
    val pendingContactsFlow = MutableStateFlow<List<AttendanceWithMemberInfo>>(emptyList())

    var updateFollowUpCalled = false
    var lastAttendanceId: String? = null
    var lastReason: String? = null
    var lastDetails: String? = null
    var lastContactMethod: String? = null
    var lastResponsibleId: String? = null

    override fun getTopAbsentMembers(): Flow<List<MemberAbsenceCount>> = topAbsentFlow

    override fun getPendingContactsForEvent(eventId: String): Flow<List<AttendanceWithMemberInfo>> = pendingContactsFlow

    override suspend fun updateAbsenceFollowUp(
        attendanceId: String,
        reason: String,
        details: String?,
        contactMethod: String,
        responsibleId: String
    ) {
        updateFollowUpCalled = true
        lastAttendanceId = attendanceId
        lastReason = reason
        lastDetails = details
        lastContactMethod = contactMethod
        lastResponsibleId = responsibleId
    }
}

class FakeAttendanceDaoForReports : AttendanceDao {
    val attendancesList = mutableListOf<AttendanceEntity>()

    override suspend fun insertAttendance(attendance: AttendanceEntity) { attendancesList.add(attendance) }
    override suspend fun insertAttendances(attendances: List<AttendanceEntity>) { attendancesList.addAll(attendances) }
    override suspend fun updateAttendance(attendance: AttendanceEntity) {}
    override fun getAttendanceForEvent(eventId: String): Flow<List<AttendanceEntity>> = flowOf(attendancesList.filter { it.eventId == eventId })
    override suspend fun getPendingSyncAttendances(): List<AttendanceEntity> = emptyList()
    override suspend fun markAsSynced(id: String) {}
    override suspend fun deleteAttendance(memberId: String, eventId: String) {}
}
