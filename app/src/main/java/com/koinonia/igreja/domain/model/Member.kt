package com.koinonia.igreja.domain.model

import java.time.ZonedDateTime

data class Member(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val joinedAt: ZonedDateTime,
    val isActive: Boolean
)
