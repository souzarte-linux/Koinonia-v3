package com.koinonia.igreja.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.koinonia.igreja.data.local.entity.VisitorEntity

@Dao
interface VisitorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitor(visitor: VisitorEntity)

    @Query("SELECT * FROM visitors WHERE eventId = :eventId")
    suspend fun getVisitorsForEvent(eventId: String): List<VisitorEntity>
}
