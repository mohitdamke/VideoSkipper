package com.mohit.videoskipper.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface KeywordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(keyword: KeywordEntity): Long

    @Delete
    suspend fun delete(keyword: KeywordEntity)

    @Query("SELECT * FROM keywords ORDER BY createdAt DESC")
    fun getAllKeywords(): Flow<List<KeywordEntity>>

    // Used by the ML detection loop — fast lookup, no Flow overhead
    @Query("SELECT text FROM keywords WHERE isActive = 1")
    suspend fun getActiveKeywordTexts(): List<String>

    @Query("UPDATE keywords SET isActive = :isActive WHERE id = :id")
    suspend fun setActive(id: Int, isActive: Boolean)
}