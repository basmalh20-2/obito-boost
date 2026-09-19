# OBITO BOOST — Mobile Gaming Performance Suite

A professional Android app that scans your device, classifies its performance
tier, benchmarks real on-screen frame performance, and generates a
personalized gaming optimization profile — with a dark navy / glowing cyan /
gold visual identity built for a gaming content creator's brand.

---

## 1. What's inside

```
OBITO_BOOST/
├── app/src/main/java/com/obitoboost/app/
│   ├── data/           → models, ProfileManager, BackupManager
│   ├── data/database/  → GameDatabase (Free Fire, PUBG Mobile, ...)
│   ├── engine/          → RecommendationEngine (the "Smart Boost" brain)
│   ├── monitor/          → DeviceInfoCollector, PerformanceMonitor, GameDetector
│   ├── util/             → JsonUtil, ExportManager (result-card image + share)
│   ├── ui/screens/       → all 10 app screens
│   ├── ui/components/    → reusable cards, metric rows, buttons
│   ├── ui/theme/         → the Deep-Navy/Cyan/Gold brand theme
│   ├── MainActivity.kt   → navigation drawer + NavHost
│   └── AppViewModel.kt   → shared state connecting every screen
└── app/src/main/res/     → strings, colors, launcher icon, manifest resources
```

Every module maps directly to a section of the original spec (Device
Analysis, Gaming Performance, Game Detection, Smart Boost, Game Settings,
Optimization, Before/After, Profiles, Backup, Export).

## 2. Important honesty notes (read this first)

Modern Android deliberately blocks apps from reading or changing many things
for your security. OBITO BOOST is built to be transparent about this instead
of faking data:

- **FPS**: no app can read another app's (i.e. your game's) live frame rate.
  What OBITO BOOST measures is its **own** on-screen render smoothness during
  a Benchmark run, using `Choreographer` — a fair, honest proxy, clearly
  labeled as such in the UI.
- **CPU usage**: Android has blocked per-app/system CPU-usage reads for
  normal apps since Android 8. The app shows "N/A" rather than a made-up
  number, and explains why on the Gaming Performance screen.
- **System settings**: without root or `WRITE_SECURE_SETTINGS` (an
  ADB-only grant), a normal app cannot directly flip most protected Android
  settings. Where that's the case, OBITO BOOST gives you a button that opens
  the right system settings screen instead of pretending to apply it itself.

This matches requirement #19 of the original spec (never fake data).

## 3. Requirements

- **Android Studio** (Koala/2024.1 or newer) — free, from
  https://developer.android.com/studio
- A computer with ~10 GB free space (Android Studio + SDK + emulator images)
- Either an Android phone (Android 7.0 / API 24+) with a USB cable, **or**
  Android Studio's built-in emulator

## 4. First-time setup

1. Install **Android Studio** and open it. On first launch it will download
   the Android SDK — accept the defaults.
2. Choose **Open** (not "New Project") and select the `OBITO_BOOST` folder
   you downloaded from this chat.
3. Android Studio will notice there's no Gradle wrapper jar yet and offer to
   create one automatically — click **OK / Use Gradle wrapper**. If it
   doesn't prompt automatically, open the **Terminal** tab at the bottom of
   Android Studio and run:
   ```
   gradle wrapper --gradle-version 8.7
   ```
   (this requires Gradle to be installed once, or you can instead just let
   Android Studio's "Sync Project with Gradle Files" button — the little
   elephant/refresh icon — do it for you).
4. Click **Sync Now** in the banner that appears at the top of the editor.
   The first sync downloads all dependencies and can take several minutes.

## 5. Running on a real phone (recommended — for accurate battery/thermal data)

1. On your phone: **Settings → About phone → tap "Build number" 7 times**
   to unlock Developer Options.
2. **Settings → Developer options → enable "USB debugging"**.
3. Connect the phone via USB. Accept the "Allow USB debugging?" prompt on
   the phone.
4. In Android Studio, your phone's name should appear in the device
   dropdown at the top. Select it.
5. Click the green **Run ▶** button (or Shift+F10).
6. The app installs and launches automatically. On first launch, Android
   will ask you to allow OBITO BOOST's permissions (battery info) — accept
   them so Device Analysis works fully.

## 6. Running on the emulator instead

1. In Android Studio: **Tools → Device Manager → Create Device**.
2. Pick any modern phone profile (e.g. Pixel 8) and a system image with
   API 33 or 34, then Finish.
3. Select the emulator in the device dropdown and click **Run ▶**.
   (Note: emulators report simulated battery/thermal data, not real
   hardware — a real phone gives you the true picture.)

## 7. Building a shareable APK

1. **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
2. When it finishes, click the **"locate"** link in the notification, or
   find it at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```
3. Copy that file to your phone (or transfer it via USB/cloud) and tap it
   to install — you'll need to allow "Install from unknown sources" once.

This debug APK is fine for your own testing and content creation. If you
later want to publish it (e.g. Play Store), you'll need to create a signed
**release** build via **Build → Generate Signed Bundle / APK**.

## 8. Where to add more games later

Open `app/src/main/java/com/obitoboost/app/data/database/GameDatabase.kt`
and append a new `GameEntry` to the `games` list — nothing else in the app
needs to change; the Games screen, detector, and recommendation engine all
read from this one list automatically.

## 9. Where the brand colors live

`app/src/main/java/com/obitoboost/app/ui/theme/Color.kt` — change
`CyanGlow`, `Gold`, `NavyDeep` etc. there to retheme the entire app in one
place.

---

Built as a complete, from-scratch Android Studio project — no placeholders,
no missing wiring. Every screen reads and writes through the same
`AppViewModel`, so device scans, selected games, generated profiles, and
benchmark results all stay in sync across the app.
