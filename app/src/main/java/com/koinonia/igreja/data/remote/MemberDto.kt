package com.koinonia.igreja.data.remote

import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.domain.model.Member
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Serializable
data class MemberDto(
    @SerialName("id") val id: String,
    @SerialName("family_id") val familyId: String? = null,
    @SerialName("full_name") val fullName: String,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("birth_date") val birthDate: String? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("is_whatsapp") val isWhatsapp: Boolean = false,
    @SerialName("social_media") val socialMedia: String? = null,
    @SerialName("civil_status") val civilStatus: String? = null,
    @SerialName("baptism_date") val baptismDate: String? = null,
    @SerialName("rebaptism_date") val rebaptismDate: String? = null,
    @SerialName("has_vehicle") val hasVehicle: Boolean = false,
    @SerialName("vehicle_type") val vehicleType: String? = null,
    @SerialName("vehicle_model") val vehicleModel: String? = null,
    @SerialName("created_at") val createdAt: String? = null
) {
    fun toEntity(): MemberEntity {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

        fun parseDate(str: String?): Date? {
            if (str.isNullOrBlank()) return null
            return try {
                dateFormat.parse(str)
            } catch (e: Exception) {
                try { isoFormat.parse(str) } catch (e2: Exception) { null }
            }
        }

        return MemberEntity(
            id = id,
            familyId = familyId,
            fullName = fullName,
            photoUrl = photoUrl,
            birthDate = parseDate(birthDate),
            cep = null,
            street = address,
            number = null,
            neighborhood = null,
            city = null,
            state = null,
            complement = null,
            phone = phone,
            isWhatsapp = isWhatsapp,
            socialMedia = socialMedia,
            civilStatus = civilStatus,
            baptismDate = parseDate(baptismDate),
            rebaptismDate = parseDate(rebaptismDate),
            rg = null,
            cpf = null,
            spouseId = null,
            spouseName = null,
            hasVehicle = hasVehicle,
            vehicleType = vehicleType,
            vehicleModel = vehicleModel,
            createdAt = parseDate(createdAt) ?: Date(),
            syncPending = false,
            updatedAt = Date(),
            email = null,
            authUserId = null,
            mustChangePassword = false
        )
    }

    fun toDomain(): Member {
        val createdInstant = try {
            if (createdAt != null) ZonedDateTime.parse(createdAt) else ZonedDateTime.now()
        } catch (e: Exception) {
            ZonedDateTime.now()
        }
        return Member(
            id = id,
            name = fullName,
            email = "",
            phone = phone ?: "",
            role = "Membro",
            joinedAt = createdInstant,
            isActive = true
        )
    }

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("America/Bahia")
        }

        fun fromEntity(entity: MemberEntity): MemberDto {
            val fullAddress = listOfNotNull(
                entity.street,
                entity.number,
                entity.neighborhood,
                entity.city,
                entity.state
            ).filter { it.isNotBlank() }.joinToString(", ").ifBlank { null }

            return MemberDto(
                id = entity.id,
                familyId = entity.familyId,
                fullName = entity.fullName,
                photoUrl = entity.photoUrl,
                birthDate = entity.birthDate?.let { dateFormat.format(it) },
                address = fullAddress,
                phone = entity.phone,
                isWhatsapp = entity.isWhatsapp,
                socialMedia = entity.socialMedia,
                civilStatus = entity.civilStatus,
                baptismDate = entity.baptismDate?.let { dateFormat.format(it) },
                rebaptismDate = entity.rebaptismDate?.let { dateFormat.format(it) },
                hasVehicle = entity.hasVehicle,
                vehicleType = entity.vehicleType,
                vehicleModel = entity.vehicleModel,
                createdAt = isoFormat.format(entity.createdAt)
            )
        }

        fun fromDomain(member: Member): MemberDto {
            return MemberDto(
                id = member.id,
                fullName = member.name,
                phone = member.phone,
                createdAt = member.joinedAt.toInstant().toString()
            )
        }
    }
}
