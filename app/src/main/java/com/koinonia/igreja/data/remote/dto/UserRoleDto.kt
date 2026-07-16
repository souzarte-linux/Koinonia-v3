package com.koinonia.igreja.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserRoleDto(
    val user_id: String,
    val role: String
)
