package com.mohit.videoskipper

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.mohit.videoskipper.presentation.CategoryListScreen
import com.mohit.videoskipper.presentation.CategoryScreen
import com.mohit.videoskipper.presentation.SubCategoryListScreen
import com.mohit.videoskipper.presentation.defaultCategories
import com.mohit.videoskipper.service.OverlayService
import com.mohit.videoskipper.ui.theme.VideoSkipperTheme

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        featureOn = OverlayService.isRunning

        setContent {
            VideoSkipperTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        featureOn = featureOn,
                        onFeatureToggle = { checked ->
                            if (checked) {
                                checkAndRequestOverlayPermission()
                            } else {
                                stopOverlayService()
                                featureOn = false
                            }
                        }
                    )
                }
            }
        }
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

@androidx.compose.runtime.Composable
private fun MainScreen(
    featureOn: Boolean,
    onFeatureToggle: (Boolean) -> Unit
) {
    var categories by remember { mutableStateOf(defaultCategories()) }
    var screen by remember { mutableStateOf<CategoryScreen>(CategoryScreen.List) }

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable Floating Icon", style = MaterialTheme.typography.titleMedium)
            Switch(checked = featureOn, onCheckedChange = onFeatureToggle)
        }

        Divider()

        when (val current = screen) {
            is CategoryScreen.List -> {
                CategoryListScreen(
                    categories = categories,
                    onCategoryClick = { category ->
                        screen = CategoryScreen.SubList(category.id)
                    }
                )
            }
            is CategoryScreen.SubList -> {
                val category = categories.firstOrNull { it.id == current.categoryId }
                if (category != null) {
                    SubCategoryListScreen(
                        category = category,
                        onBack = { screen = CategoryScreen.List },
                        onItemToggle = { itemId, checked ->
                            categories = categories.map { cat ->
                                if (cat.id == category.id) {
                                    cat.copy(items = cat.items.map { item ->
                                        if (item.id == itemId) item.copy(isBlocked = checked) else item
                                    })
                                } else cat
                            }
                        }
                    )
                }
            }
        }
    }
}