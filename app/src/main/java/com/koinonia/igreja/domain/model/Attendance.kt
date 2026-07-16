package com.koinonia.igreja.domain.model

import java.time.ZonedDateTime

data class Attendance(
    val id: String,
    val memberId: String,
    val eventId: String,
    val checkedInAt: ZonedDateTime,
    val status: AttendanceStatus,
    val notes: String?
)

enum class AttendanceStatus {
    PRESENT,
    LATE,
    ABSENT
}
