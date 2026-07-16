package com.koinonia.igreja.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.koinonia.igreja.domain.model.Attendance
import com.koinonia.igreja.domain.model.AttendanceStatus
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.UUID

@Entity(
    tableName = "attendance",
    indices = [Index(value = ["memberId", "eventId"], unique = true)]
)
data class AttendanceEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val eventId: String,
    val arrivalTime: Date?, // Nulo se faltou
    val isLate: Boolean = false,
    val lateDurationMins: Int = 0,
    val isAbsent: Boolean = false,
    val absenceReason: String?,
    val absenceReasonDetails: String?,
    val contactResponsible: String?,
    val contactMethod: String?,
    val createdAt: Date = Date(),
    val syncPending: Boolean = true
) {
    fun toDomain(): Attendance {
        val status = when {
            isAbsent -> AttendanceStatus.ABSENT
            isLate -> AttendanceStatus.LATE
            else -> AttendanceStatus.PRESENT
        }
        
        val localTime = arrivalTime ?: createdAt
        
        return Attendance(
            id = id,
            memberId = memberId,
            eventId = eventId,
            checkedInAt = ZonedDateTime.ofInstant(localTime.toInstant(), ZoneId.systemDefault()),
            status = status,
            notes = absenceReasonDetails ?: absenceReason
        )
    }

    companion object {
        fun fromDomain(attendance: Attendance, syncPending: Boolean = true): AttendanceEntity {
            val isAbsent = attendance.status == AttendanceStatus.ABSENT
            val isLate = attendance.status == AttendanceStatus.LATE
            
            return AttendanceEntity(
                id = attendance.id,
                memberId = attendance.memberId,
                eventId = attendance.eventId,
                arrivalTime = if (isAbsent) null else Date.from(attendance.checkedInAt.toInstant()),
                isLate = isLate,
                lateDurationMins = if (isLate) 15 else 0, // valor default de teste
                isAbsent = isAbsent,
                absenceReason = if (isAbsent) attendance.notes else null,
                absenceReasonDetails = if (isAbsent) attendance.notes else null,
                contactResponsible = null,
                contactMethod = null,
                createdAt = Date.from(attendance.checkedInAt.toInstant()),
                syncPending = syncPending
            )
        }
    }
}
