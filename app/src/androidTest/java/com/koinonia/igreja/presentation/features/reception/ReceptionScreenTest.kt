package com.koinonia.igreja.presentation.features.reception

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koinonia.igreja.data.local.dao.AttendanceDao
import com.koinonia.igreja.data.local.dao.EventDao
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.dao.VisitorDao
import com.koinonia.igreja.data.local.entity.AttendanceEntity
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.EventEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import com.koinonia.igreja.data.local.entity.VisitorEntity
import com.koinonia.igreja.data.repository.AttendanceRepositoryImpl
import com.koinonia.igreja.domain.usecase.FinalizeEventUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReceptionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun receptionScreen_rendersTitleAndSearchBar() {
        val fakeMemberDao = FakeMemberDaoForReception()
        val fakeAttendanceDao = FakeAttendanceDaoForReception()
        val fakeVisitorDao = FakeVisitorDaoForReception()
        val fakeEventDao = FakeEventDaoForReception()

        val workManager = androidx.work.TestWorkManager()
        val attendanceRepo = AttendanceRepositoryImpl(fakeAttendanceDao, workManager)
        val finalizeEventUseCase = FinalizeEventUseCase(fakeMemberDao, fakeAttendanceDao)

        val viewModel = ReceptionViewModel(
            memberDao = fakeMemberDao,
            visitorDao = fakeVisitorDao,
            eventDao = fakeEventDao,
            attendanceRepository = attendanceRepo,
            finalizeEventUseCase = finalizeEventUseCase
        )

        composeTestRule.setContent {
            ReceptionScreen(
                onBack = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Buscar membro pelo nome...").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nenhum membro cadastrado.").assertIsDisplayed()
    }
}

class FakeMemberDaoForReception : MemberDao {
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

class FakeAttendanceDaoForReception : AttendanceDao {
    override suspend fun insertAttendance(attendance: AttendanceEntity) {}
    override suspend fun insertAttendances(attendances: List<AttendanceEntity>) {}
    override suspend fun updateAttendance(attendance: AttendanceEntity) {}
    override fun getAttendanceForEvent(eventId: String): Flow<List<AttendanceEntity>> = flowOf(emptyList())
    override suspend fun getPendingSyncAttendances(): List<AttendanceEntity> = emptyList()
    override suspend fun markAsSynced(id: String) {}
    override suspend fun deleteAttendance(memberId: String, eventId: String) {}
}

class FakeVisitorDaoForReception : VisitorDao {
    override suspend fun insertVisitor(visitor: VisitorEntity) {}
    override suspend fun getVisitorsForEvent(eventId: String): List<VisitorEntity> = emptyList()
}

class FakeEventDaoForReception : EventDao {
    override suspend fun insertEvents(events: List<EventEntity>) {}
    override fun getAllEvents(): Flow<List<EventEntity>> = flowOf(emptyList())
    override suspend fun getEventById(id: String): EventEntity? = null
    override suspend fun insertOrUpdateEvent(event: EventEntity) {}
    override suspend fun deleteById(eventId: String) {}
    override suspend fun getOrdinaryEventsSync(): List<EventEntity> = emptyList()
}
