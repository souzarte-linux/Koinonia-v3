package com.koinonia.igreja.presentation.features.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koinonia.igreja.data.repository.AuthRepositoryImpl
import com.koinonia.igreja.domain.usecase.GetMinistryDirectorshipsUseCase
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.GlobalScope
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_displaysEmailAndPasswordField_andActionButtons() {
        var forgotPasswordClicked = false
        val supabase = createSupabaseClient("https://dummy.supabase.co", "dummy_key") {
            install(Auth)
        }
        val fakeMemberDao = FakeMemberDaoForAuth()
        val directorshipsUseCase = GetMinistryDirectorshipsUseCase(fakeMemberDao)
        val authRepo = AuthRepositoryImpl(
            supabaseClient = supabase,
            memberDao = { fakeMemberDao },
            getMinistryDirectorshipsUseCase = { directorshipsUseCase },
            applicationScope = GlobalScope
        )
        val viewModel = AuthViewModel(authRepo, ApplicationProvider.getApplicationContext())

        composeTestRule.setContent {
            LoginScreen(
                onNavigateToHome = {},
                onForgotPasswordClick = { forgotPasswordClicked = true },
                viewModel = viewModel
            )
        }

        // Verifica renderização do título e botão de esqueci a senha
        composeTestRule.onNodeWithText("Ministério do Diácono").assertIsDisplayed()
        composeTestRule.onNodeWithText("Esqueceu a senha?").performClick()
        assertTrue(forgotPasswordClicked)
    }
}
