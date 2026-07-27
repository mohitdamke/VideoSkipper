###  VideoSkipper — Smart Reel/Shorts Auto-Skip App
<img width="512" height="512" alt="appIcon" src="https://github.com/user-attachments/assets/32763238-4a51-4bb5-91b3-8460cce2a055" />


<img width="320" height="660" alt="2_reel_text_mode" src="https://github.com/user-attachments/assets/aecc8a2b-7e68-48c5-94ce-b75706eeaf78" />
<img width="320" height="660" alt="1_reel_bubble_tapped" src="https://github.com/user-attachments/assets/ecaf213d-45d1-41c6-bb41-203429f4e807" />
<img width="320" height="660" alt="4_app_home" src="https://github.com/user-attachments/assets/70dd48c6-c77d-4c1e-ba4b-83881706f006" />
<img width="320" height="660" alt="3_reel_crop_mode" src="https://github.com/user-attachments/assets/68d8bc11-cc1a-498a-841d-de33d72874f4" />
<img width="320" height="660" alt="5_app_text_list" src="https://github.com/user-attachments/assets/4d1ba035-3598-4ba5-91c1-4060339ab63f" />
<img width="320" height="660" alt="6_app_face_list" src="https://github.com/user-attachments/assets/9a693950-6dec-4f15-bcef-d111ed811eab" />


## Project Overview
I'm building an Android app called **Swipii** using **Kotlin + Jetpack Compose**. Its purpose is to eliminate the manual effort of scrolling past unwanted Reels/Shorts content (Instagram, YouTube Shorts, etc.) by using on-device ML to automatically detect and auto-skip content the user doesn't want to see — based on **face, audio, or text** they've flagged before.

## Core Problem
Users repeatedly see the same unwanted content while scrolling short-video feeds:
- The same trending song/audio in reel after reel
- The same person's face appearing repeatedly (e.g., a specific celebrity)
- The same category of content (e.g., food reels: pizza, vada pav, maggie, etc.)

Manually scrolling past these every time is repetitive and irritating.

## Solution
A floating overlay bubble (like Messenger's chat heads) that sits on top of any app. When active, it uses **one** of three selectable on-device ML detection modes (face / audio / text — user picks only one at a time for performance) to:
1. Let the user "flag" content they don't want (tap a button while viewing it)
2. Save a lightweight fingerprint/embedding of that face, audio, or text to local storage
3. Automatically detect matching content in future reels and auto-swipe past it within milliseconds — no manual scrolling needed

## Key Design Constraints
- **No GPU, no model training** — must use pretrained, on-device models only (ML Kit, TFLite pretrained models, or lightweight signal-processing algorithms)
- **Only one detection mode active at a time** (face OR audio OR text), selected via UI, to keep performance fast and battery-light
- Detection must run in near real-time (within milliseconds) so auto-swipe feels instant
- Store only lightweight fingerprints/embeddings locally (Room DB) — not raw images/audio clips — for speed, storage, and privacy
- All ML inference must be on-device (no cloud API calls, for speed and privacy)

## Technical Approach (agreed so far)
| Component | Approach |
|---|---|
| Screen content capture | `MediaProjection` API (screen frames) |
| Audio capture | `AudioPlaybackCapture` API (Android 10+) |
| Auto-swipe simulation | `AccessibilityService.dispatchGesture()` |
| Floating overlay UI | `WindowManager` + `SYSTEM_ALERT_WINDOW` permission + Jetpack Compose `ComposeView` inside a Foreground Service |
| Face detection | ML Kit Face Detection (presence) + MobileFaceNet/FaceNet TFLite (embedding + cosine similarity match) |
| Text detection | ML Kit Text Recognition v2 (OCR) + keyword/blocklist matching |
| Audio detection | Custom lightweight audio fingerprinting (Shazam-style spectral peak hashing) — no ML model needed |
| Local storage | Room DB (stores fingerprints/embeddings/keywords, not raw media) |

## Current Build Status
- ✅ Floating overlay bubble implemented (draggable, expandable, on/off toggle) using Foreground Service + WindowManager + Compose
- ⏳ Not yet built: text detection module, face detection module, audio fingerprinting module, accessibility-based auto-swipe, mode-selection UI, Room DB schema

## What I Need Help With
[Fill in your specific ask here — e.g., "Help me build the text detection module next" or "Review this code for bugs" or "Suggest UX improvements for the flagging flow"]

## Known Considerations / Risks
- Using AccessibilityService to auto-interact with Instagram/YouTube's UI is a legal/policy gray area (Play Store scrutiny + platform ToS) — worth keeping in mind for any public release strategy
- Simultaneous multi-mode detection (face + audio + text at once) is likely too slow/heavy for real-time use on typical phones — hence the single-mode-at-a-time design
