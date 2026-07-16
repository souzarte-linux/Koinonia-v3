package com.koinonia.igreja.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "families")
data class FamilyEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAt: Date = Date(),
    val syncPending: Boolean = true
)
