package com.mohit.videoskipper.di

import com.mohit.videoskipper.data.repository.AutoScrollDetectionRepositoryImpl
import com.mohit.videoskipper.data.repository.ScrollEventRepositoryImpl
import com.mohit.videoskipper.domain.repository.AutoScrollDetectionRepository
import com.mohit.videoskipper.domain.repository.ScrollEventRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AutoScrollModule {

    @Binds
    abstract fun bindAutoScrollDetectionRepository(
        impl: AutoScrollDetectionRepositoryImpl
    ): AutoScrollDetectionRepository

    @Binds
    abstract fun bindScrollEventRepository(
        impl: ScrollEventRepositoryImpl
    ): ScrollEventRepository
}