package com.koinonia.igreja.data.remote

import com.koinonia.igreja.core.util.Constants
import com.koinonia.igreja.data.remote.dto.AttendanceDto
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRemoteDataSource @Inject constructor(
    private val postgrest: Postgrest
) {
    /**
     * Busca as presenças remotas de um evento.
     */
    suspend fun fetchAttendanceForEvent(eventId: String): List<AttendanceDto> {
        return postgrest[Constants.TABLE_ATTENDANCE]
            .select {
                filter {
                    eq("event_id", eventId)
                }
            }
            .decodeList()
    }

    /**
     * Sincroniza um lote de presenças no Supabase.
     */
    suspend fun upsertAttendanceBatch(attendanceList: List<AttendanceDto>) {
        postgrest[Constants.TABLE_ATTENDANCE]
            .upsert(attendanceList)
    }

    /**
     * Envia uma única presença para a nuvem.
     */
    suspend fun upsertAttendance(attendanceDto: AttendanceDto) {
        postgrest[Constants.TABLE_ATTENDANCE]
            .upsert(attendanceDto)
    }
}
