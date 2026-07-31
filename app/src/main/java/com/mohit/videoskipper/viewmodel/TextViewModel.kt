package com.mohit.videoskipper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohit.videoskipper.data.KeywordEntity
import com.mohit.videoskipper.domain.repository.KeywordRepository
import com.mohit.videoskipper.states.TextUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import jakarta.inject.Inject
import kotlinx.coroutines.flow.launchIn

@HiltViewModel
class TextViewModel @Inject constructor(
    private val repository: KeywordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TextUiState())
    val uiState: StateFlow<TextUiState> = _uiState.asStateFlow()

    init {
        observeKeywords()
    }

    private fun observeKeywords() {
        repository.getAllKeywords()
            .onEach { keywords ->
                _uiState.update { it.copy(keywords = keywords, isLoading = false) }
            }
            .catch { e ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load keywords")
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onAddDialogOpen() {
        _uiState.update { it.copy(isAddDialogVisible = true, newKeywordInput = "") }
    }

    fun onAddDialogDismiss() {
        _uiState.update { it.copy(isAddDialogVisible = false, newKeywordInput = "") }
    }

    fun onNewKeywordInputChange(input: String) {
        _uiState.update { it.copy(newKeywordInput = input) }
    }

    fun onConfirmAddKeyword() {
        val text = _uiState.value.newKeywordInput.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            try {
                repository.addKeyword(text)
                _uiState.update { it.copy(isAddDialogVisible = false, newKeywordInput = "") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to add keyword") }
            }
        }
    }

    fun onDeleteKeyword(keyword: KeywordEntity) {
        viewModelScope.launch {
            try {
                repository.deleteKeyword(keyword)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to delete keyword") }
            }
        }
    }

    fun onToggleActive(keyword: KeywordEntity) {
        viewModelScope.launch {
            try {
                repository.setActive(keyword.id, !keyword.isActive)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to update keyword") }
            }
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

private inline fun <T> MutableStateFlow<T>.update(function: (T) -> T) {
    value = function(value)
}