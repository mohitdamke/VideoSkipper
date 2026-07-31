package com.mohit.videoskipper.states

import com.mohit.videoskipper.data.KeywordEntity

data class TextUiState(
    val keywords: List<KeywordEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isAddDialogVisible: Boolean = false,
    val newKeywordInput: String = "",
    val errorMessage: String? = null
) {
    val filteredKeywords: List<KeywordEntity>
        get() = if (searchQuery.isBlank()) {
            keywords
        } else {
            keywords.filter { it.text.contains(searchQuery, ignoreCase = true) }
        }
}