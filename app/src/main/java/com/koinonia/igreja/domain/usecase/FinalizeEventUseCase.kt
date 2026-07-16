package com.koinonia.igreja.domain.usecase

import com.koinonia.igreja.data.local.dao.AttendanceDao
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

class FinalizeEventUseCase @Inject constructor(
    private val memberDao: MemberDao,
    private val attendanceDao: AttendanceDao
) {
    /**
     * Identifica membros que não fizeram check-in e os marca como ausentes.
     */
    suspend operator fun invoke(eventId: String) {
        // 1. Busca todos os membros ativos no cache local
        val allMembers = memberDao.getAllMembers().first()
        
        // 2. Busca quem já tem registro (presente ou já marcado como ausente) neste culto
        val currentAttendance = attendanceDao.getAttendanceForEvent(eventId).first()
        val checkedInMemberIds = currentAttendance.map { it.memberId }.toSet()

        // 3. Filtra os membros que faltaram
        val absentMembers = allMembers.filter { it.id !in checkedInMemberIds }

        // 4. Prepara as entidades de ausência em lote
        val absentEntities = absentMembers.map { member ->
            AttendanceEntity(
                id = UUID.randomUUID().toString(),
                memberId = member.id,
                eventId = eventId,
                arrivalTime = null,
                isLate = false,
                lateDurationMins = 0,
                isAbsent = true, // Flag de Ausência Ativada
                absenceReason = null,
                absenceReasonDetails = null,
                contactResponsible = null,
                contactMethod = null,
                syncPending = true // Garante que será enviado ao Supabase
            )
        }

        // 5. Salva no banco local (O WorkManager de Sync cuidará do envio à nuvem posteriormente)
        if (absentEntities.isNotEmpty()) {
            attendanceDao.insertAttendances(absentEntities)
        }
    }
}
