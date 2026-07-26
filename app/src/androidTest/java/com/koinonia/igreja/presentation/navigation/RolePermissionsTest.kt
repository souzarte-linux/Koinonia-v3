package com.koinonia.igreja.presentation.navigation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.koinonia.igreja.domain.model.AppRole
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RolePermissionsTest {

    @Test
    fun appRole_fullAccessRoles_haveFullAccessPermission() {
        assertTrue(AppRole.ADMIN.hasFullAccess)
        assertTrue(AppRole.PASTOR.hasFullAccess)
        assertTrue(AppRole.ANCIAO.hasFullAccess)
        assertTrue(AppRole.DIACONO.hasFullAccess)
    }

    @Test
    fun appRole_treasuryRoles_haveTreasuryPermission() {
        assertTrue(AppRole.ADMIN.hasTreasuryAccess)
        assertTrue(AppRole.PASTOR.hasTreasuryAccess)
        assertTrue(AppRole.ANCIAO.hasTreasuryAccess)
        assertTrue(AppRole.TESOUREIRO.hasTreasuryAccess)
    }

    @Test
    fun appRole_viewerAndNone_doNotHaveFullOrTreasuryAccess() {
        assertFalse(AppRole.VIEWER.hasFullAccess)
        assertFalse(AppRole.VIEWER.hasTreasuryAccess)

        assertFalse(AppRole.NONE.hasFullAccess)
        assertFalse(AppRole.NONE.hasTreasuryAccess)
    }
}
