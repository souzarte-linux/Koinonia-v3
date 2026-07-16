package com.koinonia.igreja.data.remote.dto

import com.koinonia.igreja.domain.model.Attendance
import com.koinonia.igreja.domain.model.AttendanceStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class AttendanceDto(
    @SerialName("id") val id: String,
    @SerialName("member_id") val memberId: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("arrival_time") val arrivalTime: String?, // Formato ISO-8601
    @SerialName("is_late") val isLate: Boolean,
    @SerialName("late_duration_mins") val lateDurationMins: Int,
    @SerialName("is_absent") val isAbsent: Boolean,
    @SerialName("absence_reason") val absenceReason: String?,
    @SerialName("absence_reason_details") val absenceReasonDetails: String?
) {
    fun toDomain(): Attendance {
        val status = when {
            isAbsent -> AttendanceStatus.ABSENT
            isLate -> AttendanceStatus.LATE
            else -> AttendanceStatus.PRESENT
        }
        
        val timeString = arrivalTime ?: ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        
        return Attendance(
            id = id,
            memberId = memberId,
            eventId = eventId,
            checkedInAt = ZonedDateTime.parse(timeString),
            status = status,
            notes = absenceReasonDetails ?: absenceReason
        )
    }

    companion object {
        fun fromDomain(attendance: Attendance): AttendanceDto {
            val isAbsent = attendance.status == AttendanceStatus.ABSENT
            val isLate = attendance.status == AttendanceStatus.LATE
            
            return AttendanceDto(
                id = attendance.id,
                memberId = attendance.memberId,
                eventId = attendance.eventId,
                arrivalTime = if (isAbsent) null else attendance.checkedInAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                isLate = isLate,
                lateDurationMins = if (isLate) 15 else 0,
                isAbsent = isAbsent,
                absenceReason = if (isAbsent) attendance.notes else null,
                absenceReasonDetails = if (isAbsent) attendance.notes else null
            )
        }
    }
}
