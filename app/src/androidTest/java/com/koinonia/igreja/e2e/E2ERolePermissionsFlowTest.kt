package com.koinonia.igreja.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koinonia.igreja.domain.model.AppRole
import com.koinonia.igreja.presentation.features.unauthorized.UnauthorizedScreen
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class E2ERolePermissionsFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun e2e_rolePermissionsFlow_adminAndPastorHaveFullAndTreasuryAccess() {
        assertTrue(AppRole.ADMIN.hasFullAccess)
        assertTrue(AppRole.ADMIN.hasTreasuryAccess)

        assertTrue(AppRole.PASTOR.hasFullAccess)
        assertTrue(AppRole.PASTOR.hasTreasuryAccess)

        assertTrue(AppRole.ANCIAO.hasFullAccess)
        assertTrue(AppRole.ANCIAO.hasTreasuryAccess)
    }

    @Test
    fun e2e_rolePermissionsFlow_diaconoAndTesoureiroPermissions() {
        assertTrue(AppRole.DIACONO.hasFullAccess)
        assertFalse(AppRole.DIACONO.hasTreasuryAccess)

        assertFalse(AppRole.TESOUREIRO.hasFullAccess)
        assertTrue(AppRole.TESOUREIRO.hasTreasuryAccess)
    }

    @Test
    fun e2e_rolePermissionsFlow_unauthorizedUserIsRedirectedToUnauthorizedScreen() {
        composeTestRule.setContent {
            UnauthorizedScreen(onNavigateToCalendar = {})
        }

        composeTestRule.onNodeWithText("Acesso Negado").assertIsDisplayed()
        composeTestRule.onNodeWithText("Voltar para a Agenda").assertIsDisplayed()
    }
}
