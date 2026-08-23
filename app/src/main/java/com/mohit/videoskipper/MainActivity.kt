package com.mohit.videoskipper

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.mohit.videoskipper.navigation.NavigationScreen
import com.mohit.videoskipper.service.OverlayService
import com.mohit.videoskipper.ui.theme.VideoSkipperTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var featureOn by mutableStateOf(false)

    // Tracks whether we're mid-flow requesting the overlay permission, so onResume
    // doesn't fight with the callback that's about to fire right after this.
    private var awaitingPermissionResult = false

    private val overlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // NOTE: do NOT clear awaitingPermissionResult here. On the classic Android
            // lifecycle this callback actually fires BEFORE onResume(), so clearing the
            // flag here would let onResume() immediately overwrite the featureOn value
            // we're about to set below. onResume() itself clears the flag instead —
            // that way the skip works no matter which one runs first.
            if (Settings.canDrawOverlays(this)) {
                startOverlayService()
                featureOn = true
            }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        featureOn = OverlayService.isRunning

        setContent {
            VideoSkipperTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavigationScreen()
                }
            }
        }
    }
    fun openAccessibilitySettings(context: Context) {
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
        val expectedId = "${context.packageName}/${serviceClass.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(':').any { it.equals(expectedId, ignoreCase = true) }
    }

    override fun onResume() {
        super.onResume()

        if (awaitingPermissionResult) {
            // Skip the sync exactly once: either the permission callback already ran
            // (before this onResume) and set the correct featureOn value, or it's about
            // to run right after this. Either way, don't clobber it here.
            awaitingPermissionResult = false
        } else {
            featureOn = OverlayService.isRunning
        }
    }

    private fun checkAndRequestOverlayPermission() {
        if (Settings.canDrawOverlays(this)) {
            startOverlayService()
            featureOn = true
        } else {
            awaitingPermissionResult = true
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri()
            )
            overlayPermissionLauncher.launch(intent)
        }
    }

    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopOverlayService() {
        stopService(Intent(this, OverlayService::class.java))
    }
}
