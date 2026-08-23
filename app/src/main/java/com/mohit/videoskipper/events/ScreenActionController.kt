package com.mohit.videoskipper.events

import android.graphics.Bitmap

/**
 * Abstraction over the Android-only APIs (takeScreenshot, dispatchGesture) that
 * only an AccessibilityService can call. The service implements this; repositories
 * consume it without needing to know it's a Service.
 */
interface ScreenActionController {
    suspend fun captureScreen(): Bitmap?
    suspend fun performSwipeUp()
}