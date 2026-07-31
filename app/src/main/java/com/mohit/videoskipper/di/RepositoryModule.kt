package com.mohit.videoskipper.di

import com.mohit.videoskipper.data.repository.KeywordRepositoryImpl
import com.mohit.videoskipper.domain.repository.KeywordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindKeywordRepository(
        impl: KeywordRepositoryImpl
    ): KeywordRepository
}