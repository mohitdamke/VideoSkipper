package com.mohit.videoskipper.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.mohit.videoskipper.R
import com.mohit.videoskipper.domain.repository.KeywordRepository
import com.mohit.videoskipper.domain.repository.MonitoringRepository
import com.mohit.videoskipper.presentation.components.floating.FloatingBubbleIcon
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OverlayService : Service() {

    companion object {
        @Volatile
        var isRunning: Boolean = false
            private set
    }

    @Inject lateinit var keywordRepository: KeywordRepository
    @Inject lateinit var monitoringRepository: MonitoringRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var windowManager: WindowManager
    private lateinit var lifecycleOwner: OverlayLifecycleOwner
    private lateinit var params: WindowManager.LayoutParams

    private var bubbleView: ComposeView? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        lifecycleOwner = OverlayLifecycleOwner()
        lifecycleOwner.onCreate()
        startForegroundNotification()
        showBubble()
    }

    private fun startForegroundNotification() {
        val channelId = "overlay_channel"
        val channel = NotificationChannel(
            channelId, "VideoSkipper Overlay", NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("VideoSkipper running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    private fun showBubble() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 300
        }

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setContent {
                // Collected directly from the repository flows — this composition IS
                // the "ViewModel" here since a bare Service has no ViewModelStoreOwner.
                val isTextDetectionOn by monitoringRepository.isTextDetectionEnabled()
                    .collectAsState(initial = false)
                val isImageDetectionOn by monitoringRepository.isImageDetectionEnabled()
                    .collectAsState(initial = false)

                FloatingBubbleIcon(
                    isTextDetectionOn = isTextDetectionOn,
                    onToggleTextDetection = {
                        serviceScope.launch {
                            monitoringRepository.setTextDetectionEnabled(!isTextDetectionOn)
                        }
                    },
                    isImageDetectionOn = isImageDetectionOn,
                    onToggleImageDetection = {
                        serviceScope.launch {
                            monitoringRepository.setImageDetectionEnabled(!isImageDetectionOn)
                        }
                    },
                    onSendText = { text ->
                        serviceScope.launch {
                            keywordRepository.addKeyword(text)
                        }
                    },
                    onImageClick = { /* TODO: image keyword flow */ },
                    onDrag = { dx, dy ->
                        params.x += dx.toInt()
                        params.y += dy.toInt()
                        windowManager.updateViewLayout(this, params)
                    },
                    onTextInputFocusChange = { focused ->
                        toggleFocusable(focused)
                    }
                )
            }
        }

        bubbleView = view
        windowManager.addView(view, params)
    }

    private fun toggleFocusable(focusable: Boolean) {
        val view = bubbleView ?: return

        params.flags = if (focusable) {
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        } else {
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }
        params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE

        windowManager.updateViewLayout(view, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        bubbleView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: IllegalArgumentException) {
                // View wasn't attached — safe to ignore
            }
        }
        bubbleView = null
        if (::lifecycleOwner.isInitialized) {
            lifecycleOwner.onDestroy()
        }
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}