package com.mohit.videoskipper.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * "Signal Scan" palette — built around the app's existing brand blue
 * (the bubble gradient) so nothing you already shipped clashes with it.
 *
 *  Ocean   — brand / primary actions, headers, active icon chips
 *  Signal  — the "this is live / detecting" accent (replaces the old green)
 *  Coral   — destructive actions only (delete)
 *  Sand    — warm paper background, keeps the app from feeling like a
 *            generic white Material app
 *  Ink     — text
 */

// Brand — Ocean
val Ocean900 = Color(0xFF082C50)
val Ocean700 = Color(0xFF0C447C) // existing brand blue, unchanged
val Ocean400 = Color(0xFF4C9CE8) // existing gradient stop, unchanged
val Ocean100 = Color(0xFFDCEAFB)
val Ocean50  = Color(0xFFEFF6FD)

// Accent — Signal (the "live / detecting" color)
val Signal500 = Color(0xFF12B886)
val Signal100 = Color(0xFFD9F5EA)

// Destructive — Coral
val Coral500 = Color(0xFFE4572E)
val Coral100 = Color(0xFFFBE4DC)

// Neutral — Sand (warm paper, not stark white)
val Sand50  = Color(0xFFF6F2EB)
val Sand100 = Color(0xFFEFE8DA)
val Sand200 = Color(0xFFE3D9C4)

// Text
val Ink900 = Color(0xFF17222E)
val Ink600 = Color(0xFF4B5A68)
val Ink400 = Color(0xFF8A94A0)

val SurfaceWhite = Color(0xFFFFFFFF)