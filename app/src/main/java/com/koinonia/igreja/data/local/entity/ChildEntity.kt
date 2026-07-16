package com.koinonia.igreja.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "children")
data class ChildEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val fullName: String,
    val birthDate: Date?,
    val createdAt: Date = Date(),
    val syncPending: Boolean = true
)
