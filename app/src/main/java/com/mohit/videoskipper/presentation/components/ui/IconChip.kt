package com.mohit.videoskipper.presentation.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mohit.videoskipper.ui.theme.Ink400
import com.mohit.videoskipper.ui.theme.Ocean700
import com.mohit.videoskipper.ui.theme.Sand100
import com.mohit.videoskipper.ui.theme.SurfaceWhite

@Composable
fun IconChip(
    icon: ImageVector,
    isOn: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (isOn) Ocean700 else Sand100.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isOn) SurfaceWhite else Ink400,
            modifier = Modifier.padding(10.dp)
        )
    }
}