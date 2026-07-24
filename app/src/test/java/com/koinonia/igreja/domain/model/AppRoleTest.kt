package com.koinonia.igreja.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppRoleTest {

    @Test
    fun hasFullAccess_returnsTrueForAdminPastorAnciaoDiacono() {
        val fullAccessRoles = listOf(AppRole.ADMIN, AppRole.PASTOR, AppRole.ANCIAO, AppRole.DIACONO)
        fullAccessRoles.forEach { role ->
            assertTrue("Papel $role deveria ter acesso total", role.hasFullAccess)
        }
    }

    @Test
    fun hasFullAccess_returnsFalseForTesoureiroViewerNone() {
        val restrictedRoles = listOf(AppRole.TESOUREIRO, AppRole.VIEWER, AppRole.NONE)
        restrictedRoles.forEach { role ->
            assertFalse("Papel $role não deveria ter acesso total", role.hasFullAccess)
        }
    }

    @Test
    fun hasTreasuryAccess_returnsTrueForAdminPastorAnciaoTesoureiro() {
        val treasuryRoles = listOf(AppRole.ADMIN, AppRole.PASTOR, AppRole.ANCIAO, AppRole.TESOUREIRO)
        treasuryRoles.forEach { role ->
            assertTrue("Papel $role deveria ter acesso à tesouraria", role.hasTreasuryAccess)
        }
    }

    @Test
    fun hasTreasuryAccess_returnsFalseForDiaconoViewerNone() {
        val restrictedRoles = listOf(AppRole.DIACONO, AppRole.VIEWER, AppRole.NONE)
        restrictedRoles.forEach { role ->
            assertFalse("Papel $role não deveria ter acesso à tesouraria", role.hasTreasuryAccess)
        }
    }

    @Test
    fun valueOf_whenValidRoleName_returnsEnumInstance() {
        assertEquals(AppRole.ADMIN, AppRole.valueOf("ADMIN"))
        assertEquals(AppRole.TESOUREIRO, AppRole.valueOf("TESOUREIRO"))
        assertEquals(AppRole.NONE, AppRole.valueOf("NONE"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun valueOf_whenInvalidRoleName_throwsIllegalArgumentException() {
        AppRole.valueOf("GERENTE")
    }
}
