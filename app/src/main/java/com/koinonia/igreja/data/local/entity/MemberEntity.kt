package com.koinonia.igreja.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.koinonia.igreja.domain.model.Member
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.UUID

@Entity(
    tableName = "members",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["phone"], unique = true),
        Index(value = ["authUserId"], unique = true)
    ]
)
data class MemberEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val familyId: String? = null,
    val fullName: String,
    val photoUrl: String? = null,
    val birthDate: Date? = null,
    val cep: String? = null,
    val street: String? = null,
    val number: String? = null,
    val neighborhood: String? = null,
    val city: String? = null,
    val state: String? = null,
    val complement: String? = null,
    val phone: String? = null,
    val isWhatsapp: Boolean = false,
    val socialMedia: String? = null,
    val civilStatus: String? = null,
    val baptismDate: Date? = null,
    val rebaptismDate: Date? = null,
    val rg: String? = null,
    val cpf: String? = null,
    val spouseId: String? = null,
    val spouseName: String? = null,
    val hasVehicle: Boolean = false,
    val vehicleType: String? = null, // 'CARRO' ou 'MOTO'
    val vehicleModel: String? = null,
    val createdAt: Date = Date(),
    
    // Controle Offline-First
    val syncPending: Boolean = true,
    val updatedAt: Date = Date(),

    // Campos novos para controle de acesso sólidos
    val email: String? = null,
    val authUserId: String? = null,
    val mustChangePassword: Boolean = false
) {
    fun toDomain(): Member {
        return Member(
            id = id,
            name = fullName,
            email = email ?: "", // E-mail real do membro
            phone = phone ?: "",
            role = "Membro", // Papel padrão
            joinedAt = ZonedDateTime.ofInstant(createdAt.toInstant(), ZoneId.systemDefault()),
            isActive = true
        )
    }

    companion object {
        fun fromDomain(member: Member, syncPending: Boolean = true): MemberEntity {
            return MemberEntity(
                id = member.id,
                familyId = null,
                fullName = member.name,
                photoUrl = null,
                birthDate = null,
                cep = null,
                street = null,
                number = null,
                neighborhood = null,
                city = null,
                state = null,
                complement = null,
                phone = member.phone,
                isWhatsapp = false,
                socialMedia = null,
                email = member.email,
                civilStatus = null,
                baptismDate = null,
                rebaptismDate = null,
                rg = null,
                cpf = null,
                spouseId = null,
                spouseName = null,
                hasVehicle = false,
                vehicleType = null,
                vehicleModel = null,
                createdAt = Date.from(member.joinedAt.toInstant()),
                syncPending = syncPending,
                updatedAt = Date(),
                authUserId = null,
                mustChangePassword = false
            )
        }
    }
}
