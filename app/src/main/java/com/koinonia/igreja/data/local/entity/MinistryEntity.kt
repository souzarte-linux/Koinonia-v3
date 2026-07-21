package com.koinonia.igreja.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ministries")
data class MinistryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val parentMinistryId: String?,
    val minAge: Int?,
    val maxAge: Int?,
    val minMembershipMonths: Int?,
    val notes: String?
)
