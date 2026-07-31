package com.mohit.videoskipper.data.repository

import com.mohit.videoskipper.data.KeywordDao
import com.mohit.videoskipper.data.KeywordEntity
import com.mohit.videoskipper.domain.repository.KeywordRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class KeywordRepositoryImpl @Inject constructor(
    private val dao: KeywordDao
) : KeywordRepository {

    override fun getAllKeywords(): Flow<List<KeywordEntity>> = dao.getAllKeywords()

    override suspend fun addKeyword(text: String) {
        val trimmed = text.trim().lowercase()
        if (trimmed.isNotEmpty()) {
            dao.insert(KeywordEntity(text = trimmed))
        }
    }

    override suspend fun deleteKeyword(keyword: KeywordEntity) = dao.delete(keyword)

    override suspend fun setActive(id: Int, isActive: Boolean) = dao.setActive(id, isActive)

    override suspend fun getActiveKeywordTexts(): List<String> = dao.getActiveKeywordTexts()
}