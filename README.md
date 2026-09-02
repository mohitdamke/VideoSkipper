# 🎬 VideoSkipper

<p align="center">
  <img
    src="https://github.com/user-attachments/assets/149c5644-e67e-4aca-a049-4d127857df7a"
    width="180"
    alt="VideoSkipper App Icon"
  />
</p>

<h1 align="center">VideoSkipper</h1>

<p align="center">
  <strong>Automatically skip unwanted Reels & Shorts using on-device OCR.</strong>
</p>

<p align="center">
  An Android automation app that detects user-defined keywords on short-form videos
  and automatically scrolls past matching content.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?style=for-the-badge" alt="Android"/>
  <img src="https://img.shields.io/badge/Language-Kotlin-purple?style=for-the-badge" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-blue?style=for-the-badge" alt="Jetpack Compose"/>
  <img src="https://img.shields.io/badge/Min%20SDK-30-orange?style=for-the-badge" alt="Minimum SDK"/>
</p>

---

# 🎥 Demo

<p align="center">
  <img
    src="demo.gif"
    width="600"
    alt="VideoSkipper Demo"
  />
</p>

<p align="center">
  <i>VideoSkipper automatically detects unwanted content and skips it.</i>
</p>

### Full Demo

<p align="center">
  <a href="https://www.youtube.com/watch?v=w0JSiymhGUY">
    ▶️ Watch the full VideoSkipper demo on YouTube
  </a>
</p>

---

# 📸 Screenshots

### VideoSkipper Demo 1

https://github.com/user-attachments/assets/069616eb-4ffc-4680-a73f-2359ddd4c0ae

### VideoSkipper Demo 2

https://github.com/user-attachments/assets/2676f585-8ca7-40f2-a467-a997b8366e7d

<p align="center">
  <i>VideoSkipper running on top of short-form video applications.</i>
</p>

---

# 📱 What is VideoSkipper?

**VideoSkipper** automatically skips short-form video content when it detects user-defined keywords on the screen.

It works with apps such as:

* Instagram Reels

VideoSkipper monitors the foreground screen, captures screenshots when a genuine scroll occurs, extracts visible text using **Google ML Kit's on-device OCR**, and checks the detected text against the user's saved keywords.

If a keyword matches, VideoSkipper automatically performs a swipe gesture to move to the next video.

## Basic Workflow

```text
User Scrolls
     │
     ▼
AccessibilityService
     │
     ▼
Detect Scroll Event
     │
     ▼
Capture Screenshot
     │
     ▼
Downscale Bitmap
     │
     ▼
ML Kit OCR
     │
     ▼
Extract Text
     │
     ▼
Compare Keywords
     │
     ├───────────────┐
     │               │
   Match           No Match
     │               │
     ▼               ▼
Swipe Next       Do Nothing
 Video
```

Everything happens **locally on the device**.

---

# ✨ Features

<table>
<tr>
<td width="50%">

### 🔍 Real-Time Detection

Detects text appearing on Reels and Shorts using on-device OCR.

</td>

<td width="50%">

### 🤖 Automatic Skipping

Automatically performs a swipe when a configured keyword is detected.

</td>
</tr>

<tr>
<td width="50%">

### 💬 Floating Bubble

Add keywords and control detection without leaving the currently opened app.

</td>

<td width="50%">

### 🔋 Battery Conscious

Uses event-based detection, foreground-app gating, bitmap optimization and automatic safeguards.

</td>
</tr>

<tr>
<td width="50%">

### 🔒 Fully On-Device

OCR processing happens locally. Screenshots are not uploaded to a server.

</td>

<td width="50%">

### 🎛️ Independent Controls

Text and image detection can be controlled independently.

</td>
</tr>
</table>

---

# 🏗️ Architecture

VideoSkipper follows a **Clean Architecture + MVVM** approach with clear separation between presentation, domain logic and data sources.

```text
VideoSkipper
│
├── presentation
│   ├── screens
│   ├── components
│   └── floating overlay UI
│
├── viewmodel
│   ├── MonitoringViewModel
│   └── TextViewModel
│
├── domain
│   └── repository
│       ├── KeywordRepository
│       ├── MonitoringRepository
│       ├── AutoScrollDetectionRepository
│       └── ScreenActionController
│
├── data
│   └── repository
│       ├── Room-backed repositories
│       ├── DataStore-backed repositories
│       └── ML Kit-backed repositories
│
├── service
│   ├── PizzaDetectorAccessibilityService
│   └── OverlayService
│
└── di
    ├── DatabaseModule
    └── RepositoryModule
```

## Architecture Flow

```text
┌───────────────────┐
│    Compose UI     │
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐
│     ViewModel     │
│     StateFlow     │
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐
│   Domain Layer    │
│    Repository     │
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐
│    Data Layer     │
│ Room / DataStore  │
│      / ML Kit     │
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐
│ Android Services  │
│ Accessibility /   │
│   WindowManager   │
└───────────────────┘
```

---

# 🛠️ Tech Stack

| Category                | Technology                               |
| ----------------------- | ---------------------------------------- |
| Language                | Kotlin                                   |
| UI                      | Jetpack Compose                          |
| Design                  | Material 3                               |
| Architecture            | Clean Architecture + MVVM                |
| Dependency Injection    | Hilt                                     |
| Local Database          | Room                                     |
| Preferences             | DataStore                                |
| Concurrency             | Kotlin Coroutines                        |
| Reactive State          | Kotlin Flow / StateFlow                  |
| OCR                     | Google ML Kit Text Recognition           |
| Screen Capture          | AccessibilityService `takeScreenshot()`  |
| Automation              | AccessibilityService `dispatchGesture()` |
| Overlay                 | `WindowManager` + `ComposeView`          |
| Minimum Android Version | Android 11 / API 30                      |

---

# ⚙️ How It Works

## 1. Floating Overlay

`OverlayService` creates a draggable floating bubble using `WindowManager`.

The overlay allows users to:

* Add keywords
* Enable/disable detection
* Control VideoSkipper without leaving Instagram, YouTube or TikTok

---

## 2. Detecting Scroll Events

`PizzaDetectorAccessibilityService` listens for accessibility events generated by the watched application.

Instead of reacting to every accessibility event, VideoSkipper focuses on:

```text
TYPE_VIEW_SCROLLED
```

This prevents unnecessary OCR processing caused by frequent UI updates such as:

* Video progress updates
* Caption animations
* UI changes
* Content refreshes

---

## 3. Screenshot Capture

When a genuine scroll is detected, the accessibility service captures the screen using:

```kotlin
takeScreenshot()
```

The captured bitmap is then downscaled before OCR processing to reduce memory usage and CPU consumption.

---

## 4. On-Device OCR

The processed bitmap is passed to **ML Kit Text Recognition**.

```text
Screenshot
    ↓
Bitmap Processing
    ↓
ML Kit
    ↓
Recognized Text
```

No cloud OCR service is required.

---

## 5. Keyword Matching

The recognized text is compared against the user's active keyword list stored locally.

```text
Detected Text
      ↓
Active Keywords
      ↓
String Matching
      ↓
Keyword Found?
```

If a keyword is detected, VideoSkipper triggers the automatic scrolling mechanism.

---

## 6. Automatic Swipe

The service uses:

```kotlin
dispatchGesture()
```

to perform a synthetic swipe and move past the current video.

```text
Keyword Found
      │
      ▼
dispatchGesture()
      │
      ▼
Swipe Up
      │
      ▼
Next Reel / Short
```

---

# 🔋 Performance & Battery Engineering

Performance was a major part of the implementation.

## Event Precision

The initial implementation relied on:

```text
TYPE_WINDOW_CONTENT_CHANGED
```

This produced excessive detection cycles because the event can fire frequently while content changes.

It was replaced with:

```text
TYPE_VIEW_SCROLLED
```

This significantly reduces unnecessary screenshot and OCR operations.

## Foreground-App Gating

OCR processing only runs when a supported application is actually in the foreground.

```text
Instagram / YouTube / TikTok
             │
             ▼
         App Foreground?
           /       \
         YES        NO
          │          │
          ▼          ▼
      Run OCR     Do Nothing
```

## Bitmap Optimization

Screenshots are downscaled before being processed.

```text
1080px Screenshot
       ↓
Bitmap Downscale
       ↓
720px Processing Image
       ↓
ML Kit OCR
```

## Mutex Protection

Detection cycles are protected using a `Mutex`.

This prevents multiple screenshot/OCR operations from running simultaneously when scroll events occur rapidly.

## Automatic Safety Stop

VideoSkipper includes a **6-hour automatic stop safeguard** to prevent an unattended accessibility detection process from continuously consuming battery.

---

# 🐛 Notable Bug Fix

During development, the accessibility service appeared as:

```text
Connected
Enabled
```

inside Android system settings, but it received **zero accessibility events**.

After debugging the runtime `AccessibilityServiceInfo`, the following was discovered:

```text
eventTypes = 0
typeAllMask = -1
```

The service configuration was not being applied.

The root cause was a one-character typo in the manifest metadata key.

### ❌ Incorrect

```xml
<meta-data
    android:name="android.accessibility.service"
    ... />
```

### ✅ Correct

```xml
<meta-data
    android:name="android.accessibilityservice"
    ... />
```

Android silently ignored the incorrect metadata key, causing the entire event-processing pipeline to fail.

---

# 🚀 Getting Started

## Requirements

* Android Studio
* Kotlin
* Android SDK
* Physical Android device
* Android 11 / API 30 or higher

A physical device is required because VideoSkipper depends on Android Accessibility APIs and `takeScreenshot()`.

---

# 📦 Installation

## 1. Clone the repository

```bash
git clone https://github.com/mohitdamke/VideoSkipper.git
```

## 2. Open the project

Open the project in Android Studio.

## 3. Build the application

Allow Gradle to sync and build the project.

## 4. Install on a physical device

Run the application on an Android 11+ device.

---

# 🔐 Required Permissions

VideoSkipper requires special Android permissions because it interacts with other applications.

## Display Over Other Apps

Required for the floating overlay bubble.

```text
Settings
   ↓
Apps
   ↓
Special App Access
   ↓
Display over other apps
   ↓
VideoSkipper
   ↓
Allow
```

## Accessibility Service

Enable VideoSkipper from:

```text
Settings
   ↓
Accessibility
   ↓
Installed Apps / Downloaded Apps
   ↓
VideoSkipper
   ↓
Enable
```

## Android 13+

If the application was installed through sideloading, Android may require:

```text
App Info
   ↓
⋮
   ↓
Allow Restricted Settings
```

before the Accessibility Service can be enabled.

---

# ⚠️ Limitations

* Requires **Android 11 / API 30+**
* OCR accuracy depends on text visibility, size, contrast and styling
* Highly stylized or animated text may not be detected
* Current keyword matching is substring-based
* Accessibility-based automation applications may face Google Play policy restrictions
* Image detection UI exists, but the complete image-detection pipeline is not currently implemented
* Continuous screen analysis can still consume battery despite optimization

---

# 🔒 Privacy

VideoSkipper is designed around **on-device processing**.

The OCR pipeline does not require uploading screenshots to a remote server.

Captured screen content is processed locally and is not intentionally persisted to disk as part of the detection pipeline.

> Accessibility services have powerful capabilities on Android. Users should only enable VideoSkipper if they understand and trust the permissions being granted.

---

# 📂 Project Highlights

This project demonstrates practical Android engineering concepts including:

* Jetpack Compose
* MVVM
* Clean Architecture
* Hilt Dependency Injection
* Room
* DataStore
* Kotlin Coroutines
* Kotlin Flow
* Accessibility Services
* `takeScreenshot()`
* `dispatchGesture()`
* WindowManager overlays
* ML Kit OCR
* Bitmap optimization
* Concurrency control
* Battery-conscious background processing

---

# 📄 License

This project currently does not specify a license.

If you plan to make the repository open source, consider adding an appropriate license such as **MIT** or **Apache-2.0**.

---

# 👨‍💻 Author

<p align="center">
  <strong>Mohit Damke</strong>
  <br/>
  Android Developer
</p>

---

<p align="center">
  Made with ❤️ using Kotlin & Android
</p>

<p align="center">
  🎬 <strong>VideoSkipper — Scroll less. See what you want.</strong>
</p>
