package com.koinonia.igreja.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.koinonia.igreja.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Query("SELECT * FROM events ORDER BY startTime ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: String): EventEntity?
}
