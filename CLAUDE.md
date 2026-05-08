# ZeddiHub Tools TV — projektový mozek

> Tento soubor je **trvalá paměť projektu** napříč konverzacemi.
> Vše, co se v projektu rozhodlo, naplánovalo nebo dokončilo, žije zde.
> V nové session si Claude přečte tento soubor jako první, aby věděl,
> kde jsme skončili.
>
> **JAZYK ODPOVĚDÍ:** Komunikace s uživatelem výhradně **česky**.

---

## 0. Identita projektu

| Pole | Hodnota |
|---|---|
| Název | **ZeddiHub Tools TV** |
| Repo (lokálně) | `C:\Users\12voj\Documents\zeddihub-tools-tv\` |
| Repo (GitHub) | `https://github.com/ZeddiS/zeddihub-tools-tv` |
| Package | `com.zeddihub.tv` |
| Aktuální verze | **0.1.0** (versionCode 1) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |
| Cílová zařízení | Android TV — primárně **Xiaomi TV Box S 3rd Gen**, sekundárně Google TV / Nvidia Shield |
| Tech stack | Kotlin 1.9.23 + Compose for TV + Material 3 + Hilt + Retrofit + DataStore |
| Toolchain | Bundled (sdílený s `zeddihub_tools_mobile/tools/`) |

---

## 1. Globální kontext (rodinný strom)

ZeddiHub má 4 platformy:

| Platforma | Repo / složka | Verze | Stav |
|---|---|---|---|
| Web admin + landing | `zeddihub-tools-website` | 0.9.3 | Production |
| Mobile (Android) | `zeddihub_tools_mobile` | 0.9.3 | Production |
| Desktop (Windows) | `zeddihub_tools_desktop` | 1.7.6 | Production |
| **TV (Android TV)** | **`zeddihub-tools-tv`** | **0.1.0** | **In development (this repo)** |

Společné API: `https://zeddihub.eu/api/`
Společný admin: `https://zeddihub.eu/tools/admin/`

Master CLAUDE.md (rodinný kontext, mapa repozitářů, build pravidla, deploy):
`C:\Users\12voj\Documents\CLAUDE.md` — vždy číst první.

---

## 2. Rozhodnutí (immutable)

Toto jsou rozhodnutí, ke kterým jsme došli s uživatelem v setup chatu (2026-05-08).
Nepřehodnocovat bez výslovného pokynu uživatele.

| Téma | Rozhodnutí |
|---|---|
| Tech stack | **Kotlin + Compose for TV** (`androidx.tv:tv-foundation`, `androidx.tv:tv-material`) |
| App ID | `com.zeddihub.tv` |
| Verze line | Vlastní `0.x.x` od 0.1.0, sjednotí se s ekosystémem v 0.9.0 / 1.0.0 |
| Sleep Timer overlay | **SYSTEM_ALERT_WINDOW** — plovoucí odpočet přes všechny aplikace |
| Trigger button (rychlé možnosti) | **Konfigurovatelné v Nastavení** (default: long-press D-pad OK) |
| Vypnout v rychlých možnostech | **Vypne TV i box** (standby) — Device Admin API + power intent fallback |
| Pozice odpočtu | **Pravý horní roh** default; 4 rohy konfigurovatelné |
| Auto-update | Ano — release-gating přes `https://zeddihub.eu/api/app-version.php` (kind=tv) |
| ZeddiHub integrace | **Telemetry + release-gating** (jako mobile). Login/FCM zatím odložené. |
| Zahrnuté funkce v0.1.0 | Sleep Timer (full), Dashboard, Network tools, Media remote, Game servers + FCM (placeholdery zatím) |

---

## 3. Roadmap

### Milník 0.1.0 — KOMPLETNÍ první release (per uživatel: "všechny požadavky")
- [x] Profesionální README.md
- [x] CLAUDE.md (tento soubor)
- [ ] Gradle setup (root + app + wrapper, sdílený toolchain s mobile)
- [ ] AndroidManifest.xml (kompletní permissions)
- [ ] App shell (Application + MainActivity + Theme + Nav + main scaffold)
- [ ] **Sleep Timer feature — PLNÁ implementace** (foreground service, SYSTEM_ALERT_WINDOW overlay, drag-and-drop pozice, konfigurovatelný trigger, rychlé možnosti pauza/stop/vypnout, vypnutí TV+box v 0:00)
- [ ] Dashboard (hodiny + datum + počasí Open-Meteo + system info + quick launch dlaždice)
- [ ] Network tools (Wake-on-LAN, paralelní ping batch, speed test, Wi-Fi RSSI/kanál, ARP scan)
- [ ] Media remote (Plex JSON-RPC, Kodi, streaming app launcher s deep links)
- [ ] Game servers (live status karet, FCM stub) — sdílený endpoint s mobile
- [ ] Settings (theme, language, timer trigger, timer pozice, WoL device store, Plex/Kodi creds)
- [ ] UpdateChecker (release-gating + APK download + system installer)
- [ ] Public landing page `zeddihub-tools-website/tools/tv/index.php` (modrý TV accent)
- [ ] `tools/data/version_tv.json` (auto-spravováno admin panelem v budoucnu)
- [ ] Link na TV landing v desktop landingu (`tools/index.php` — promo box "Také pro TV")
- [ ] git init + first commit + GitHub repo `ZeddiS/zeddihub-tools-tv`

### Milník 0.2.0 — Network plný
- [ ] Wake-on-LAN — multi-device profil store
- [ ] Ping batch (paralelní TCP)
- [ ] Speed test
- [ ] Wi-Fi diagnostika (RSSI, kanál, frekvence)
- [ ] Local network scan (ARP + mDNS)

### Milník 0.3.0 — Media remote
- [ ] Plex JSON-RPC client + screen
- [ ] Kodi remote
- [ ] Streaming app launcher (deep links na Netflix / YouTube / atd.)

### Milník 0.4.0 — Game servers + FCM
- [ ] Game server status karty (Rust PVE, CS2, CS:GO, …) — stejný backend jako mobile
- [ ] FCM integrace (`google-services.json`, ZeddiFirebaseMessagingService)
- [ ] Notif kategorie: server-down, eventy, oznámení

### Milník 0.5.0 — SMB galerie
- [ ] SMB klient (jcifs-ng)
- [ ] Foto / video grid
- [ ] Video přehrávač (ExoPlayer)

### Milník 0.6.0 — Calendar widget + i18n
- [ ] Google Calendar OAuth (volitelně)
- [ ] Lokalizace UI strings (cs / en)

### Milník 0.9.0 — Sjednocení verze line
- [ ] Bump na 0.9.x ve sjednocenou release line

### Milník 1.0.0 — Stable release
- [ ] Beta testy 2 týdny
- [ ] Crash reporting
- [ ] Performance pass

---

## 4. Aktivní úkoly (current sprint)

> Vždy aktualizovat při dokončení nebo přesunu úkolu.

### In progress
- _žádný_

### Next up (priorita)
1. Gradle setup (root + app + wrapper)
2. AndroidManifest.xml + resources
3. App shell (Application + MainActivity + Theme + Nav)
4. Sleep Timer implementace
5. Auto-updater
6. Landing page + odkaz v desktop landingu
7. git init + GitHub repo

### Blocked
- _žádné_

---

## 5. Architektura

```
com.zeddihub.tv/
├── data/
│   ├── prefs/              ← AppPrefs (DataStore), TimerPrefs
│   ├── repository/         ← Auth (later), Telemetry, Update
│   ├── remote/             ← Retrofit API services
│   └── update/             ← UpdateChecker (release-gating)
├── timer/                  ← Sleep Timer feature
│   ├── SleepTimerService.kt    ← foreground service + countdown
│   ├── TimerOverlayManager.kt  ← SYSTEM_ALERT_WINDOW overlay
│   ├── TimerQuickActionsView   ← rychlá nabídka (pauza/stop/vypnout)
│   ├── TimerScreen.kt          ← in-app screen
│   └── TimerViewModel.kt
├── dashboard/              ← Dashboard screen + widgety
├── network/                ← WoL, ping, speed test, Wi-Fi info
├── media/                  ← Plex/Kodi/SMB/Streaming launcher
├── servers/                ← Game server status
├── settings/               ← Settings screen + sub-screens
├── ui/
│   ├── theme/              ← Material 3 for TV theme
│   └── components/         ← TV cards, focus borders, common widgets
├── nav/                    ← Compose navigation graph
├── fcm/                    ← Firebase Messaging Service (later)
├── MainActivity.kt
└── ZeddiHubTvApp.kt        ← Application + DI + notif channels
```

---

## 6. Sleep Timer — design spec

### Komponenty
- **`SleepTimerService`** (foreground) — drží countdown state, broadcastuje tick každou vteřinu, persistent notif, při 0 → standby intent.
- **`TimerOverlayManager`** — `WindowManager.LayoutParams(TYPE_APPLICATION_OVERLAY)` overlay.
  - `FloatingCountdownView` — malý chip s časem (cca 64×40 dp).
  - `QuickActionsView` — expandovaná nabídka při dlouhém stisku trigger tlačítka.
- **`AccessibilityKeyTrap`** — `AccessibilityService` zachytává globální key events (long-press OK/Back/Home/Menu) i mimo aplikaci.
- **`TimerScreen`** (Compose for TV) — in-app UI: presets (15/30/45/60/90/120 min) + custom + start.
- **`TimerSettings`** — pozice (4 rohy / drag), trigger button volba, ztlumení 10 s před koncem (volitelné).

### Stavy
```
IDLE → ARMED (countdown běží) → PAUSED → ARMED → EXPIRED (provede shutdown action) → IDLE
                              ↘ STOPPED → IDLE
```

### Permissions
- `SYSTEM_ALERT_WINDOW` — overlay (uživatel udělí přes Settings)
- `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` (Android 14)
- `WAKE_LOCK` — udržet CPU během countdownu
- `BIND_ACCESSIBILITY_SERVICE` — pro globální key trap (uživatel udělí přes Settings)
- `BIND_DEVICE_ADMIN` (volitelně, pro standby) nebo fallback na `Intent.ACTION_REQUEST_SHUTDOWN` / `lockNow()`.

### Shutdown akce v 0:00
1. Pokud Device Admin **udělen** → `DevicePolicyManager.lockNow()` + IR/HDMI-CEC standby.
2. Jinak fallback: launcher home + screen off + system standby intent (na Xiaomi TV Box S funguje `android.intent.action.ACTION_REQUEST_SHUTDOWN` u některých firmware).
3. Vždy: wake lock release, foreground service stop, telemetry event `timer.expired`.

---

## 7. Auto-update flow

1. Při startu (Application.onCreate → po 3 s delay) volá `UpdateChecker.check()`.
2. GET `https://zeddihub.eu/api/app-version.php?kind=tv&version_code=N` →
   ```json
   {
     "live": { "version_code": 2, "version_name": "0.2.0", "apk_url": "...", "min_sdk": 26,
               "release_notes_cs": "...", "release_notes_en": "...", "force": false }
   }
   ```
3. Pokud `live.version_code > current` → dialog `StartupUpdateDialog`:
   - Tlačítka: **Aktualizovat** / **Později** (pokud `force=false`).
   - Pokud `force=true` → jen **Aktualizovat** (nelze odložit).
4. Download APK přes DownloadManager s progresem.
5. `Intent.ACTION_VIEW` + `application/vnd.android.package-archive` + `FileProvider` URI → systémový installer.
6. V Settings → tlačítko **Zkontrolovat aktualizace** (manuální force-check).

---

## 8. Build & deploy

### Build commands
```powershell
# PowerShell, z rootu
$env:JAVA_HOME       = "C:\Users\12voj\Documents\zeddihub_tools_mobile\tools\jdk17"
$env:Path            = "$env:JAVA_HOME\bin;$env:Path"
$env:ANDROID_HOME    = "C:\Users\12voj\Documents\zeddihub_tools_mobile\tools\android-sdk"
$env:GRADLE_USER_HOME = "C:\Users\12voj\Documents\zeddihub_tools_mobile\tools\gradle-cache"
.\gradlew.bat clean assembleRelease --no-daemon
```

> Toolchain je sdílen s mobile repem, šetří 2+ GB diskového prostoru. Pokud
> chceme mít TV repo úplně samostatný, můžeme zkopírovat `tools/` adresář
> sem (cca 2.5 GB) — viz §9 deploy plán.

### Output
`app/build/outputs/apk/release/ZeddiHub-TV-{versionName}.apk`

### Deploy APK na web
Stejně jako mobile — kopírovat do `zeddihub-tools-website/downloads/` a smazat starou:
```powershell
$ver = "0.1.0"
Copy-Item "zeddihub-tools-tv\app\build\outputs\apk\release\ZeddiHub-TV-$ver.apk" `
          "zeddihub-tools-website\downloads\ZeddiHub-TV-$ver.apk"
Get-ChildItem "zeddihub-tools-website\downloads\ZeddiHub-TV-*.apk" |
  Where-Object { $_.Name -ne "ZeddiHub-TV-$ver.apk" } | Remove-Item
```

### Versioning rules
| Soubor | Co změnit |
|---|---|
| `app/build.gradle.kts` | `versionCode` (++) + `versionName` |
| `zeddihub-tools-website/tools/data/staged_releases.json` | nový záznam s `kind=tv` |
| `CLAUDE.md` (tento soubor §0 + §3) | aktuální verze |
| `RELEASE_NOTES_{ver}.md` | changelog |

---

## 9. Web landing — zeddihub-tools-website/tools/tv/

### Struktura
- Nový adresář `tools/tv/index.php` — landing page mirroruje `tools/mobile/index.php` strukturou, ale s **modrým TV accent** (#3b82f6 nebo Material You blue).
- Verze se čte z `tools/data/version_tv.json` (analog `version_android.json`).
- Auto-spravováno přes admin panel (až přidáme `kind=tv` do `app_releases.php`).
- Ikony / banner v `tools/assets/tv/`.

### Link v desktop landingu
- V `tools/index.php` přidat sekci **"Také pro TV"** vedle existující **"Také pro Android"** promo box.

### Admin panel rozšíření (later)
- `app_releases.php` musí podporovat `kind=tv` (vedle desktop/mobile).
- `staged_releases.json` přijímá `kind: 'tv' | 'mobile' | 'desktop'`.
- Nová `analytics_tv.php` (zatím není priorita, šablona připravena).

---

## 10. Pitfalls / gotchas (TV-specific)

### Compose for TV
- **`tv-material`** ≠ `material3` — pro TV použít `androidx.tv.material3.*` (ne klasické `androidx.compose.material3.*`).
- `Surface` na TV má jiné focus chování — důležité explicitně nastavit `border` / `glow` přes `ClickableSurfaceDefaults`.
- D-pad navigace fungujou out-of-box, ale pozor na nested `LazyRow`/`LazyColumn` — `Modifier.focusRestorer()` na top-level řeší ztrátu focusu při návratu.

### Overlay
- Android 12+ vyžaduje **`SYSTEM_ALERT_WINDOW`** přes Settings, ne jen runtime request. Nutno otevřít `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` intent.
- Na Xiaomi může overlay collidovat s vlastní MIUI for TV "PiP" → testovat reálně.
- Overlay se musí ukrýt při pop-up keyboardu (jinak blokuje input).

### Foreground service na Android 14
- `FOREGROUND_SERVICE_SPECIAL_USE` + dokumentace v `serviceInfo.specialUse` v manifestu.
- TV často běží na starším AOSP — ale Xiaomi TV Box S 3rd Gen má Android 14 (Google TV) → musí to splňovat.

### Standby na Android TV
- `Intent.ACTION_REQUEST_SHUTDOWN` funguje **jen jako system app** nebo s root.
- `DevicePolicyManager.lockNow()` po Device Admin enrollment funguje, ale jen lockne obrazovku — TV se nevypne.
- Plnohodnotný "vypnout box i TV": potřeba **HDMI-CEC** signál (Android nemá veřejné API, jen přes vendor SDK).
- **Realistický fallback** v0.1.0: `lockNow()` + spustit launcher home + screen off (přes `PowerManager.goToSleep()` reflection — funguje na rooted, jinak ne).
- **Long-term řešení**: zjistit HDMI-CEC bridge přes adb shell `input keyevent KEYCODE_SLEEP` (lze trigger přes `Runtime.exec` jen s root).

### Bundled toolchain sdílený s mobile
- `local.properties` ukazuje na `..\zeddihub_tools_mobile\tools\android-sdk` — relativní cesta funguje, ale nelze přesunout repo nezávisle.
- Pro CI / čistý setup → kopírovat `tools/` adresář sem (~2.5 GB).

---

## 11. Historie změn (changelog projektu)

> Každá session zapisuje co se udělalo, datum, klíčové soubory.

### 2026-05-08 — Session #1: Initial scaffold
- ✅ Vytvořen README.md (professional, mirroring mobile/desktop pattern, modrý TV accent)
- ✅ Vytvořen CLAUDE.md (tento soubor) — projektový mozek
- 🔄 V práci: gradle setup, manifest, app shell, sleep timer, auto-updater, landing
- 📝 Rozhodnutí: Compose for TV, com.zeddihub.tv, 0.1.0, SYSTEM_ALERT_WINDOW overlay, konfigurovatelný trigger button, vypnutí TV+box v 0:00, telemetry + release-gating

---

## 12. User preferences (specifické pro tento projekt)

| Preference | Hodnota |
|---|---|
| Jazyk | Čeština |
| Klikatelné otázky | Pro nejisté scénáře — ano, dokud 100% jistý |
| Cílové zařízení primárně | Xiaomi TV Box S 3rd Gen |
| UI feel | Moderní, jednoduché, ale s rozšířenými funkcemi |
| Verze line | Vlastní 0.x.x do 0.9.0 |

---

## 13. Reference

- Master rodinný CLAUDE.md: `C:\Users\12voj\Documents\CLAUDE.md`
- Mobile CLAUDE.md: `C:\Users\12voj\Documents\zeddihub_tools_mobile\CLAUDE.md`
- Web admin: `https://zeddihub.eu/tools/admin/`
- API base: `https://zeddihub.eu/api/`

---

**Poslední update:** 2026-05-08 (session #1, foundation sprint)
**Důvod existence:** Kontextová paměť projektu napříč chaty. V nové session: přečíst první.
