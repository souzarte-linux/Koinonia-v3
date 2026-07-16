package com.koinonia.igreja.domain.usecase

import com.koinonia.igreja.data.local.dao.AttendanceDao
import kotlinx.coroutines.flow.first
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class AnalyzeArrivalPeaksUseCase @Inject constructor(
    private val attendanceDao: AttendanceDao
) {
    suspend operator fun invoke(eventId: String): Map<String, Int> {
        val attendances = attendanceDao.getAttendanceForEvent(eventId).first()
        
        // Filtra apenas quem esteve presente e possui horário de chegada
        val presentAttendances = attendances.filter { !it.isAbsent && it.arrivalTime != null }

        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val zoneBahia = ZoneId.of("America/Bahia")

        // Agrupa as chegadas arredondando para blocos de 10 minutos
        val peaks = presentAttendances.groupingBy { attendance ->
            val zonedTime = attendance.arrivalTime!!.toInstant().atZone(zoneBahia)
            val minute = zonedTime.minute
            val bucketMinute = (minute / 10) * 10
            val bucketTime = zonedTime.withMinute(bucketMinute).withSecond(0)
            bucketTime.format(formatter)
        }.eachCount()

        // Ordena cronologicamente os blocos de tempo
        return peaks.toSortedMap()
    }
}
