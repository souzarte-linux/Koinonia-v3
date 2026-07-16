package com.koinonia.igreja.domain.usecase

import com.koinonia.igreja.domain.model.Attendance
import com.koinonia.igreja.domain.model.AttendanceStatus
import com.koinonia.igreja.domain.repository.AttendanceRepository
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject

class SyncAttendanceUseCase @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val calculateLateTimeUseCase: CalculateLateTimeUseCase
) {
    /**
     * Registra a presença de um membro calculando o status de pontualidade.
     *
     * @param memberId ID do membro.
     * @param eventId ID do evento/culto.
     * @param eventStartTime Horário oficial de início do evento.
     * @param checkInTime Horário em que o membro chegou.
     * @param notes Observações opcionais.
     */
    suspend operator fun invoke(
        memberId: String,
        eventId: String,
        eventStartTime: ZonedDateTime,
        checkInTime: ZonedDateTime,
        notes: String? = null
    ) {
        val lateMinutes = calculateLateTimeUseCase(eventStartTime, checkInTime)
        
        // Tolerância de 10 minutos de atraso (exemplo de regra de negócio purista no Use Case!)
        val status = if (lateMinutes > 10L) {
            AttendanceStatus.LATE
        } else {
            AttendanceStatus.PRESENT
        }

        val attendance = Attendance(
            id = UUID.randomUUID().toString(),
            memberId = memberId,
            eventId = eventId,
            checkedInAt = checkInTime,
            status = status,
            notes = if (status == AttendanceStatus.LATE) "Atrasado(a) por $lateMinutes minutos. $notes" else notes
        )

        attendanceRepository.recordAttendance(attendance)
    }
}
