package com.mohit.videoskipper.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Bitmap
import android.graphics.Path
import android.os.Build
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import com.mohit.videoskipper.data.usecase.DetectPizzaAndScrollUseCase
import com.mohit.videoskipper.domain.repository.MonitoringRepository
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class PizzaDetectorAccessibilityService : AccessibilityService() {

    @Inject lateinit var detectUseCase: DetectPizzaAndScrollUseCase
    @Inject lateinit var monitoringRepository: MonitoringRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var pollingJob: Job? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        observeMonitoringFlag()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used for polling; screenshot loop is driven separately (see startPolling)
    }

    override fun onInterrupt() {}

    /**
     * Single source of truth for whether scanning should run — driven by the
     * persisted DataStore flag, so this stays correct even if the service is
     * recreated by the OS independently of the app process / ViewModel.
     */
    private fun observeMonitoringFlag() {
        monitoringRepository.isTextDetectionEnabled()
            .onEach { enabled -> if (enabled) startPolling() else stopPolling() }
            .launchIn(serviceScope)
    }

    /** Called by the ViewModel for an immediate UI-driven response. */
    fun startMonitoring() = startPolling()
    fun stopMonitoring() = stopPolling()

    private fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = serviceScope.launch {
            while (isActive) {
                captureAndCheck()
                delay(1500)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun captureAndCheck() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        val bitmap = takeScreenshotSuspend() ?: return
        val match = detectUseCase(bitmap)
        bitmap.recycle()
        if (match != null) {
            withContext(Dispatchers.Main) { performScroll() }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun takeScreenshotSuspend(): Bitmap? =
        suspendCancellableCoroutine { cont ->
            takeScreenshot(
                Display.DEFAULT_DISPLAY,
                mainExecutor,
                object : TakeScreenshotCallback {
                    override fun onSuccess(result: ScreenshotResult) {
                        val bitmap = Bitmap.wrapHardwareBuffer(
                            result.hardwareBuffer, result.colorSpace
                        )?.copy(Bitmap.Config.ARGB_8888, false)
                        result.hardwareBuffer.close()
                        if (cont.isActive) cont.resume(bitmap) {}
                    }

                    override fun onFailure(errorCode: Int) {
                        if (cont.isActive) cont.resume(null) {}
                    }
                }
            )
        }

    private fun performScroll() {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        val path = Path().apply {
            moveTo(width / 2f, height * 0.75f)
            lineTo(width / 2f, height * 0.25f)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
            .build()

        dispatchGesture(gesture, null, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
        serviceScope.cancel()
        instance = null
    }

    companion object {
        var instance: PizzaDetectorAccessibilityService? = null
    }
}