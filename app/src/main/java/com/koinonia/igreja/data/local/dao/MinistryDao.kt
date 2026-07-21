package com.koinonia.igreja.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.koinonia.igreja.data.local.entity.MinistryEntity
import com.koinonia.igreja.data.local.entity.MinistryRoleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MinistryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMinistry(ministry: MinistryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMinistries(ministries: List<MinistryEntity>)

    @Query("SELECT * FROM ministries ORDER BY name ASC")
    fun getAllMinistries(): Flow<List<MinistryEntity>>

    @Query("SELECT * FROM ministries WHERE id = :id LIMIT 1")
    suspend fun getMinistryById(id: String): MinistryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRole(role: MinistryRoleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoles(roles: List<MinistryRoleEntity>)

    @Query("SELECT * FROM ministry_roles ORDER BY title ASC")
    fun getAllRoles(): Flow<List<MinistryRoleEntity>>
}
