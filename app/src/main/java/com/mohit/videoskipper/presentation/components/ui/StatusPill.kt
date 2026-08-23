package com.mohit.videoskipper.presentation.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mohit.videoskipper.ui.theme.Ink400
import com.mohit.videoskipper.ui.theme.Sand100
import com.mohit.videoskipper.ui.theme.Signal100
import com.mohit.videoskipper.ui.theme.Signal500

/** Small pill used for status text like "On" / "Off" / "3 keywords". */
@Composable
fun StatusPill(text: String, active: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (active) Signal100 else Sand100)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) Signal500 else Ink400
        )
    }
}