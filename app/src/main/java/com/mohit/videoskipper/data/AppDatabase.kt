package com.mohit.videoskipper.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [KeywordEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun keywordDao(): KeywordDao
}