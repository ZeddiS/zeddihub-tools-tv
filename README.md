# ZeddiHub Tools — TV

<p align="center">
  <img src="https://img.shields.io/badge/platform-Android%20TV%208.0%2B-3DDC84?logo=android&logoColor=white" alt="Android TV 8.0+">
  <img src="https://img.shields.io/badge/Kotlin-1.9-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Compose%20for%20TV-Material%203-4285F4?logo=jetpackcompose&logoColor=white" alt="Compose for TV">
  <img src="https://img.shields.io/github/v/release/ZeddiS/zeddihub-tools-tv?color=orange" alt="Release">
  <img src="https://img.shields.io/github/downloads/ZeddiS/zeddihub-tools-tv/total?color=blueviolet" alt="Downloads">
  <img src="https://img.shields.io/badge/license-Proprietary-red" alt="License">
</p>

> **Android TV companion to [ZeddiHub Tools Mobile](https://github.com/ZeddiS/zeddihub-tools-mobile) and [Desktop](https://github.com/ZeddiS/zeddihub-tools-desktop).** Sleep timer, network utilities, media remote a herní servery — to vše ovládané z gauče přes D-pad.

---

## Overview

**ZeddiHub Tools TV** je oficiální aplikace pro Android TV (testováno na Xiaomi TV Box S 3rd Gen, Google TV, Nvidia Shield). Aplikace je postavená na **Jetpack Compose for TV** + **Material 3 for TV**, plně optimalizovaná pro D-pad navigaci a 10-foot UI.

Aplikace je součástí ekosystému [zeddihub.eu](https://zeddihub.eu) — sdílí release-gating, telemetrii a admin panel s mobilní a desktopovou variantou.

---

## Features

### Sleep Timer (vlajková funkce)
- **Plovoucí odpočet** přes všechny aplikace (Netflix, YouTube, Disney+ — nezáleží)
- **4 rohy** obrazovky, výchozí pravý horní; pozice volně konfigurovatelná
- **Konfigurovatelné trigger tlačítko** — D-pad OK / Back / Home / Menu (long-press ~800 ms)
- **Rychlé možnosti** v overlay menu: ⏸ Pozastavit / ⏹ Zastavit / ⏻ Vypnout TV
- **Vypnutí v 0:00** uspí celý box i televizi (Device Admin + standby intent fallback)
- **Profily** — 15 / 30 / 45 / 60 / 90 / 120 min preset + vlastní hodnota
- **Ztlumení vstupu** — 10 s před uplynutím postupně sníží hlasitost (volitelné)

### Dashboard (10-foot home)
- Velké hodiny + datum
- Počasí (Open-Meteo, bez API klíče)
- Kalendář — nadcházející události (Google Calendar volitelně)
- Systémové info — RAM, storage, IP, uptime, Wi-Fi signál
- Quick-launch dlaždice na nejpoužívanější streaming aplikace

### Síťové nástroje
- **Wake-on-LAN** — magic packet z TV na PC / NAS / server
- **Ping & latence** — paralelní TCP reachability checks
- **Speed test** — download / upload / latence
- **Wi-Fi diagnostika** — RSSI, kanál, frekvence, link speed, SSID
- **Local network scan** — ARP / mDNS sweep s identifikací zařízení

### Media remote
- **Plex** ovládání (play/pause/seek/library)
- **Kodi** JSON-RPC remote
- **Galerie** ze SMB / NAS sd&iacute;leni (foto / video)
- **Streaming launcher** — rychlá dlaždice pro Netflix, YouTube, Disney+, HBO Max, Spotify atd.

### Game servers + push
- Live status pro **Rust PVE**, **CS2 AWP**, **CS:GO** servery (sdíleno s mobile/desktop)
- Player counts, uptime, mapy
- **FCM push** notifikace na TV — server down, eventy, oznámení (sdíleno s ZeddiHub push composerem)

### Auto-update
- **Release-gating** přes `https://zeddihub.eu/api/app-version.php` (kind=tv)
- Startup check + manuální tlačítko v Nastavení
- One-tap install — APK download + system installer
- Per-version release notes (CS / EN)
- Min-version gating — nucený update kdy backend vyžaduje

### Settings
- Téma (light / dark / system / amoled-black)
- Jazyk (en / cs)
- Trigger button časovače (OK / Back / Home / Menu)
- Pozice odpočtu (4 rohy / drag-and-drop)
- Notifikace (server down / eventy / oznámení)
- Wake-on-LAN cílová zařízení
- Plex / Kodi / SMB credentials
- About — verze, build, links, licence

---

## Screenshots

> Coming soon — screenshot set v `docs/screenshots/`.

---

## Install

### Z Releases (doporučeno)
1. Otevři latest release na [releases page](https://github.com/ZeddiS/zeddihub-tools-tv/releases/latest).
2. Stáhni `ZeddiHub-TV-<version>.apk`.
3. Na TV zapni **Instalace z neznámých zdrojů** (Settings → Apps → Security).
4. Nainstaluj APK přes ADB nebo přes `Send Files to TV` / `Downloader`.

```bash
adb install ZeddiHub-TV-0.1.0.apk
```

### Z mobilní instalace (sideload)
- Otevři `https://zeddihub.eu/tools/` v telefonu, stáhni APK, přepošli přes `LocalSend` na TV box.

---

## Build from source

Projekt obsahuje **bundled toolchain** (JDK 17 + Android SDK 34 + Gradle cache) — build běží bez nutnosti `ANDROID_HOME` v systému. Toolchain je sdílen s [zeddihub-tools-mobile](https://github.com/ZeddiS/zeddihub-tools-mobile).

```powershell
# PowerShell, z rootu repu
$env:JAVA_HOME       = (Resolve-Path .\tools\jdk17).Path
$env:Path            = "$env:JAVA_HOME\bin;$env:Path"
$env:ANDROID_HOME    = (Resolve-Path .\tools\android-sdk).Path
$env:GRADLE_USER_HOME = (Resolve-Path .\tools\gradle-cache).Path
.\gradlew.bat clean assembleRelease --no-daemon
```

Výstup:
```
app/build/outputs/apk/release/ZeddiHub-TV-<versionName>.apk
```

### Signing

Release builds vyžadují `keystore.properties` v rootu repu:

```properties
storeFile=keystore/zeddihub-tv-release.jks
storePassword=...
keyAlias=zeddihub-tv
keyPassword=...
```

Bez něj proběhne build s debug klíčem — užitečné pro lokální testování, nepoužitelné pro release distribuci.

---

## Tech stack

| Vrstva | Knihovna |
|---|---|
| UI | Jetpack Compose for TV (`tv-foundation`, `tv-material`) |
| Theme | Material 3 for TV |
| DI | Hilt 2.51 |
| Network | Retrofit 2.11 + OkHttp 4.12 + Moshi 1.15 |
| Storage | DataStore Preferences + EncryptedSharedPreferences |
| Background | WorkManager + Coroutines/Flow |
| Push | Firebase Cloud Messaging |
| Update | Custom release-gating client (sdílený s mobile) |
| Image | Coil 2.6 |
| Logging | Timber |

Min SDK **26** (Android 8.0), target SDK **34** (Android 14).

---

## Architecture

```
com.zeddihub.tv/
├── data/
│   ├── prefs/            ← AppPrefs (DataStore), TimerPrefs
│   ├── repository/       ← Auth, Telemetry, Update repos
│   ├── remote/           ← Retrofit API services
│   └── update/           ← UpdateChecker (release-gating client)
├── timer/                ← Sleep Timer feature
│   ├── SleepTimerService.kt    ← foreground service + countdown
│   ├── TimerOverlayManager.kt  ← SYSTEM_ALERT_WINDOW overlay
│   ├── TimerQuickActionsView   ← rychlá nabídka (pauza/stop/vypnout)
│   ├── TimerScreen.kt          ← in-app screen
│   └── TimerViewModel.kt
├── dashboard/            ← Dashboard screen + widgety
├── network/              ← Wake-on-LAN, ping, speed test, Wi-Fi info
├── media/                ← Plex/Kodi/SMB/Streaming launcher
├── servers/              ← Game server status
├── settings/             ← Settings screen + sub-screens
├── ui/
│   ├── theme/            ← Material 3 for TV theme
│   └── components/       ← TV cards, focus borders, common widgets
├── nav/                  ← Compose navigation graph
├── fcm/                  ← Firebase Messaging Service
├── MainActivity.kt
└── ZeddiHubTvApp.kt      ← Application + DI + notif channels
```

---

## ZeddiHub ecosystem

| Component | Repo | Verze |
|---|---|---|
| Web admin + landing | [zeddihub-tools-website](https://github.com/ZeddiS/zeddihub-tools-website) | 0.9.3 |
| Mobile (Android) | [zeddihub-tools-mobile](https://github.com/ZeddiS/zeddihub-tools-mobile) | 0.9.3 |
| Desktop (Windows) | [zeddihub-tools-desktop](https://github.com/ZeddiS/zeddihub-tools-desktop) | 1.7.6 |
| **TV (Android TV)** | **zeddihub-tools-tv** | **0.1.0** |

Společné API: `https://zeddihub.eu/api/`
Společný admin: `https://zeddihub.eu/tools/admin/`

---

## Roadmap

- [ ] **0.1.0** — Project scaffold, Sleep Timer s overlay, dashboard, settings
- [ ] **0.2.0** — Wake-on-LAN, ping, speed test
- [ ] **0.3.0** — Plex / Kodi remote
- [ ] **0.4.0** — Game server status + FCM push
- [ ] **0.5.0** — SMB galerie + streaming launcher
- [ ] **0.6.0** — Calendar widget + lokalizace UI strings
- [ ] **0.9.0** — Sjednocení verze line s ekosystémem
- [ ] **1.0.0** — Stable release

---

## Privacy

- Aplikace **neloguje IP adresy** v telemetrii (jen hash `sha256(salt|ip)`).
- Žádná third-party analytics (Google Analytics / Firebase Analytics jsou opt-in).
- Wi-Fi credentials, Plex tokeny atd. uložené v EncryptedSharedPreferences (Android Keystore).
- Žádná telemetrie pro neautentifikované uživatele.

---

## License

Proprietary — © 2026 Zeddis. Source code je publikován pro transparentnost a community contributions, ale binárka a brand patří do soukromého vlastnictví. Pull requesty vítány.

---

## Links

- 🌐 Web: [zeddihub.eu](https://zeddihub.eu)
- 💬 Discord: [dsc.gg/zeddihub](https://dsc.gg/zeddihub)
- 📱 Mobile: [zeddihub-tools-mobile](https://github.com/ZeddiS/zeddihub-tools-mobile)
- 💻 Desktop: [zeddihub-tools-desktop](https://github.com/ZeddiS/zeddihub-tools-desktop)
