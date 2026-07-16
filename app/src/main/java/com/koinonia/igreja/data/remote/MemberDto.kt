package com.koinonia.igreja.data.remote

import com.koinonia.igreja.domain.model.Member
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
data class MemberDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("phone") val phone: String,
    @SerialName("role") val role: String,
    @SerialName("joined_at") val joinedAtIso: String,
    @SerialName("is_active") val isActive: Boolean
) {
    fun toDomain(): Member {
        return Member(
            id = id,
            name = name,
            email = email,
            phone = phone,
            role = role,
            joinedAt = ZonedDateTime.parse(joinedAtIso),
            isActive = isActive
        )
    }

    companion object {
        fun fromDomain(member: Member): MemberDto {
            return MemberDto(
                id = member.id,
                name = member.name,
                email = member.email,
                phone = member.phone,
                role = member.role,
                joinedAtIso = member.joinedAt.toInstant().toString(),
                isActive = member.isActive
            )
        }
    }
}
