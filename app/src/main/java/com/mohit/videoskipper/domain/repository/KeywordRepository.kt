package com.mohit.videoskipper.domain.repository

import com.mohit.videoskipper.data.KeywordEntity
import kotlinx.coroutines.flow.Flow

interface KeywordRepository {
    fun getAllKeywords(): Flow<List<KeywordEntity>>
    suspend fun addKeyword(text: String)
    suspend fun deleteKeyword(keyword: KeywordEntity)
    suspend fun setActive(id: Int, isActive: Boolean)
    suspend fun getActiveKeywordTexts(): List<String>
}