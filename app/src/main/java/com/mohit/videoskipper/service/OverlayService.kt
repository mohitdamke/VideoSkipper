package com.mohit.videoskipper.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.mohit.videoskipper.MainActivity
import com.mohit.videoskipper.R
import com.mohit.videoskipper.presentation.components.FloatingBubbleIcon

class OverlayService : Service() {

    companion object {
        // Tracks whether the service is actually alive right now — MainActivity reads
        // this instead of Settings.canDrawOverlays(), which only reflects permission,
        // not whether the overlay is actually running.
        @Volatile
        var isRunning: Boolean = false
            private set
    }

    private lateinit var windowManager: WindowManager
    private var bubbleView: ComposeView? = null
    private lateinit var lifecycleOwner: OverlayLifecycleOwner

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

        val params = WindowManager.LayoutParams(
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

        bubbleView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setContent {
                FloatingBubbleIcon(
                    onClick = {
                        val intent = Intent(this@OverlayService, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        }
                        startActivity(intent)
                    },
                    onDrag = { dx, dy ->
                        params.x += dx.toInt()
                        params.y += dy.toInt()
                        windowManager.updateViewLayout(bubbleView, params)
                    }
                )
            }
        }

        windowManager.addView(bubbleView, params)
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
    }

    override fun onBind(intent: Intent?): IBinder? = null
}