package com.mohit.videoskipper.di

import android.content.Context
import androidx.room.Room
import com.mohit.videoskipper.data.AppDatabase
import com.mohit.videoskipper.data.KeywordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "app_db")
            .fallbackToDestructiveMigration(false) // fine for dev; remove once you ship
            .build()

    @Provides
    fun provideKeywordDao(db: AppDatabase): KeywordDao = db.keywordDao()
}