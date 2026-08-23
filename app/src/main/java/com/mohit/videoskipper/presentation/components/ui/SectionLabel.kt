package com.mohit.videoskipper.presentation.components.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohit.videoskipper.ui.theme.Ink400

/** Small uppercase "eyebrow" label used above a group of content, e.g. DETECTION TYPE. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = Ink400,
        modifier = modifier.padding(start = 4.dp)
    )
}