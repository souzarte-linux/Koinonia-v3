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
    val familyId: String?,
    val fullName: String,
    val photoUrl: String?,
    val birthDate: Date?,
    val cep: String?,
    val street: String?,
    val number: String?,
    val neighborhood: String?,
    val city: String?,
    val state: String?,
    val complement: String?,
    val phone: String?,
    val isWhatsapp: Boolean = false,
    val socialMedia: String?,
    val civilStatus: String?,
    val baptismDate: Date?,
    val rebaptismDate: Date?,
    val rg: String?,
    val cpf: String?,
    val spouseId: String?,
    val spouseName: String?,
    val hasVehicle: Boolean = false,
    val vehicleType: String?, // 'CARRO' ou 'MOTO'
    val vehicleModel: String?,
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
