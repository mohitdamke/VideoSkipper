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
import com.mohit.videoskipper.service.PizzaDetectorAccessibilityService
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

    init {
        Log.d(TAG, "MonitoringViewModel initialized")
        observeState()
        refreshAccessibilityServiceStatus()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun observeState() {
        Log.d(TAG, "observeState() started")

        combine(
            monitoringRepository.isTextDetectionEnabled(),
            monitoringRepository.isImageDetectionEnabled(),
            keywordRepository.getAllKeywords()
        ) { textEnabled, imageEnabled, keywords ->
            Triple(textEnabled, imageEnabled, keywords.count { it.isActive })
        }
            .onEach { (textEnabled, imageEnabled, activeCount) ->

                Log.d(
                    TAG,
                    "State Updated -> textEnabled=$textEnabled, imageEnabled=$imageEnabled, activeKeywordCount=$activeCount"
                )

                _uiState.update {
                    it.copy(
                        isTextDetectionEnabled = textEnabled,
                        isImageDetectionEnabled = imageEnabled,
                        activeKeywordCount = activeCount,
                        isLoading = false
                    )
                }

                Log.d(TAG, "Calling syncTextDetectionServiceState($textEnabled)")
                syncTextDetectionServiceState(textEnabled)
            }
            .catch { e ->
                Log.e(TAG, "observeState() error", e)

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
        Log.d(TAG, "refreshAccessibilityServiceStatus() called")

        val enabled = isAccessibilityServiceEnabled(
            getApplication(),
            PizzaDetectorAccessibilityService::class.java
        )

        Log.d(TAG, "Accessibility Enabled = $enabled")

        _uiState.update { it.copy(isAccessibilityServiceEnabled = enabled) }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun onToggleTextDetection(enabled: Boolean) {
        Log.d(TAG, "onToggleTextDetection($enabled)")

        if (enabled && !_uiState.value.isAccessibilityServiceEnabled) {
            Log.w(TAG, "Accessibility permission not granted")

            _uiState.update {
                it.copy(errorMessage = "Enable accessibility permission first")
            }
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Saving text detection state to repository: $enabled")

                monitoringRepository.setTextDetectionEnabled(enabled)

                Log.d(TAG, "Repository updated successfully")

                // state flow above will pick this up and call syncTextDetectionServiceState,
                // but we also call it immediately for a snappier UI response
                syncTextDetectionServiceState(enabled)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update text detection", e)

                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to update text detection")
                }
            }
        }
    }

    fun onToggleImageDetection(enabled: Boolean) {
        Log.d(TAG, "onToggleImageDetection($enabled)")

        if (enabled && !_uiState.value.isAccessibilityServiceEnabled) {
            Log.w(TAG, "Accessibility permission not granted")

            _uiState.update {
                it.copy(errorMessage = "Enable accessibility permission first")
            }
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Saving image detection state to repository: $enabled")

                monitoringRepository.setImageDetectionEnabled(enabled)

                Log.d(TAG, "Image detection repository updated successfully")

                // TODO: sync with an image-detection service/use case once that pipeline exists
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update image detection", e)

                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to update image detection")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun syncTextDetectionServiceState(enabled: Boolean) {
        Log.d(TAG, "syncTextDetectionServiceState(enabled=$enabled)")

        val service = PizzaDetectorAccessibilityService.instance

        if (service == null) {
            Log.w(TAG, "PizzaDetectorAccessibilityService.instance is NULL")
            return
        }

        if (enabled) {
            Log.d(TAG, "Calling service.startMonitoring()")
            service.startMonitoring()
        } else {
            Log.d(TAG, "Calling service.stopMonitoring()")
            service.stopMonitoring()
        }
    }

    fun onErrorShown() {
        Log.d(TAG, "onErrorShown()")
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun isAccessibilityServiceEnabled(
        context: android.content.Context,
        serviceClass: Class<*>
    ): Boolean {
        val expectedId = "${context.packageName}/${serviceClass.canonicalName}"

        Log.d(TAG, "Expected Service ID = $expectedId")

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: run {
            Log.d(TAG, "No accessibility services enabled")
            return false
        }

        Log.d(TAG, "Enabled Accessibility Services = $enabledServices")

        val result = enabledServices.split(':')
            .any { it.equals(expectedId, ignoreCase = true) }

        Log.d(TAG, "Accessibility Service Found = $result")

        return result
    }
}