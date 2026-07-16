package com.koinonia.igreja.core.di

import com.koinonia.igreja.data.repository.MemberRepositoryImpl
import com.koinonia.igreja.domain.repository.MemberRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMemberRepository(
        memberRepositoryImpl: MemberRepositoryImpl
    ): MemberRepository

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(
        attendanceRepositoryImpl: com.koinonia.igreja.data.repository.AttendanceRepositoryImpl
    ): com.koinonia.igreja.domain.repository.AttendanceRepository
}
