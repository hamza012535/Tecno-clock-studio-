# Tecno Clock Studio

**Tecno Clock Studio** is an open-source, Material 3 lock screen clock customization app engineered specifically for **Tecno smartphones running HiOS on Android 6 through Android 13+ (API 23–33+)**.

Built with **Kotlin**, **Jetpack Compose**, and **DataStore Preferences**, it adapts its rendering engine automatically to your Android version and HiOS environment:

- **Android 13+ (API 33+)**: Registers a native `ClockProviderPlugin` service (`ClockController` + `ComposeView` host) so Tecno Clock Studio faces appear in **Wallpaper & style**.
- **Android 6–12 (API 23–32)**: Runs a lightweight foreground `LockScreenOverlayService` using `WindowManager` (`TYPE_APPLICATION_OVERLAY` / `FLAG_SHOW_WHEN_LOCKED`) and a dedicated `CustomLockScreenActivity` with keyguard PIN/Pattern passthrough.
- **HiOS Compatibility Suite**: Built-in **HiOS Conflict Checker** for Magazine Lockscreen, **Battery Optimization Whitelist** shortcut for Phone Master, and **Wake on Lift** accelerometer trigger to work around HiOS's 15-second Always-On Display (AOD) timeout.

---

## Features

- **5 Base Clock Styles**: Minimal Digital, Bold Analog, Neon Pulse, Typography, and Weather Hub.
- **Deep Customization**: 6 bundled open-source fonts (`Space Grotesk`, `JetBrains Mono`, `Playfair Display`, `Bebas Neue`, `Orbitron`, `Plus Jakarta Sans`), solid & gradient digit/background color pickers, 12h/24h toggle, seconds toggle, and date formats.
- **Complications**: Weather, live battery percentage, next system alarm, and step count.
- **Wake Transitions**: Instant, Fade In, Slide Up, and Scale Pop animations.
- **Live Lock Screen Preview**: Simulate blurred wallpaper and unlock gestures before applying.

---

## Download

### Option 1: Install via Komi Store (Recommended)
1. Open the **Komi Store** app on your Android device.
2. Search for **Tecno Clock Studio**.
3. Tap **Install** — updates are indexed automatically from GitHub Releases.

### Option 2: Direct APK from GitHub Releases
1. Visit the [Releases](../../releases) page of this repository.
2. Expand **Assets** on the latest release and download `tecno-clock-studio-vX.Y.Z-release.apk`.
3. Tap the downloaded `.apk` file to install.

### Enabling "Install from unknown sources"
Because this app is sideloaded outside Google Play, Android will ask for permission the first time you install an APK:
- **Android 8–13+ (HiOS 5–13)**: When prompted, tap **Settings** -> toggle ON **Allow from this source** for your browser, file manager, or Komi Store, then press Back and tap **Install**.
- **Android 6–7 (HiOS 2–4)**: Open **Settings -> Security** -> enable **Unknown sources**, then open the downloaded APK again.

---

## Open-Source Distribution & F-Droid Compatibility

- **Komi Store**: Automatically discovers this public repository via its published, non-draft GitHub Releases and attached signed `.apk` assets.
- **F-Droid**: This project uses standard Gradle Kotlin DSL, bundles all fonts locally in `app/src/main/res/font/`, avoids proprietary runtime SDKs, and is structured for submission to **F-Droid** for reproducible open-source builds alongside Komi Store.

---

## Recommended GitHub Repository Topics

Add these topics in **GitHub -> About (gear icon) -> Topics** to maximize discovery on GitHub Search and Komi Store:

```text
android, lock-screen, clock, tecno, hios, customization, jetpack-compose, kotlin, f-droid, komi-store
```

---

## Architecture Overview

- `com.example.systemui.ClockProviderPluginService` & `TecnoClockProviderPlugin`: Android 13+ `ClockProviderPlugin` + `ClockController` inflating Jetpack Compose into SystemUI.
- `com.example.service.LockScreenOverlayService`: Android 6–12 `WindowManager` overlay service with screen on/off receivers and Wake-on-Lift accelerometer listener.
- `com.example.ui.lockscreen.CustomLockScreenActivity`: Live preview & keyguard passthrough lock activity.
- `com.example.data.ClockPreferencesRepository`: DataStore Preferences persistence.
- `com.example.compatibility.HiOsCompatibilityHelper`: API level routing, Magazine Lockscreen detection, and battery/overlay permission intents.
