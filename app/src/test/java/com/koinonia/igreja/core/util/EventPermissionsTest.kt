package com.koinonia.igreja.core.util

import com.koinonia.igreja.data.local.converter.EventType
import com.koinonia.igreja.data.local.converter.LocationType
import com.koinonia.igreja.data.local.entity.EventEntity
import com.koinonia.igreja.domain.model.AppRole
import com.koinonia.igreja.domain.model.MinistryDirectorship
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class EventPermissionsTest {

    private fun createDummyEvent(type: EventType, ministryId: String? = null): EventEntity {
        return EventEntity(
            id = "event_123",
            title = "Evento Teste",
            type = type,
            startTime = Date(),
            endTime = Date(),
            locationType = LocationType.IGREJA_LOCAL,
            address = null,
            ministryId = ministryId,
            creatorEmail = "creator@koinonia.org"
        )
    }

    @Test
    fun canManageEvent_whenRoleHasFullAccess_returnsTrueForAnyEvent() {
        val fullAccessRoles = listOf(AppRole.ADMIN, AppRole.PASTOR, AppRole.ANCIAO, AppRole.DIACONO)
        val dummyOrdinaryEvent = createDummyEvent(EventType.ORDINARIO)

        fullAccessRoles.forEach { role ->
            val result = EventPermissions.canManageEvent(
                event = dummyOrdinaryEvent,
                targetMinistryId = "min_1",
                currentRole = role,
                directedMinistries = emptyList(),
                hasOrdinaryConflict = true
            )
            assertTrue("Papel $role deveria ter permissão total", result)
        }
    }

    @Test
    fun canManageEvent_whenEventIsOrdinaryAndRoleHasNoFullAccess_returnsFalse() {
        val nonFullAccessRoles = listOf(AppRole.TESOUREIRO, AppRole.VIEWER, AppRole.NONE)
        val ordinaryEvent = createDummyEvent(EventType.ORDINARIO)

        nonFullAccessRoles.forEach { role ->
            val result = EventPermissions.canManageEvent(
                event = ordinaryEvent,
                targetMinistryId = "min_1",
                currentRole = role,
                directedMinistries = listOf(MinistryDirectorship("min_1", "Louvor")),
                hasOrdinaryConflict = false
            )
            assertFalse("Papel $role não deveria ter permissão em evento ORDINARIO", result)
        }
    }

    @Test
    fun canManageEvent_whenUserIsDirectorOfTargetMinistryAndNoConflict_returnsTrue() {
        val extraEvent = createDummyEvent(EventType.EXTRAORDINARIO, ministryId = "min_louvor")
        val directed = listOf(MinistryDirectorship("min_louvor", "Ministério de Louvor"))

        val result = EventPermissions.canManageEvent(
            event = extraEvent,
            targetMinistryId = "min_louvor",
            currentRole = AppRole.NONE,
            directedMinistries = directed,
            hasOrdinaryConflict = false
        )

        assertTrue("Diretor local sem conflito ordinário deve poder gerenciar o evento", result)
    }

    @Test
    fun canManageEvent_whenUserIsDirectorOfTargetMinistryButHasOrdinaryConflict_returnsFalse() {
        val extraEvent = createDummyEvent(EventType.EXTRAORDINARIO, ministryId = "min_louvor")
        val directed = listOf(MinistryDirectorship("min_louvor", "Ministério de Louvor"))

        val result = EventPermissions.canManageEvent(
            event = extraEvent,
            targetMinistryId = "min_louvor",
            currentRole = AppRole.NONE,
            directedMinistries = directed,
            hasOrdinaryConflict = true
        )

        assertFalse("Diretor local com conflito ordinário não deve poder gerenciar o evento", result)
    }

    @Test
    fun canManageEvent_whenUserIsNotDirectorOfTargetMinistry_returnsFalse() {
        val extraEvent = createDummyEvent(EventType.EXTRAORDINARIO, ministryId = "min_louvor")
        val directed = listOf(MinistryDirectorship("min_teatro", "Ministério de Teatro"))

        val result = EventPermissions.canManageEvent(
            event = extraEvent,
            targetMinistryId = "min_louvor",
            currentRole = AppRole.NONE,
            directedMinistries = directed,
            hasOrdinaryConflict = false
        )

        assertFalse("Usuário que não é diretor do ministério alvo deve ter permissão negada", result)
    }

    @Test
    fun canManageEvent_whenTargetMinistryIdIsNull_returnsFalseForNonFullAccess() {
        val extraEvent = createDummyEvent(EventType.EXTRAORDINARIO)
        val directed = listOf(MinistryDirectorship("min_1", "Louvor"))

        val result = EventPermissions.canManageEvent(
            event = extraEvent,
            targetMinistryId = null,
            currentRole = AppRole.VIEWER,
            directedMinistries = directed,
            hasOrdinaryConflict = false
        )

        assertFalse("Sem targetMinistryId e sem FullAccess deve retornar false", result)
    }
}
