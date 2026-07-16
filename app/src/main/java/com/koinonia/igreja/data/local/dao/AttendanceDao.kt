package com.koinonia.igreja.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.koinonia.igreja.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendances(attendances: List<AttendanceEntity>)

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Query("SELECT * FROM attendance WHERE eventId = :eventId ORDER BY arrivalTime DESC")
    fun getAttendanceForEvent(eventId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE syncPending = 1")
    suspend fun getPendingSyncAttendances(): List<AttendanceEntity>

    @Query("UPDATE attendance SET syncPending = 0 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
