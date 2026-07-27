package com.koinonia.igreja.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import com.koinonia.igreja.data.repository.AuthRepositoryImpl
import com.koinonia.igreja.domain.usecase.GetMinistryDirectorshipsUseCase
import com.koinonia.igreja.presentation.features.auth.AuthViewModel
import com.koinonia.igreja.presentation.features.auth.ForgotPasswordScreen
import com.koinonia.igreja.presentation.features.auth.LoginScreen
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class E2EAuthFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createViewModel(): AuthViewModel {
        val supabase = createSupabaseClient("https://dummy.supabase.co", "dummy_key") {
            install(Auth)
        }
        val fakeMemberDao = FakeMemberDaoForE2EAuth()
        val directorshipsUseCase = GetMinistryDirectorshipsUseCase(fakeMemberDao)
        val authRepo = AuthRepositoryImpl(
            supabaseClient = supabase,
            memberDao = { fakeMemberDao },
            getMinistryDirectorshipsUseCase = { directorshipsUseCase },
            applicationScope = GlobalScope
        )
        return AuthViewModel(authRepo, ApplicationProvider.getApplicationContext())
    }

    @Test
    fun e2e_authFlow_step1_loginScreen_rendersAndTriggersForgotPassword() {
        var forgotPasswordNavigated = false
        val viewModel = createViewModel()

        composeTestRule.setContent {
            LoginScreen(
                onNavigateToHome = {},
                onForgotPasswordClick = { forgotPasswordNavigated = true },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Ministério do Diácono").assertIsDisplayed()
        composeTestRule.onNodeWithText("Esqueceu a senha?").performClick()
        assertTrue(forgotPasswordNavigated)
    }

    @Test
    fun e2e_authFlow_step2_forgotPasswordScreen_rendersAndNavigatesBack() {
        var backToLoginNavigated = false
        val viewModel = createViewModel()

        composeTestRule.setContent {
            ForgotPasswordScreen(
                viewModel = viewModel,
                onResetSent = {},
                onBackToLogin = { backToLoginNavigated = true }
            )
        }

        composeTestRule.onNodeWithText("Recuperar Acesso").assertIsDisplayed()
        composeTestRule.onNodeWithText("Voltar ao Login").performClick()
        assertTrue(backToLoginNavigated)
    }
}

class FakeMemberDaoForE2EAuth : MemberDao {
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
