package com.koinonia.igreja.presentation.features.calendar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koinonia.igreja.data.local.dao.EventDao
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.dao.MinistryDao
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.EventEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import com.koinonia.igreja.data.local.entity.MinistryRoleEntity
import com.koinonia.igreja.data.repository.AuthRepositoryImpl
import com.koinonia.igreja.domain.usecase.GenerateOrdinaryEventsUseCase
import com.koinonia.igreja.domain.usecase.GetMinistryDirectorshipsUseCase
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun calendarScreen_rendersHeaderAndCalendarView() {
        val fakeEventDao = FakeEventDaoForCalendar()
        val fakeMinistryDao = FakeMinistryDaoForCalendar()
        val fakeMemberDao = FakeMemberDaoForCalendar()
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

        val viewModel = CalendarViewModel(
            eventDao = fakeEventDao,
            ministryDao = fakeMinistryDao,
            generateOrdinaryEventsUseCase = ordinaryUseCase,
            authRepository = authRepo
        )

        composeTestRule.setContent {
            CalendarScreen(
                onBack = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Agenda").assertIsDisplayed()
    }
}

class FakeEventDaoForCalendar : EventDao {
    override suspend fun insertEvents(events: List<EventEntity>) {}
    override fun getAllEvents(): Flow<List<EventEntity>> = flowOf(emptyList())
    override suspend fun getEventById(id: String): EventEntity? = null
    override suspend fun insertOrUpdateEvent(event: EventEntity) {}
    override suspend fun deleteById(eventId: String) {}
    override suspend fun getOrdinaryEventsSync(): List<EventEntity> = emptyList()
}

class FakeMinistryDaoForCalendar : MinistryDao {
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

class FakeMemberDaoForCalendar : MemberDao {
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
