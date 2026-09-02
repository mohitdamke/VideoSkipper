package com.mohit.videoskipper.presentation.components.floating

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohit.videoskipper.R
import com.mohit.videoskipper.ui.theme.Ink600
import com.mohit.videoskipper.ui.theme.Ocean400
import com.mohit.videoskipper.ui.theme.Ocean700
import com.mohit.videoskipper.ui.theme.Sand100
import com.mohit.videoskipper.ui.theme.Signal500
import com.mohit.videoskipper.ui.theme.SurfaceWhite
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun FloatingBubbleIcon(
    isTextDetectionOn: Boolean,
    onToggleTextDetection: () -> Unit,
    isImageDetectionOn: Boolean,
    onToggleImageDetection: () -> Unit,
    onSendText: (String) -> Unit,
    onImageClick: () -> Unit,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onTextInputFocusChange: (Boolean) -> Unit = {} // service uses this to toggle window flags
) {
    var expanded by remember { mutableStateOf(false) }
    var showTextInput by remember { mutableStateOf(false) }

    val isAnyDetectionOn = isTextDetectionOn || isImageDetectionOn

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        AnimatedVisibility(
            visible = showTextInput,
            enter = fadeIn(tween(150)) + expandVertically(tween(180)),
            exit = fadeOut(tween(120)) + shrinkVertically(tween(150))
        ) {
            InlineTextInputCard(
                onSend = { text ->
                    onSendText(text)
                    showTextInput = false
                    expanded = false
                    onTextInputFocusChange(false)
                },
                onFocusChange = onTextInputFocusChange
            )
        }

        AnimatedVisibility(
            visible = expanded && !showTextInput,
            enter = fadeIn(tween(150)) + expandVertically(tween(180)),
            exit = fadeOut(tween(120)) + shrinkVertically(tween(150))
        ) {
            Card(
                modifier = Modifier.shadow(10.dp, RoundedCornerShape(22.dp), clip = false),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(min = 208.dp)
                        .padding(vertical = 8.dp)
                ) {
                    DetectionRow(
                        icon = Icons.Filled.TextFields,
                        label = "Text",
                        isOn = isTextDetectionOn,
                        onIconClick = {
                            showTextInput = true
                            onTextInputFocusChange(true)
                        },
                        onToggle = onToggleTextDetection
                    )

                    // Image detection — coming soon, hidden from the UI for now.
                    // DetectionRow(
                    //     icon = Icons.Filled.Image,
                    //     label = "Image detection",
                    //     isOn = isImageDetectionOn,
                    //     onIconClick = onImageClick,
                    //     onToggle = onToggleImageDetection
                    // )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(8.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(Ocean700, Ocean400))
                )
                // Signal ring = at least one detection type is live
                .then(
                    if (isAnyDetectionOn) {
                        Modifier.border(2.dp, Signal500, CircleShape)
                    } else Modifier
                )
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                }
                .clickable {
                    if (showTextInput) {
                        showTextInput = false
                        onTextInputFocusChange(false)
                    }
                    expanded = !expanded
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.videoskipper),
                contentDescription = null,
                modifier = Modifier.size(70.dp)
            )
        }
    }
}

@Composable
private fun DetectionRow(
    icon: ImageVector,
    label: String,
    isOn: Boolean,
    onIconClick: () -> Unit,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isOn) Ocean700 else Sand100)
                .clickable(onClick = onIconClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isOn) SurfaceWhite else Ink600,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Ink600,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = isOn,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = SurfaceWhite,
                checkedTrackColor = Signal500,
                uncheckedThumbColor = SurfaceWhite,
                uncheckedTrackColor = Sand100,
                uncheckedBorderColor = Sand100
            ),
            modifier = Modifier.size(width = 40.dp, height = 24.dp)
        )
    }
}

@Composable
private fun InlineTextInputCard(
    onSend: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        delay(150.milliseconds) // small delay so WM flag change lands before requesting focus
        focusRequester.requestFocus()
    }

    Card(
        modifier = Modifier.shadow(10.dp, RoundedCornerShape(28.dp), clip = false),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .width(180.dp)
                    .focusRequester(focusRequester),
                placeholder = { Text("e.g. pizza", fontSize = 13.sp, color = Ink600.copy(alpha = 0.5f)) },
                singleLine = true,
                shape = RoundedCornerShape(50.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (text.isNotBlank()) {
                            onSend(text.trim())
                            focusManager.clearFocus()
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Sand100.copy(alpha = 0.4f),
                    unfocusedContainerColor = Sand100.copy(alpha = 0.4f),
                    focusedBorderColor = Ocean700,
                    unfocusedBorderColor = Sand100
                )
            )

            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSend(text.trim())
                        focusManager.clearFocus()
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Ocean700
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Send",
                    tint = if (text.isNotBlank()) Ocean700 else Ink600.copy(alpha = 0.35f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FloatingBubbleIconPreview() {
    Box(modifier = Modifier.size(260.dp), contentAlignment = Alignment.Center) {
        FloatingBubbleIcon(
            isTextDetectionOn = true,
            onToggleTextDetection = {},
            isImageDetectionOn = false,
            onToggleImageDetection = {},
            onSendText = {},
            onImageClick = {},
            onDrag = { _, _ -> }
        )
    }
}