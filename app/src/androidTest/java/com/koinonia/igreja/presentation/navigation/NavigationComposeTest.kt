package com.koinonia.igreja.presentation.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koinonia.igreja.presentation.features.unauthorized.UnauthorizedScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun screenRoutes_verifySealedClassRouteStrings() {
        assertEquals("auth", Screen.Auth.route)
        assertEquals("forgot_password", Screen.ForgotPassword.route)
        assertEquals("members_list", Screen.MembersList.route)
        assertEquals("member_add", Screen.MemberAdd.route)
        assertEquals("calendar", Screen.Calendar.route)
        assertEquals("reception", Screen.Reception.route)
        assertEquals("reports", Screen.Reports.route)

        val details = Screen.MemberDetails("mem_123")
        assertEquals("member_details/mem_123", details.route)
        assertEquals("member_details/{memberId}", Screen.MemberDetails.ROUTE_TEMPLATE)
    }

    @Test
    fun unauthorizedScreen_displaysAccessDeniedTitleAndMessage() {
        composeTestRule.setContent {
            UnauthorizedScreen(onNavigateToCalendar = {})
        }

        composeTestRule.onNodeWithText("Acesso Negado").assertIsDisplayed()
        composeTestRule.onNodeWithText("Você não tem permissão para acessar esta tela. Entre em contato com a liderança da igreja se precisar deste privilégio.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Voltar para a Agenda").assertIsDisplayed()
    }
}
