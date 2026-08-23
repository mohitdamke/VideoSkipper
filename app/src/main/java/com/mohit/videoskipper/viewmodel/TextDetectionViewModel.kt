package com.mohit.videoskipper.viewmodel

import android.app.Application
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mohit.videoskipper.domain.repository.KeywordRepository
import com.mohit.videoskipper.domain.repository.MonitoringRepository
import com.mohit.videoskipper.service.PizzaDetectorAccessibilityService
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
        Log.d(TAG, "================ ViewModel Initialized ================")
        Log.d(TAG, "Initializing TextDetectionViewModel")
        observeState()
        refreshAccessibilityServiceStatus()
    }

    private fun observeState() {
        Log.d(TAG, "Starting observeState()")

        combine(
            monitoringRepository.isTextDetectionEnabled(),
            keywordRepository.getAllKeywords()
        ) { enabled, keywords ->
            Log.d(
                TAG,
                "combine() -> enabled=$enabled, totalKeywords=${keywords.size}, activeKeywords=${keywords.count { it.isActive }}"
            )
            enabled to keywords.count { it.isActive }
        }
            .onEach { (enabled, activeCount) ->

                Log.d(
                    TAG,
                    "Received new state -> enabled=$enabled, activeKeywordCount=$activeCount"
                )

                _uiState.update {
                    Log.d(TAG, "Updating UI State")
                    it.copy(
                        isTextDetectionEnabled = enabled,
                        activeKeywordCount = activeCount,
                        isLoading = false
                    )
                }

                Log.d(TAG, "Calling applyToggleTransition($enabled)")
                applyToggleTransition(enabled)
            }
            .catch { e ->

                Log.e(TAG, "observeState() failed", e)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load state"
                    )
                }
            }
            .launchIn(viewModelScope)

        Log.d(TAG, "observeState() launched in viewModelScope")
    }

    /**
     * This is the actual "apply the swipe logic" step: when the flag flips
     * true, we start the service's scroll-event collector AND run one
     * immediate check on whatever's on screen right now. When it flips
     * false, we stop the collector entirely so no OCR/swipe runs at all.
     */
    private fun applyToggleTransition(enabled: Boolean) {

        Log.d(TAG, "applyToggleTransition(enabled=$enabled)")
        Log.d(TAG, "Previous value = $previousEnabledValue")

        if (previousEnabledValue == enabled) {
            Log.d(TAG, "Ignoring duplicate transition")
            return
        }

        previousEnabledValue = enabled

        Log.d(TAG, "Updated previousEnabledValue=$previousEnabledValue")

        val service = PizzaDetectorAccessibilityService.instance

        Log.d(TAG, "Accessibility Service Instance = $service")

        if (service == null) {
            Log.d(TAG, "Service is NULL. Waiting for Accessibility Service connection.")
            return
        }

        if (enabled) {

            Log.d(TAG, "Starting monitoring...")

            service.startMonitoring()

            Log.d(TAG, "Monitoring started")

            service.triggerImmediateCheck()

            Log.d(TAG, "Immediate screen check triggered")

        } else {

            Log.d(TAG, "Stopping monitoring...")

            service.stopMonitoring()

            Log.d(TAG, "Monitoring stopped")
        }
    }

    /** Call this when the screen resumes — accessibility permission can change outside the app. */
    fun refreshAccessibilityServiceStatus() {

        Log.d(TAG, "Refreshing accessibility service status")

        val enabled = isAccessibilityServiceEnabled(
            getApplication(),
            PizzaDetectorAccessibilityService::class.java
        )

        Log.d(TAG, "Accessibility Enabled = $enabled")

        _uiState.update {
            it.copy(isAccessibilityServiceEnabled = enabled)
        }

        Log.d(TAG, "UI State updated with accessibility status")
    }

    fun onToggleTextDetection(enabled: Boolean) {

        Log.d(TAG, "User toggled Text Detection -> $enabled")

        if (enabled && !_uiState.value.isAccessibilityServiceEnabled) {

            Log.d(TAG, "Accessibility permission missing")

            _uiState.update {
                it.copy(errorMessage = "Enable accessibility permission first")
            }

            return
        }

        viewModelScope.launch {

            Log.d(TAG, "Saving detection state to repository")

            try {

                monitoringRepository.setTextDetectionEnabled(enabled)

                Log.d(
                    TAG,
                    "Repository updated successfully. Waiting for Flow to emit latest state."
                )

            } catch (e: Exception) {

                Log.e(TAG, "Failed to update text detection", e)

                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "Failed to update text detection"
                    )
                }
            }
        }
    }

    fun onErrorShown() {

        Log.d(TAG, "Clearing error message")

        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    private fun isAccessibilityServiceEnabled(
        context: Context,
        serviceClass: Class<*>
    ): Boolean {

        Log.d(TAG, "Checking Accessibility Service status")

        val expectedId = "${context.packageName}/${serviceClass.canonicalName}"

        Log.d(TAG, "Expected Service ID = $expectedId")

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: run {
            Log.d(TAG, "No accessibility services are enabled")
            return false
        }

        Log.d(TAG, "Enabled Services = $enabledServices")

        val result = enabledServices
            .split(':')
            .any { it.equals(expectedId, ignoreCase = true) }

        Log.d(TAG, "Accessibility Service Enabled = $result")

        return result
    }
}