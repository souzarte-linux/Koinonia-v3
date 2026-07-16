package com.koinonia.igreja.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.koinonia.igreja.domain.model.Member
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.UUID

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val familyId: String?,
    val fullName: String,
    val photoUrl: String?,
    val birthDate: Date?,
    val address: String?,
    val phone: String?,
    val isWhatsapp: Boolean = false,
    val socialMedia: String?,
    val civilStatus: String?,
    val baptismDate: Date?,
    val rebaptismDate: Date?,
    val hasVehicle: Boolean = false,
    val vehicleType: String?, // 'CARRO' ou 'MOTO'
    val vehicleModel: String?,
    val createdAt: Date = Date(),
    
    // Controle Offline-First
    val syncPending: Boolean = true,
    val updatedAt: Date = Date() 
) {
    fun toDomain(): Member {
        return Member(
            id = id,
            name = fullName,
            email = socialMedia ?: "", // Campo de apoio provisório
            phone = phone ?: "",
            role = vehicleType ?: "Membro", // Usado para mapear a função
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
                address = null,
                phone = member.phone,
                isWhatsapp = false,
                socialMedia = member.email,
                civilStatus = null,
                baptismDate = null,
                rebaptismDate = null,
                hasVehicle = false,
                vehicleType = member.role,
                vehicleModel = null,
                createdAt = Date.from(member.joinedAt.toInstant()),
                syncPending = syncPending,
                updatedAt = Date()
            )
        }
    }
}
