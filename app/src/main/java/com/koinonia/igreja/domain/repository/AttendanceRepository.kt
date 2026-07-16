package com.koinonia.igreja.domain.repository

import com.koinonia.igreja.domain.model.Attendance
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    /**
     * Retorna a lista de presenças registradas em um evento específico.
     */
    fun getAttendanceForEvent(eventId: String): Flow<List<Attendance>>

    /**
     * Registra a presença de um membro localmente e sincroniza.
     */
    suspend fun recordAttendance(attendance: Attendance)
}
