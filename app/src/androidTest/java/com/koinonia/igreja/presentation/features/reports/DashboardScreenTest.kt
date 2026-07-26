package com.koinonia.igreja.presentation.features.reports

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koinonia.igreja.data.local.dao.AttendanceWithMemberInfo
import com.koinonia.igreja.data.local.dao.MemberAbsenceCount
import com.koinonia.igreja.data.local.dao.ReportsDao
import com.koinonia.igreja.domain.usecase.AnalyzeArrivalPeaksUseCase
import com.koinonia.igreja.data.local.dao.AttendanceDao
import com.koinonia.igreja.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dashboardScreen_rendersTitleAndAnalyticsCards() {
        val fakeReportsDao = FakeReportsDaoForDashboard()
        val fakeAttendanceDao = FakeAttendanceDaoForDashboard()
        val analyzeArrivalPeaksUseCase = AnalyzeArrivalPeaksUseCase(fakeAttendanceDao)
        val viewModel = ReportsViewModel(fakeReportsDao, analyzeArrivalPeaksUseCase)

        composeTestRule.setContent {
            DashboardScreen(
                onBack = {},
                onMenuClick = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Métricas & Relatórios").assertIsDisplayed()
        composeTestRule.onNodeWithText("Picos de Chegada (Último Culto)").assertIsDisplayed()
    }
}

class FakeReportsDaoForDashboard : ReportsDao {
    override fun getTopAbsentMembers(): Flow<List<MemberAbsenceCount>> = flowOf(emptyList())
    override fun getPendingContactsForEvent(eventId: String): Flow<List<AttendanceWithMemberInfo>> = flowOf(emptyList())
    override suspend fun updateAbsenceFollowUp(attendanceId: String, reason: String, details: String?, contactMethod: String, responsibleId: String) {}
}

class FakeAttendanceDaoForDashboard : AttendanceDao {
    override suspend fun insertAttendance(attendance: AttendanceEntity) {}
    override suspend fun insertAttendances(attendances: List<AttendanceEntity>) {}
    override suspend fun updateAttendance(attendance: AttendanceEntity) {}
    override fun getAttendanceForEvent(eventId: String): Flow<List<AttendanceEntity>> = flowOf(emptyList())
    override suspend fun getPendingSyncAttendances(): List<AttendanceEntity> = emptyList()
    override suspend fun markAsSynced(id: String) {}
    override suspend fun deleteAttendance(memberId: String, eventId: String) {}
}
