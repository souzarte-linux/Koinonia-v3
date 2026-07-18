package com.koinonia.igreja.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.koinonia.igreja.data.local.converter.AppTypeConverters
import com.koinonia.igreja.data.local.dao.AttendanceDao
import com.koinonia.igreja.data.local.dao.EventDao
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.dao.MemberRegistrationDao
import com.koinonia.igreja.data.local.dao.ReportsDao
import com.koinonia.igreja.data.local.dao.VisitorDao
import com.koinonia.igreja.data.local.entity.AttendanceEntity
import com.koinonia.igreja.data.local.entity.ChildEntity
import com.koinonia.igreja.data.local.entity.EventEntity
import com.koinonia.igreja.data.local.entity.FamilyEntity
import com.koinonia.igreja.data.local.entity.MemberEntity
import com.koinonia.igreja.data.local.entity.MinistryHistoryEntity
import com.koinonia.igreja.data.local.entity.VisitorEntity

@Database(
    entities = [
        MemberEntity::class, 
        EventEntity::class, 
        AttendanceEntity::class, 
        VisitorEntity::class,
        FamilyEntity::class,
        ChildEntity::class,
        MinistryHistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun memberRegistrationDao(): MemberRegistrationDao
    abstract fun eventDao(): EventDao
    abstract fun visitorDao(): VisitorDao
    abstract fun reportsDao(): ReportsDao
}
