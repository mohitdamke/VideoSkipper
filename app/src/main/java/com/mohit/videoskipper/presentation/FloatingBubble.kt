package com.mohit.videoskipper.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun FloatingBubble(
    expanded: Boolean,
    featureOn: Boolean,
    onBubbleClick: () -> Unit,
    onToggle: () -> Unit,
    onDrag: (dx: Float, dy: Float) -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF6200EE))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                }
                .clickable { onBubbleClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
        }

        if (expanded) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.DarkGray)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable { onToggle() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (featureOn) "ON" else "OFF", color = Color.White)
                Switch(checked = featureOn, onCheckedChange = { onToggle() })
            }
        }
    }
}