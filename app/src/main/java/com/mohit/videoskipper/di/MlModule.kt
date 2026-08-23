package com.mohit.videoskipper.di

import com.mohit.videoskipper.data.repository.TextDetectionRepositoryImpl
import com.mohit.videoskipper.domain.repository.TextDetectionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MlModule {
    @Binds
    abstract fun bindTextDetectionRepository(
        impl: TextDetectionRepositoryImpl
    ): TextDetectionRepository
}