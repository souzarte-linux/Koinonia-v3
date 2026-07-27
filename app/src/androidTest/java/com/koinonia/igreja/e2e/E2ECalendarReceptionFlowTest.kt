package com.koinonia.igreja.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koinonia.igreja.data.local.dao.AttendanceDao
import com.koinonia.igreja.data.local.dao.EventDao
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.dao.MinistryDao
import com.koinonia.igreja.data.local.dao.VisitorDao
import com.koinonia.igreja.data.local.entity.AttendanceEntity
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.EventEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import com.koinonia.igreja.data.local.entity.MinistryRoleEntity
import com.koinonia.igreja.data.local.entity.VisitorEntity
import com.koinonia.igreja.data.repository.AttendanceRepositoryImpl
import com.koinonia.igreja.data.repository.AuthRepositoryImpl
import com.koinonia.igreja.domain.usecase.FinalizeEventUseCase
import com.koinonia.igreja.domain.usecase.GenerateOrdinaryEventsUseCase
import com.koinonia.igreja.domain.usecase.GetMinistryDirectorshipsUseCase
import com.koinonia.igreja.presentation.features.calendar.CalendarScreen
import com.koinonia.igreja.presentation.features.calendar.CalendarViewModel
import com.koinonia.igreja.presentation.features.reception.ReceptionScreen
import com.koinonia.igreja.presentation.features.reception.ReceptionViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class E2ECalendarReceptionFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun e2e_calendarAndReceptionFlow_step1_rendersCalendarScreen() {
        val fakeEventDao = FakeEventDaoForE2E()
        val fakeMinistryDao = FakeMinistryDaoForE2E()
        val fakeMemberDao = FakeMemberDaoForE2E()

        val ordinaryUseCase = GenerateOrdinaryEventsUseCase(fakeEventDao)
        val supabase = createSupabaseClient("https://dummy.supabase.co", "dummy_key") {
            install(Auth)
        }
        val directorshipsUseCase = GetMinistryDirectorshipsUseCase(fakeMemberDao)
        val authRepo = AuthRepositoryImpl(
            supabaseClient = supabase,
            memberDao = { fakeMemberDao },
            getMinistryDirectorshipsUseCase = { directorshipsUseCase },
            applicationScope = GlobalScope
        )

        val calendarViewModel = CalendarViewModel(
            eventDao = fakeEventDao,
            ministryDao = fakeMinistryDao,
            generateOrdinaryEventsUseCase = ordinaryUseCase,
            authRepository = authRepo
        )

        composeTestRule.setContent {
            CalendarScreen(
                onBack = {},
                viewModel = calendarViewModel
            )
        }
        composeTestRule.onNodeWithText("Agenda").assertIsDisplayed()
    }

    @Test
    fun e2e_calendarAndReceptionFlow_step2_rendersReceptionScreen() {
        val fakeEventDao = FakeEventDaoForE2E()
        val fakeMemberDao = FakeMemberDaoForE2E()
        val fakeAttendanceDao = FakeAttendanceDaoForE2E()
        val fakeVisitorDao = FakeVisitorDaoForE2E()

        val workManager = androidx.work.TestWorkManager()
        val attendanceRepo = AttendanceRepositoryImpl(fakeAttendanceDao, workManager)
        val finalizeEventUseCase = FinalizeEventUseCase(fakeMemberDao, fakeAttendanceDao)

        val receptionViewModel = ReceptionViewModel(
            memberDao = fakeMemberDao,
            visitorDao = fakeVisitorDao,
            eventDao = fakeEventDao,
            attendanceRepository = attendanceRepo,
            finalizeEventUseCase = finalizeEventUseCase
        )

        composeTestRule.setContent {
            ReceptionScreen(
                onBack = {},
                viewModel = receptionViewModel
            )
        }
        composeTestRule.onNodeWithText("Buscar membro pelo nome...").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nenhum membro cadastrado.").assertIsDisplayed()
    }
}

class FakeEventDaoForE2E : EventDao {
    override suspend fun insertEvents(events: List<EventEntity>) {}
    override fun getAllEvents(): Flow<List<EventEntity>> = flowOf(emptyList())
    override suspend fun getEventById(id: String): EventEntity? = null
    override suspend fun insertOrUpdateEvent(event: EventEntity) {}
    override suspend fun deleteById(eventId: String) {}
    override suspend fun getOrdinaryEventsSync(): List<EventEntity> = emptyList()
}

class FakeMinistryDaoForE2E : MinistryDao {
    override suspend fun insertMinistry(ministry: MinistryEntity) {}
    override suspend fun insertMinistries(ministries: List<MinistryEntity>) {}
    override fun getAllMinistries(): Flow<List<MinistryEntity>> = flowOf(emptyList())
    override suspend fun getMinistryById(id: String): MinistryEntity? = null
    override suspend fun deleteMinistry(id: String) {}
    override suspend fun deleteAllMinistries() {}
    override suspend fun insertRole(role: MinistryRoleEntity) {}
    override suspend fun insertRoles(roles: List<MinistryRoleEntity>) {}
    override fun getAllRoles(): Flow<List<MinistryRoleEntity>> = flowOf(emptyList())
    override suspend fun deleteRole(id: String) {}
    override suspend fun deleteAllRoles() {}
}

class FakeMemberDaoForE2E : MemberDao {
    override fun getAllMembers(): Flow<List<MemberEntity>> = flowOf(emptyList())
    override suspend fun getMemberById(id: String): MemberEntity? = null
    override suspend fun getMemberByEmail(email: String): MemberEntity? = null
    override suspend fun getMemberByPhone(phone: String): MemberEntity? = null
    override suspend fun insertMember(member: MemberEntity) {}
    override suspend fun insertMembers(members: List<MemberEntity>) {}
    override suspend fun deleteById(id: String) {}
    override suspend fun getChildrenByMemberId(memberId: String): List<ChildEntity> = emptyList()
    override suspend fun getMinistryHistoryByMemberId(memberId: String): List<MinistryHistoryEntity> = emptyList()
    override fun getAllMinistryHistoriesFlow(): Flow<List<MinistryHistoryEntity>> = flowOf(emptyList())
    override suspend fun getFamilyMembers(familyId: String): List<MemberEntity> = emptyList()
    override suspend fun getPendingSyncMembers(): List<MemberEntity> = emptyList()
    override suspend fun markAsSynced(id: String) {}
    override suspend fun insertMinistryHistories(histories: List<MinistryHistoryEntity>) {}
}

class FakeAttendanceDaoForE2E : AttendanceDao {
    override suspend fun insertAttendance(attendance: AttendanceEntity) {}
    override suspend fun insertAttendances(attendances: List<AttendanceEntity>) {}
    override suspend fun updateAttendance(attendance: AttendanceEntity) {}
    override fun getAttendanceForEvent(eventId: String): Flow<List<AttendanceEntity>> = flowOf(emptyList())
    override suspend fun getPendingSyncAttendances(): List<AttendanceEntity> = emptyList()
    override suspend fun markAsSynced(id: String) {}
    override suspend fun deleteAttendance(memberId: String, eventId: String) {}
}

class FakeVisitorDaoForE2E : VisitorDao {
    override suspend fun insertVisitor(visitor: VisitorEntity) {}
    override suspend fun getVisitorsForEvent(eventId: String): List<VisitorEntity> = emptyList()
}
