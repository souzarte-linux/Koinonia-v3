package com.koinonia.igreja.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "ministry_history")
data class MinistryHistoryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val ministryName: String,
    val role: String,
    val startDate: Date?,
    val endDate: Date?,
    val createdAt: Date = Date(),
    val syncPending: Boolean = true
)
