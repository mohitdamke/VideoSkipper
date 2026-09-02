# 🚗 CarTalk

<p align="center">
  <strong>Real-time voice communication between nearby vehicles.</strong>
</p>

<p align="center">
  Discover nearby vehicles using Bluetooth Low Energy (BLE) and communicate through low-latency WebRTC voice calls — without sharing phone numbers.
</p>

<p align="center">
  <a href="https://www.youtube.com/watch?v=bMRLdE_ClGQ">
    <img
      src="https://img.youtube.com/vi/bMRLdE_ClGQ/maxresdefault.jpg"
      alt="CarTalk App Demo"
      width="700"
    />
  </a>
</p>

<p align="center">
  ▶️ <strong>Click the video to watch the CarTalk Demo</strong>
</p>


<p align="center">
  🎥 <strong>Watch the CarTalk Demo</strong>
</p>

---

## 📱 Overview

CarTalk is an Android application that enables drivers traveling near each other to discover and communicate with one another.

Instead of exchanging phone numbers, users can identify vehicles using their **car model and vehicle number**.

The application uses:

* 🔵 **Bluetooth Low Energy (BLE)** for nearby vehicle discovery
* 📡 **RSSI** to estimate proximity
* 📞 **WebRTC** for real-time voice communication
* 🔥 **Firebase** for signaling and backend services
* 🎨 **Jetpack Compose + Material 3** for the UI

The goal is simple:

> **Find a nearby vehicle → Select it → Start a voice call.**

---

## ✨ Features

### 🔍 Nearby Vehicle Discovery

Automatically discovers CarTalk users within Bluetooth range.

Each nearby vehicle can be identified using:

* Vehicle number
* Car model
* Bluetooth signal strength

### 📞 Real-Time Voice Calling

Uses **WebRTC** to establish low-latency peer-to-peer audio communication.

### 🔐 Privacy Focused

CarTalk is designed around vehicle-based identification instead of exposing personal phone numbers.

### 📡 BLE Proximity Detection

Bluetooth Low Energy is used to discover nearby CarTalk devices and retrieve their advertising information.

### 🔥 Firebase Signaling

Firebase is used as the signaling layer required to establish WebRTC connections between users.

### 🎨 Modern Android UI

The application is built completely with:

* Jetpack Compose
* Material 3
* Modern Android architecture

---

# 📸 Screenshots

<p align="center">
  <img
    src="https://github.com/user-attachments/assets/c209c228-6ce7-474d-a499-14552252c1da"
    width="220"
    alt="CarTalk Onboarding"
  />
  <img
    src="https://github.com/user-attachments/assets/5253a642-b957-46ef-beee-cfac1dad83cf"
    width="220"
    alt="CarTalk Discovery"
  />
  <img
    src="https://github.com/user-attachments/assets/610ba444-b1b0-4ea6-b130-4ba95592a123"
    width="220"
    alt="CarTalk Incoming Call"
  />
</p>

<p align="center">
  <img
    src="https://github.com/user-attachments/assets/8709648d-4620-4189-b6c7-ebb0cc546047"
    width="220"
    alt="CarTalk Calling"
  />
  <img
    src="https://github.com/user-attachments/assets/90d8e66e-c9e5-4c63-847a-d8f5c43a3ba9"
    width="220"
    alt="CarTalk Connected Call"
  />
  <img
    src="https://github.com/user-attachments/assets/f8f7b5ee-84fb-40ce-9fa4-62bf7a3c83b1"
    width="220"
    alt="CarTalk App Icon"
  />
</p>

---

# 🎥 Feature Demos

### 🔍 Vehicle Discovery & Connection

<p align="center">
  <video
    src="https://github.com/user-attachments/assets/69f5faa8-a521-4b4d-a1ea-34619d54aa91"
    width="600"
    controls
  ></video>
</p>

<p align="center">
  <i>Discovering and connecting with a nearby CarTalk user.</i>
</p>

<br>

### 📞 Real-Time Voice Communication

<p align="center">
  <video
    src="https://github.com/user-attachments/assets/6b335d54-dd93-4443-8815-8cdc4f369bb9"
    width="600"
    controls
  ></video>
</p>

<p align="center">
  <i>Real-time voice communication using WebRTC.</i>
</p>

---

# 🏗 Architecture

CarTalk follows a **modular Clean Architecture** approach.

```text
CarTalk
│
├── app
│   └── Application entry point
│
├── core
│   ├── common
│   ├── domain
│   ├── navigation
│   ├── ui
│   └── firebase
│
└── feature
    ├── onboarding
    │   ├── data
    │   ├── domain
    │   └── presentation
    │
    ├── nearby
    │   ├── data
    │   ├── domain
    │   └── presentation
    │
    └── calling
        ├── data
        ├── domain
        └── presentation
```

### Module Responsibilities

| Module                | Responsibility                                  |
| --------------------- | ----------------------------------------------- |
| `:app`                | Application entry point and main navigation     |
| `:core:common`        | Shared utilities and common components          |
| `:core:domain`        | Shared domain models and abstractions           |
| `:core:navigation`    | Application navigation                          |
| `:core:ui`            | Shared UI components and design system          |
| `:core:firebase`      | Firebase-related functionality                  |
| `:feature:onboarding` | User and vehicle setup                          |
| `:feature:nearby`     | BLE advertising, scanning and vehicle discovery |
| `:feature:calling`    | WebRTC voice calling                            |

Each feature is separated into:

```text
Data
 ↓
Domain
 ↓
Presentation
```

This keeps business logic independent from Android UI and infrastructure.

---

# 🛠 Tech Stack

### Android

* Kotlin
* Android SDK
* Jetpack Compose
* Material 3
* Compose Navigation
* Kotlin Coroutines
* Kotlin Flow

### Architecture

* Clean Architecture
* MVVM
* Multi-module architecture
* Repository pattern
* Dependency Injection

### Dependency Injection

* Hilt

### Nearby Communication

* Bluetooth Low Energy (BLE)
* BluetoothLeScanner
* BluetoothLeAdvertiser
* RSSI-based proximity estimation

### Voice Communication

* WebRTC
* Peer-to-peer audio communication

### Backend

* Firebase
* Firebase Realtime Database
* Firebase Analytics
* Firebase Crashlytics

---

# 🔄 How It Works

```text
┌─────────────────────┐
│     CarTalk User    │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ BLE Advertisement   │
│ Vehicle Information │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Nearby BLE Scanner  │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Nearby Vehicles     │
│ + RSSI              │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Select Vehicle      │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Firebase Signaling  │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ WebRTC Connection   │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Real-Time Voice Call│
└─────────────────────┘
```

---

# 🚦 Getting Started

## Requirements

* Android Studio
* Android SDK 26+
* Kotlin
* A physical Android device

> A physical device is strongly recommended because CarTalk relies on Bluetooth Low Energy and real-time WebRTC communication.

---

## Installation

### 1. Clone the repository

```bash
git clone https://github.com/mohitdamke/CarTalk.git
```

### 2. Open the project

Open the cloned project using Android Studio.

### 3. Configure Firebase

Add your Firebase configuration file:

```text
app/google-services.json
```

> Do not commit your `google-services.json` file to a public repository.

### 4. Build the project

Sync Gradle and build the application.

### 5. Run on a physical device

Install CarTalk on two Android devices and test:

```text
Device A
   ↕
BLE Discovery
   ↕
Device B

Device A
   ↕
Firebase Signaling
   ↕
WebRTC
   ↕
Device B
```

---

# 🔒 Privacy

CarTalk is designed to minimize the amount of personal information exposed during vehicle discovery.

The discovery experience is based on vehicle information rather than directly exposing a user's phone number.

However, **BLE discovery itself should not be treated as a security boundary**. Any information intentionally included in BLE advertisements can potentially be observed by other nearby Bluetooth scanners.

For production use, sensitive information should therefore never be placed directly into BLE advertisements.

---

# 🧪 Current Status

CarTalk is currently an **MVP / experimental project** focused on validating:

* BLE-based nearby vehicle discovery
* Vehicle identification
* Proximity detection
* Firebase signaling
* WebRTC voice communication
* Modular Android architecture

The project is intended as a foundation for further development.

---

# 🤝 Contributing

Contributions, suggestions and improvements are welcome.

If you find a bug or have an idea for improving CarTalk, feel free to open an issue or submit a pull request.

---

# 👨‍💻 Author

**Mohit**

Android Developer

---

<p align="center">
  Made with ❤️ using Kotlin & Android
</p>

<p align="center">
  🚗 <strong>CarTalk — Connect with the cars around you.</strong>
</p>
