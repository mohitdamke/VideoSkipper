package com.mohit.videoskipper.presentation.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.mohit.videoskipper.ui.theme.Ocean400
import com.mohit.videoskipper.ui.theme.Ocean700
import com.mohit.videoskipper.ui.theme.Signal500

/**
 * The app's one signature element: a thin two-tone gradient line, standing
 * in for a "scan" — used directly under every top bar so the three screens
 * are instantly recognizable as one product.
 */
@Composable
fun ScanAccentDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Ocean700, Ocean400, Signal500)
                )
            )
    )
}



