package com.koinonia.igreja.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.koinonia.igreja.data.local.converter.EventType
import com.koinonia.igreja.data.local.converter.LocationType
import java.util.Date
import java.util.UUID

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: EventType,
    val startTime: Date,
    val endTime: Date,
    val locationType: LocationType,
    val address: String?,
    val ministryId: String?,
    val createdAt: Date = Date(),
    val syncPending: Boolean = true
)
