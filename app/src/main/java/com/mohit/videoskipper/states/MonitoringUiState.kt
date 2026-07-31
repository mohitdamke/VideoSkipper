package com.mohit.videoskipper.states

data class MonitoringUiState(
    val isTextDetectionEnabled: Boolean = false,
    val isImageDetectionEnabled: Boolean = false,
    val isAccessibilityServiceEnabled: Boolean = false,
    val activeKeywordCount: Int = 0,
    val isLoading: Boolean = true,
    val lastDetectedKeyword: String? = null,
    val errorMessage: String? = null
) {
    // Text detection only actually works if BOTH the toggle is on AND the
    // accessibility service permission is granted — surface that clearly to the UI.
    val canMonitorText: Boolean
        get() = isTextDetectionEnabled && isAccessibilityServiceEnabled

    val canMonitorImage: Boolean
        get() = isImageDetectionEnabled && isAccessibilityServiceEnabled

    val isAnyDetectionEnabled: Boolean
        get() = isTextDetectionEnabled || isImageDetectionEnabled

    val statusMessage: String
        get() = when {
            !isAccessibilityServiceEnabled -> "Accessibility permission required"
            !isAnyDetectionEnabled -> "Detection is off"
            isTextDetectionEnabled && activeKeywordCount == 0 ->
                "No active keywords — add one to start"
            isTextDetectionEnabled && isImageDetectionEnabled ->
                "Watching text ($activeKeywordCount keyword(s)) and image"
            isTextDetectionEnabled ->
                "Watching for $activeKeywordCount keyword(s)"
            else -> "Watching for images"
        }
}