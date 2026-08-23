package com.mohit.videoskipper.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Bitmap
import android.graphics.Path
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import com.mohit.videoskipper.domain.repository.AutoScrollDetectionRepository
import com.mohit.videoskipper.domain.repository.MonitoringRepository
import com.mohit.videoskipper.domain.repository.ScrollEventRepository
import com.mohit.videoskipper.events.AutoScrollDecision
import com.mohit.videoskipper.events.ScreenActionController
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class PizzaDetectorAccessibilityService : AccessibilityService(), ScreenActionController {
    private val TAG = "HEEEEWaooo"

    @Inject lateinit var scrollEventRepository: ScrollEventRepository
    @Inject lateinit var autoScrollDetectionRepository: AutoScrollDetectionRepository
    @Inject lateinit var monitoringRepository: MonitoringRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var samplerJob: Job? = null
    private var consecutiveAutoSwipes = 0

    // True only while one of watchedPackages is the actual foreground app.
    // The sampler sleeps entirely when this is false — no screenshots, no OCR,
    // no wasted CPU while the user is anywhere else (home screen, other apps,
    // even Instagram's settings/DM screens technically still count as the
    // app being foreground, which is fine — the eventType filtering on scroll
    // still applies inside runDetectionCycle via package checks upstream).
    @Volatile private var isTargetAppForeground = false
    @Volatile private var currentForegroundPackage: String? = null

    private val hasPendingScroll = AtomicBoolean(false)
    private val liveBitmapCount = AtomicInteger(0)

    // Marks when continuous detection started, so we can auto-stop after a
    // safety ceiling — prevents the service running unattended for many hours
    // straight and draining battery if the user forgot it was on.
    private var monitoringStartedAtElapsedMs: Long = 0L

    private val watchedPackages = setOf(
        "com.instagram.android",
        "com.google.android.youtube",
        "com.zhiliaoapp.musically"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Accessibility service connected")
        startSampler()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString()

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            currentForegroundPackage = pkg
            val wasTargetApp = isTargetAppForeground
            isTargetAppForeground = pkg in watchedPackages
            if (wasTargetApp != isTargetAppForeground) {
                Log.d(TAG, if (isTargetAppForeground)
                    "Target app '$pkg' came to foreground — sampler active"
                else
                    "Left target app (now '$pkg') — sampler idling, no captures")
            }
        }

        if (pkg !in watchedPackages) return

        // ONLY TYPE_VIEW_SCROLLED represents a real "user swiped to a new reel"
        // signal. TYPE_WINDOW_CONTENT_CHANGED fires constantly for unrelated UI
        // updates (video progress, caption animation, like-count ticking, mute
        // icon) and was causing repeated captures on the SAME reel — this was
        // the actual bug, not a timing issue.
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            hasPendingScroll.set(true)
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted")
    }

    private var lastCaptureAtMs = 0L

    private fun startSampler() {
        if (samplerJob?.isActive == true) return
        monitoringStartedAtElapsedMs = SystemClock.elapsedRealtime()
        Log.d(TAG, "Sampler starting — auto-stop in ${MAX_CONTINUOUS_RUNTIME_MS / 3_600_000}h")

        samplerJob = serviceScope.launch {
            while (isActive) {
                val elapsed = SystemClock.elapsedRealtime() - monitoringStartedAtElapsedMs
                if (elapsed >= MAX_CONTINUOUS_RUNTIME_MS) {
                    Log.w(TAG, "Reached ${MAX_CONTINUOUS_RUNTIME_MS / 3_600_000}h continuous runtime — auto-stopping detection")
                    monitoringRepository.setTextDetectionEnabled(false)
                    stopSampler()
                    break
                }

                if (!isTargetAppForeground) {
                    delay(IDLE_POLL_MS.milliseconds)
                    continue
                }

                if (hasPendingScroll.compareAndSet(true, false)) {
                    val now = SystemClock.elapsedRealtime()
                    val sinceLastCapture = now - lastCaptureAtMs
                    if (sinceLastCapture < MIN_CAPTURE_INTERVAL_MS) {
                        // A duplicate scroll signal for the same physical swipe —
                        // ignore it, don't double-capture the same reel.
                        delay(POLL_INTERVAL_MS.milliseconds)
                        continue
                    }
                    lastCaptureAtMs = now

                    delay(RENDER_SETTLE_MS.milliseconds)
                    Log.d(TAG, "Capturing single screenshot for this scroll")
                    runDetectionCycle()
                } else {
                    delay(POLL_INTERVAL_MS.milliseconds)
                }
            }
        }
    }

    private fun stopSampler() {
        samplerJob?.cancel()
        samplerJob = null
    }

    private suspend fun runDetectionCycle() {
        var rawBitmap: Bitmap? = null
        var scaledBitmap: Bitmap? = null

        try {
            rawBitmap = captureScreenRaw() ?: return
            scaledBitmap = downscaleForOcr(rawBitmap)

            val decision = autoScrollDetectionRepository.decide(scaledBitmap)

            when (decision) {
                is AutoScrollDecision.Skip -> {
                    Log.i(TAG, "Match found: '${decision.matchedKeyword}' — auto-scrolling")
                    if (consecutiveAutoSwipes >= MAX_CONSECUTIVE_AUTO_SWIPES) {
                        Log.w(TAG, "Hit max consecutive auto-swipes, cooling down")
                        consecutiveAutoSwipes = 0
                        return
                    }
                    consecutiveAutoSwipes++
                    performSwipeUp()
                    // The swipe itself fires new events -> hasPendingScroll
                    // gets set again -> next loop iteration captures once
                    // more for the newly landed reel. Still one shot per scroll.
                }
                AutoScrollDecision.Stay -> consecutiveAutoSwipes = 0
                AutoScrollDecision.DetectionDisabled -> consecutiveAutoSwipes = 0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Detection cycle failed", e)
        } finally {
            recycleBitmap(rawBitmap, "raw screenshot")
            if (scaledBitmap !== rawBitmap) {
                recycleBitmap(scaledBitmap, "scaled screenshot")
            }
        }
    }

    private fun recycleBitmap(bitmap: Bitmap?, label: String) {
        if (bitmap == null) return
        if (bitmap.isRecycled) return
        bitmap.recycle()
        val remaining = liveBitmapCount.decrementAndGet()
        Log.d(TAG, "Recycled $label bitmap — live bitmap count now: $remaining")
    }

    private suspend fun captureScreenRaw(): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Log.e(TAG, "takeScreenshot() unsupported below API 30")
            return null
        }
        val bitmap = takeScreenshotSuspend()
        if (bitmap != null) {
            val live = liveBitmapCount.incrementAndGet()
            Log.d(TAG, "Screenshot captured: ${bitmap.width}x${bitmap.height} — live bitmap count now: $live")
        }
        return bitmap
    }

    override suspend fun captureScreen(): Bitmap? = captureScreenRaw()

    private fun downscaleForOcr(source: Bitmap): Bitmap {
        val targetWidth = 720
        if (source.width <= targetWidth) return source

        val scale = targetWidth.toFloat() / source.width
        val targetHeight = (source.height * scale).toInt()
        val scaled = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)

        if (scaled !== source) {
            val live = liveBitmapCount.incrementAndGet()
            Log.d(TAG, "Downscaled to ${scaled.width}x${scaled.height} — live bitmap count now: $live")
            recycleBitmap(source, "raw screenshot (pre-scale)")
        }
        return scaled
    }

    override suspend fun performSwipeUp() {
        withContext(Dispatchers.Main) {
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

            dispatchGesture(
                gesture,
                object : GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        Log.d(TAG, "Swipe gesture completed")
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        Log.w(TAG, "Swipe gesture cancelled")
                    }
                },
                null
            )
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
                        Log.e(TAG, "takeScreenshot() failed, errorCode=$errorCode")
                        if (cont.isActive) cont.resume(null) {}
                    }
                }
            )
        }

    fun triggerImmediateCheck() {
        serviceScope.launch { runDetectionCycle() }
    }

    fun startMonitoring() = startSampler()
    fun stopMonitoring() = stopSampler()

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed — final live bitmap count: ${liveBitmapCount.get()}")
        stopSampler()
        serviceScope.cancel()
        instance = null
    }

    companion object {
        var instance: PizzaDetectorAccessibilityService? = null

        private const val RENDER_SETTLE_MS = 250L
        private const val POLL_INTERVAL_MS = 120L

        // Longer sleep interval used while NOT in a watched app — no point
        // polling every 120ms when there's nothing to detect at all.
        private const val IDLE_POLL_MS = 1_000L
        private const val MIN_CAPTURE_INTERVAL_MS = 600L
        private const val MAX_CONSECUTIVE_AUTO_SWIPES = 15

        // Safety ceiling: auto-disable detection after this many hours of
        // continuous running, protecting battery if left on unattended.
        private const val MAX_CONTINUOUS_RUNTIME_MS = 6 * 60 * 60 * 1000L // 6 hours
    }
}