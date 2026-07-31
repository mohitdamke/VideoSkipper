package com.mohit.videoskipper.di

import com.mohit.videoskipper.data.repository.MonitoringRepositoryImpl
import com.mohit.videoskipper.domain.repository.MonitoringRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MonitoringRepositoryModule {
    @Binds
    abstract fun bindMonitoringRepository(
        impl: MonitoringRepositoryImpl
    ): MonitoringRepository
}