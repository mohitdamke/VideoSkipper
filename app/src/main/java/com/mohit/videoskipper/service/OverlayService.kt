package com.mohit.videoskipper.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.mohit.videoskipper.R
import com.mohit.videoskipper.presentation.FloatingBubble

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubbleView: ComposeView? = null
    private var expanded by mutableStateOf(false)
    private var featureOn by mutableStateOf(false)

    override fun onCreate() {
        super.onCreate()
        OverlayLifecycleOwner.onCreate()
        startForegroundNotification()
        showBubble()
    }

    private fun startForegroundNotification() {
        val channelId = "overlay_channel"
        val channel = NotificationChannel(
            channelId, "Swipii Overlay", NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Swipii running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    @SuppressLint("ClickableViewAccessibility")
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
            setViewTreeLifecycleOwner(OverlayLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(OverlayLifecycleOwner)
            setContent {
                FloatingBubble(
                    expanded = expanded,
                    featureOn = featureOn,
                    onBubbleClick = { expanded = !expanded },
                    onToggle = { featureOn = !featureOn },
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
        bubbleView?.let { windowManager.removeView(it) }
        OverlayLifecycleOwner.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

}