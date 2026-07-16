package com.koinonia.igreja.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "visitors")
data class VisitorEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val eventId: String,
    val name: String,
    val phone: String?,
    val isWhatsapp: Boolean,
    val socialMedia: String?,
    val createdAt: Date = Date(),
    val syncPending: Boolean = true
)
