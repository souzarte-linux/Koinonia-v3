package com.koinonia.igreja.core.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.koinonia.igreja.data.local.AppDatabase
import com.koinonia.igreja.data.local.dao.AttendanceDao
import com.koinonia.igreja.data.local.dao.EventDao
import com.koinonia.igreja.data.local.dao.MemberDao
import com.koinonia.igreja.data.local.dao.MemberRegistrationDao
import com.koinonia.igreja.data.local.dao.ReportsDao
import com.koinonia.igreja.data.local.dao.VisitorDao
import com.koinonia.igreja.data.local.dao.MinistryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "koinonia_database"
        )
        .addMigrations(AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideMemberDao(database: AppDatabase): MemberDao {
        return database.memberDao()
    }

    @Provides
    @Singleton
    fun provideAttendanceDao(database: AppDatabase): AttendanceDao {
        return database.attendanceDao()
    }

    @Provides
    @Singleton
    fun provideMinistryDao(database: AppDatabase): MinistryDao {
        return database.ministryDao()
    }

    @Provides
    @Singleton
    fun provideMemberRegistrationDao(database: AppDatabase): MemberRegistrationDao {
        return database.memberRegistrationDao()
    }

    @Provides
    @Singleton
    fun provideEventDao(database: AppDatabase): EventDao {
        return database.eventDao()
    }

    @Provides
    @Singleton
    fun provideVisitorDao(database: AppDatabase): VisitorDao {
        return database.visitorDao()
    }

    @Provides
    @Singleton
    fun provideReportsDao(database: AppDatabase): ReportsDao {
        return database.reportsDao()
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}
