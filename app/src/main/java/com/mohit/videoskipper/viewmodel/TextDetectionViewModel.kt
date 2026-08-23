package com.mohit.videoskipper.viewmodel

import android.app.Application
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mohit.videoskipper.domain.repository.KeywordRepository
import com.mohit.videoskipper.domain.repository.MonitoringRepository
import com.mohit.videoskipper.service.TextDetectorAccessibilityService
import com.mohit.videoskipper.states.TextDetectionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TextDetectionViewModel @Inject constructor(
    application: Application,
    private val monitoringRepository: MonitoringRepository,
    private val keywordRepository: KeywordRepository
) : AndroidViewModel(application) {
    val TAG = "HEEEEWaooo"
    private val _uiState = MutableStateFlow(TextDetectionUiState())
    val uiState: StateFlow<TextDetectionUiState> = _uiState.asStateFlow()

    // Tracks the last-applied value so we only act on actual transitions
    // (false->true / true->false), not on every recomposition of the flow.
    private var previousEnabledValue: Boolean? = null

    init {
        observeState()
        refreshAccessibilityServiceStatus()
    }

    private fun observeState() {

        combine(
            monitoringRepository.isTextDetectionEnabled(),
            keywordRepository.getAllKeywords()
        ) { enabled, keywords ->
            enabled to keywords.count { it.isActive }
        }
            .onEach { (enabled, activeCount) ->

                _uiState.update {
                    it.copy(
                        isTextDetectionEnabled = enabled,
                        activeKeywordCount = activeCount,
                        isLoading = false
                    )
                }

                applyToggleTransition(enabled)
            }
            .catch { e ->

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load state"
                    )
                }
            }
            .launchIn(viewModelScope)

    }

    /**
     * This is the actual "apply the swipe logic" step: when the flag flips
     * true, we start the service's scroll-event collector AND run one
     * immediate check on whatever's on screen right now. When it flips
     * false, we stop the collector entirely so no OCR/swipe runs at all.
     */
    private fun applyToggleTransition(enabled: Boolean) {

        if (previousEnabledValue == enabled) {
            return
        }

        previousEnabledValue = enabled


        val service = TextDetectorAccessibilityService.instance ?: return


        if (enabled) {
            service.startMonitoring()
            service.triggerImmediateCheck()

        } else {
            service.stopMonitoring()
        }
    }

    /** Call this when the screen resumes — accessibility permission can change outside the app. */
    fun refreshAccessibilityServiceStatus() {

        val enabled = isAccessibilityServiceEnabled(
            getApplication(),
            TextDetectorAccessibilityService::class.java
        )

        _uiState.update {
            it.copy(isAccessibilityServiceEnabled = enabled)
        }
    }

    fun onToggleTextDetection(enabled: Boolean) {

        if (enabled && !_uiState.value.isAccessibilityServiceEnabled) {

            _uiState.update {
                it.copy(errorMessage = "Enable accessibility permission first")
            }

            return
        }

        viewModelScope.launch {

            try {

                monitoringRepository.setTextDetectionEnabled(enabled)

            } catch (e: Exception) {

                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "Failed to update text detection"
                    )
                }
            }
        }
    }

    fun onErrorShown() {

        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    private fun isAccessibilityServiceEnabled(
        context: Context,
        serviceClass: Class<*>
    ): Boolean {

        val expectedId = "${context.packageName}/${serviceClass.canonicalName}"

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: run {
            return false
        }

        val result = enabledServices
            .split(':')
            .any { it.equals(expectedId, ignoreCase = true) }

        return result
    }
}