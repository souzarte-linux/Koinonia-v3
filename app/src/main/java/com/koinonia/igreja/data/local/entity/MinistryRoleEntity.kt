package com.koinonia.igreja.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.koinonia.igreja.domain.model.MinistryPositionTier
import java.util.UUID

@Entity(tableName = "ministry_roles")
data class MinistryRoleEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val tier: MinistryPositionTier
)
