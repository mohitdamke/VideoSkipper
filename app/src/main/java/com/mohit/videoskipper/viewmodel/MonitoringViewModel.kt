package com.mohit.videoskipper.viewmodel

import android.app.Application
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mohit.videoskipper.domain.repository.KeywordRepository
import com.mohit.videoskipper.domain.repository.MonitoringRepository
import com.mohit.videoskipper.service.TextDetectorAccessibilityService
import com.mohit.videoskipper.states.MonitoringUiState
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

@RequiresApi(Build.VERSION_CODES.R)
@HiltViewModel
class MonitoringViewModel @Inject constructor(
    application: Application,
    private val monitoringRepository: MonitoringRepository,
    private val keywordRepository: KeywordRepository
) : AndroidViewModel(application) {

    private val TAG = "HEEEEWaooo"

    private val _uiState = MutableStateFlow(MonitoringUiState())
    val uiState: StateFlow<MonitoringUiState> = _uiState.asStateFlow()

    // Tracks the last-applied value so we only act on actual transitions
    private var previousTextEnabledValue: Boolean? = null

    init {
        observeState()
        refreshAccessibilityServiceStatus()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun observeState() {
        combine(
            monitoringRepository.isTextDetectionEnabled(),
            monitoringRepository.isImageDetectionEnabled(),
            keywordRepository.getAllKeywords()
        ) { textEnabled, imageEnabled, keywords ->
            Triple(textEnabled, imageEnabled, keywords.count { it.isActive })
        }
            .onEach { (textEnabled, imageEnabled, activeCount) ->

                _uiState.update {
                    it.copy(
                        isTextDetectionEnabled = textEnabled,
                        isImageDetectionEnabled = imageEnabled,
                        activeKeywordCount = activeCount,
                        isLoading = false
                    )
                }

                syncTextDetectionServiceState(textEnabled)
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

    /** Call this when the screen resumes — accessibility permission can change outside the app. */
    fun refreshAccessibilityServiceStatus() {
        val enabled = isAccessibilityServiceEnabled(
            getApplication(),
            TextDetectorAccessibilityService::class.java
        )

        _uiState.update { it.copy(isAccessibilityServiceEnabled = enabled) }
    }

    @RequiresApi(Build.VERSION_CODES.R)
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

                // state flow above will pick this up and call syncTextDetectionServiceState,
                // but we also call it immediately for a snappier UI response
                syncTextDetectionServiceState(enabled)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to update text detection")
                }
            }
        }
    }

    fun onToggleImageDetection(enabled: Boolean) {

        if (enabled && !_uiState.value.isAccessibilityServiceEnabled) {

            _uiState.update {
                it.copy(errorMessage = "Enable accessibility permission first")
            }
            return
        }

        viewModelScope.launch {
            try {

                monitoringRepository.setImageDetectionEnabled(enabled)

                // TODO: sync with an image-detection service/use case once that pipeline exists
            } catch (e: Exception) {

                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to update image detection")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun syncTextDetectionServiceState(enabled: Boolean) {
        if (previousTextEnabledValue == enabled) return
        previousTextEnabledValue = enabled

        val service = TextDetectorAccessibilityService.instance ?: return

        if (enabled) {
            service.startMonitoring()
            service.triggerImmediateCheck()
        } else {
            service.stopMonitoring()
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun isAccessibilityServiceEnabled(
        context: android.content.Context,
        serviceClass: Class<*>
    ): Boolean {
        val expectedId = "${context.packageName}/${serviceClass.canonicalName}"

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: run {
            return false
        }


        val result = enabledServices.split(':')
            .any { it.equals(expectedId, ignoreCase = true) }


        return result
    }
}