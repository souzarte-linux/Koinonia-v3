package com.koinonia.igreja.core.util

import com.koinonia.igreja.data.local.entity.EventEntity
import com.koinonia.igreja.data.local.converter.EventType
import com.koinonia.igreja.domain.model.AppRole
import com.koinonia.igreja.domain.model.MinistryDirectorship

object EventPermissions {
    fun canManageEvent(
        event: EventEntity?,
        targetMinistryId: String?,
        currentRole: AppRole,
        directedMinistries: List<MinistryDirectorship>,
        hasOrdinaryConflict: Boolean
    ): Boolean {
        // 1. Papel Global de Acesso Total
        if (currentRole.hasFullAccess) {
            return true
        }

        // Se for um evento do tipo ORDINARIO (Culto Ordinário), apenas quem tem FullAccess pode mexer
        if (event?.type == EventType.ORDINARIO) {
            return false
        }

        // 2. Diretor de Ministério Local
        val isDirectorOfTarget = targetMinistryId != null && directedMinistries.any { it.ministryId == targetMinistryId }
        if (isDirectorOfTarget) {
            // Não pode haver conflito com Culto Ordinário para diretores locais
            return !hasOrdinaryConflict
        }

        return false
    }
}
