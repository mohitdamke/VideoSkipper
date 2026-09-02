package com.mohit.videoskipper.presentation.components.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohit.videoskipper.ui.theme.Ink400

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.SemiBold
        ),
        color = Ink400,
        modifier = modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}