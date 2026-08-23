package com.mohit.videoskipper.states

data class TextDetectionUiState(
    val isTextDetectionEnabled: Boolean = false,
    val isAccessibilityServiceEnabled: Boolean = false,
    val activeKeywordCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val isActuallyRunning: Boolean
        get() = isTextDetectionEnabled && isAccessibilityServiceEnabled

    val statusMessage: String
        get() = when {
            !isAccessibilityServiceEnabled -> "Accessibility permission required"
            !isTextDetectionEnabled -> "Text detection is off"
            activeKeywordCount == 0 -> "No active keywords — add one to start"
            else -> "Watching for $activeKeywordCount keyword(s) — auto-skip active"
        }
}