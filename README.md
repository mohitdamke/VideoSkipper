# VideoSkipper 🎬🚫

<p align="center">
  <img src="https://github.com/user-attachments/assets/32763238-4a51-4bb5-91b3-8460cce2a055" width="140" alt="VideoSkipper App Icon"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/880dd6cf-bc06-497e-830a-25ff940baaf9" width="850" alt="VideoSkipper"/>
</p>

<p align="center">
  <b>Automatically skip unwanted Reels and Shorts using on-device OCR.</b>
</p>

---
**An Android app that automatically scrolls past short-form video content (Reels/Shorts) containing user-defined keywords — using on-device OCR, accessibility services, and synthetic gesture automation.**

VideoSkipper watches your screen in real time while you scroll Instagram Reels or YouTube Shorts, reads on-screen text using ML Kit's on-device OCR, and automatically swipes past any content matching your saved keyword list — no manual scrolling, no cloud processing, no data leaving your device.

---

## ✨ Features

- 🔍 **Real-time keyword detection** — OCR-based text recognition on live screen content
- 🤖 **Automatic scroll-past** — synthetic swipe gestures triggered on keyword match
- 💬 **Floating overlay bubble** — add keywords without leaving Instagram/YouTube
- 🔋 **Battery-conscious** — foreground-app gating, bitmap downscaling, auto-stop safeguards
- 🔒 **Fully on-device** — no network calls, no data collection, screenshots never persisted to disk
- 🎛️ **Independent toggles** — enable/disable text detection and image detection separately

---

## 🏗️ Architecture

Built with **Clean Architecture** principles — clear separation between UI, domain logic, and data sources, wired together with dependency injection.

```
presentation/          → Jetpack Compose UI (screens, floating overlay components)
viewmodel/             → MonitoringViewModel, TextViewModel (StateFlow-driven UI state)
domain/repository/     → Interfaces (KeywordRepository, MonitoringRepository, 
                          AutoScrollDetectionRepository, ScreenActionController)
data/repository/       → Implementations (Room-backed, DataStore-backed, ML Kit-backed)
service/               → PizzaDetectorAccessibilityService, OverlayService
di/                    → Hilt modules (DatabaseModule, RepositoryModule, etc.)
```

### Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose, Material 3 |
| DI | Hilt |
| Local storage | Room (keywords), DataStore (settings) |
| Concurrency | Kotlin Coroutines, Flow, Mutex |
| OCR | ML Kit Text Recognition (on-device) |
| Screen automation | AccessibilityService (`takeScreenshot()`, `dispatchGesture()`) |
| Overlay UI | `WindowManager` + Compose `ComposeView` |

---

## ⚙️ How It Works

1. **`OverlayService`** renders a draggable floating bubble (via `WindowManager`) with quick actions to add keywords and toggle detection — usable directly on top of Instagram/YouTube.
2. **`PizzaDetectorAccessibilityService`** listens for `TYPE_VIEW_SCROLLED` accessibility events from watched apps (Instagram, YouTube, TikTok).
3. On a genuine scroll event, it captures a screenshot (`takeScreenshot()`, API 30+), downscales it (1080px → 720px) to reduce memory/CPU load, and runs it through **ML Kit's on-device text recognizer**.
4. The recognized text is checked against the user's saved keyword list (`Active keywords from DB`).
5. **On a match** → a synthetic swipe gesture (`dispatchGesture()`) is dispatched, scrolling past the content automatically.
6. **No match** → nothing happens; the app returns to listening for the next scroll.

All of this only runs when:
- Text detection is toggled **ON**, and
- The watched app (Instagram/YouTube/TikTok) is the **actual foreground app**

---

## 🔋 Performance & Battery Engineering

This project went through several iterations to get detection both **fast** and **cheap**:

- **Debounce → continuous sampler**: an early debounce-based approach silently skipped any reel scrolled past faster than the debounce window — replaced with a lightweight polling loop gated by a "pending scroll" flag, checking almost every reel a user lands on.
- **Event-type precision**: switched from `TYPE_WINDOW_CONTENT_CHANGED` (fires constantly for video-progress ticks, captions animating, etc.) to `TYPE_VIEW_SCROLLED` (fires only on real scroll gestures) — this alone cut redundant detection cycles per reel from 5–6× down to exactly 1×.
- **Foreground-app gating**: zero screenshots or OCR calls while the watched app isn't in the foreground.
- **Bitmap lifecycle management**: every captured/scaled bitmap is recycled immediately after use inside a guaranteed `finally` block; verified with custom live-bitmap-count instrumentation during development.
- **Mutex-guarded detection cycles**: prevents overlapping screenshot/OCR work if scroll events fire faster than a single cycle can complete.
- **6-hour auto-stop safeguard**: detection automatically disables itself after 6 continuous hours to protect against battery drain from an unattended background service.

---

## 🐛 A Notable Bug Fix

Midway through development, the accessibility service reported itself as **"connected"** and **"enabled"** in system settings — yet received **zero events**, even from the app's own UI. After ruling out permissions, Android 13 restricted-settings, and OEM battery management, the root cause was found by dumping the runtime `AccessibilityServiceInfo` object and comparing it against the declared manifest config:

```
eventTypes = 0 (typeAllMask = -1)   ← config was NOT being applied at all
```

The actual bug: a single-character typo in the manifest's meta-data key —

```xml
<!-- Wrong: silently ignored by Android -->
<meta-data android:name="android.accessibility.service" .../>

<!-- Correct -->
<meta-data android:name="android.accessibilityservice" .../>
```

A one-character fix that had been silently breaking the entire detection pipeline.

---

## 📸 Screenshots

*(Add screenshots/GIFs here: floating bubble, keyword list screen, home screen with toggles, before/after auto-scroll demo)*

---

## 🚀 Setup

1. Clone the repo
2. Open in Android Studio (Giraffe or newer recommended)
3. Build and run on a device running **API 30+** (required for `takeScreenshot()`)
4. On first launch:
   - Grant **"Display over other apps"** permission (for the floating bubble)
   - Enable **VideoSkipper** under **Settings → Accessibility**
   - If sideloading on Android 13+, you may need to enable **"Allow restricted settings"** for the app under **App info → ⋮**

---

## ⚠️ Known Limitations

- Requires **API 30+** (Android 11) due to `takeScreenshot()` API dependency
- Accessibility-based automation apps face Play Store policy scrutiny; not currently published
- OCR accuracy depends on on-screen text clarity/contrast — heavily stylized captions may be missed
- Keyword matching is currently substring-based (e.g. `"cat"` would also match `"category"`)

---

## 🧭 Possible Future Improvements

- Word-boundary keyword matching (regex-based) to reduce false positives
- Image-based detection (currently a UI toggle exists but the pipeline isn't implemented)
- Per-app keyword lists instead of one global list
- Local on-device analytics dashboard (reels skipped, time saved)

---

## 📄 License

*(Add your license here — MIT, Apache 2.0, etc.)*
