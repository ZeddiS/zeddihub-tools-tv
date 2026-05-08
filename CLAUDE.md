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
| Aktuální verze | **0.2.0** (versionCode 2) |
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

### Milník 0.1.0 — Foundation (DOKONČENO 2026-05-08)
- [x] Profesionální README.md
- [x] CLAUDE.md (tento soubor)
- [x] Gradle setup (root + app + wrapper, sdílený toolchain s mobile)
- [x] AndroidManifest.xml (kompletní permissions)
- [x] App shell (Application + MainActivity + Theme + Nav + main scaffold)
- [x] **Sleep Timer feature — PLNÁ implementace** (foreground service, SYSTEM_ALERT_WINDOW overlay, konfigurovatelný roh, konfigurovatelný trigger key, rychlé možnosti pauza/stop/vypnout přes AccessibilityService, GLOBAL_ACTION_LOCK_SCREEN v 0:00, audio fade-out v posledních 10 s)
- [x] Dashboard (hodiny + datum + počasí Open-Meteo + system info + quick launch dlaždice)
- [x] Network tools (Wake-on-LAN, paralelní ping batch, Cloudflare speed test, Wi-Fi info)
- [x] Media remote (streaming app launcher pro 8 hlavních aplikací; Plex/Kodi credentials uložení v DataStore)
- [x] Game servers (live status karet ze sdíleného `/api/servers.php` backendu)
- [x] Settings (theme, timer trigger, timer pozice, fade audio toggle, permissions, manual update check)
- [x] UpdateChecker (release-gating + DownloadManager + FileProvider system installer)
- [x] Public landing page `zeddihub-tools-website/tools/tv/index.php` (modrý TV accent)
- [x] `tools/data/version_tv.json` (manifest s release notes CS/EN)
- [x] Link na TV landing v desktop landingu (`tools/index.php` — promo box "Také pro TV")
- [x] git init + first commit + GitHub repo: https://github.com/ZeddiS/zeddihub-tools-tv (public)

### Milník 0.2.0 — Sleep Timer power-up + Health monitor (TENTO SPRINT)
- [ ] **Schedule presets** — opětovný časovač podle dnů v týdnu (po-pá 22:30, so-ne 00:00). WorkManager + AlarmManager pipeline.
- [ ] **Bedtime routine** — řetěz akcí: ztlumit zvuk → spustit timer → custom hooks (Hue webhook, atd.). User-configurable order.
- [ ] **TV box health monitor** — teplota CPU/baterie, RAM, storage, network throughput. Real-time graf (30s/5min/1h okno). Alert při přehřátí > 70°C.
- [ ] **Cross-device file send (LocalSend)** — ZeddiHub-vlastní LAN protokol pro příjem souborů z mobile/desktop na TV. Open-with akce.

### Milník 0.3.0 — Smart home + Wake-up
- [ ] **Wake-up alarm** — ráno zapne TV v zadaný čas, voliteľne spustí app. AlarmManager exact alarm + WAKE_LOCK.
- [ ] **Hue / Tasmota / Tuya remote** — přímé HTTP API klientky pro nejběžnější DIY chytré domácnosti. "Filmový režim" jedno tlačítko.
- [ ] **Watch later inbox** — sdílená fronta odkazů ze všech ZeddiHub klientů (`api/watchlater.php`). Open-with deep link routing (YouTube ID → YT app).
- [ ] **Push notifikace overlay** — admin push se zobrazí jako overlay banner na TV (sdíleno s push.php composerem).

### Milník 0.4.0 — Hub integrace + alerty
- [ ] **Home Assistant integrace** — webhook caller + script trigger; konfigurovatelný HA endpoint v Settings.
- [ ] **HDMI-CEC controller přes Wi-Fi** — vlastní protokol mezi TV app a desktop appkou (volume sync, input switch); HA bridge volitelně.
- [ ] **Server down overlay alert** — když Rust/CS2 padne, full-screen banner na TV (z FCM topicu `server-status`).
- [ ] **Connection test (pre-flight)** — diagnostický pipeline před streamem (DNS check, latency, jitter, throughput) s fix sugescemi.

### Milník 0.5.0 — Parental + accessibility
- [ ] **Audio output switcher** — TV speakers / HDMI ARC / Bluetooth quick toggle z anywhere v aplikaci.
- [ ] **Screen time / parental limity** — PIN-locked apps, denní limity, bedtime hodiny per-app. UsageStatsManager + DeviceAdmin.
- [ ] **Smart auto-detect spánku** — AccessibilityService listener na input, AudioManager STREAM_MUSIC volume → návrh auto-shutdown po 15 min nečinnosti.
- [ ] **Universal CC toggle + dyslektický font** — accessibility service force CC napříč apps, font replacement (OpenDyslexic).

### Milník 0.9.0 — Sjednocení verze line
- [ ] Bump na 0.9.x ve sjednocenou release line s mobile/web/desktop

### Milník 1.0.0 — Stable release
- [ ] Beta testy 2 týdny
- [ ] Crash reporting (Firebase Crashlytics)
- [ ] Performance pass (cold start time, memory profiling)

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

### 2026-05-08 — Session #1: Foundation 0.1.0 (KOMPLETNÍ)
- ✅ Profesionální README.md (TV blue accent, mirror mobile/desktop pattern)
- ✅ CLAUDE.md projektový mozek
- ✅ Gradle setup (sdílený toolchain s mobile přes local.properties)
- ✅ AndroidManifest s SYSTEM_ALERT_WINDOW, FGS specialUse, AccessibilityService, FileProvider
- ✅ Resources: themes, colors, strings, banner, adaptive icon, file_provider_paths, accessibility_service_config
- ✅ App shell: ZeddiHubTvApp (notif channels), MainActivity (Hilt + update check on startup), AppScaffold (side rail nav), Compose for TV theme (dark + TV blue + orange brand)
- ✅ Sleep Timer: SleepTimerService (foreground specialUse, partial wake lock, 1s ticker, audio fade-out), TimerOverlayManager (chip + quick actions), TimerAccessibilityService (long-press trigger detection, GLOBAL_ACTION_LOCK_SCREEN dispatcher), TimerScreen + ViewModel + State + Actions
- ✅ Dashboard: clock ticker, Open-Meteo weather (Praha lat/lon), RAM/storage/Wi-Fi info, streaming launcher row
- ✅ Network: WakeOnLan magic packet, parallel TCP ping batch, Cloudflare speed test
- ✅ Media: 8-app launcher grid (Netflix/YT/Disney/HBO/Spotify/Plex/Kodi/Twitch)
- ✅ Servers: backend pull + status cards
- ✅ Settings: theme/trigger/corner/fade/perms/manual update
- ✅ UpdateChecker: poll /api/app-version.php?kind=tv, DownloadManager, FileProvider installer
- ✅ Web landing /tools/tv/index.php + version_tv.json
- ✅ "Také pro TV" promo box v desktop landing /tools/index.php (modrý gradient)
- ✅ GitHub repo: https://github.com/ZeddiS/zeddihub-tools-tv (public)
- 📝 Rozhodnutí: Compose for TV, com.zeddihub.tv, v0.1.0, SYSTEM_ALERT_WINDOW overlay, konfigurovatelný trigger button (default OK long-press 800ms), vypnutí TV+box v 0:00 přes AccessibilityService LOCK_SCREEN, telemetry + release-gating, sdílený toolchain s mobile

### 2026-05-08 — Session #2: Build + Release + Admin (KOMPLETNÍ)
- ✅ Opraveno `androidx.tv.material3.SurfaceDefaults.shape(...)` API mismatch (5 souborů, bulk regex fix)
- ✅ Vygenerován keystore `keystore/zeddihub-tv-release.jks` (4096-bit RSA, 10000 dní validity)
- ✅ `keystore.properties` v rootu (gitignored)
- ✅ **Build APK** úspěšný — `gradlew.bat clean assembleRelease` v 3m 29s, `ZeddiHub-TV-0.1.0.apk` (2.3 MB, podepsán)
- ✅ APK deploynut do `zeddihub-tools-website/downloads/ZeddiHub-TV-0.1.0.apk`
- ✅ Admin panel TV sekce:
  - `tv_releases.php` — kompaktní release manager (DB platform='tv', sync s version_tv.json)
  - `tv_features.php` — landing features editor (mirror mobile_features)
  - `tv_popular.php` — popular tiles editor (mirror mobile_popular)
  - `analytics_tv.php` — TV-only telemetrie (filter client_kind='tv')
- ✅ Sidebar: nová sekce **TV** (tv_releases, tv_features, tv_popular) + analytics_tv v Analytics sekci
- ✅ `_analytics_helpers.php` rozšířený o `tv` paletu (modrá #3b82f6, ikona 📺) + 4. tab odkaz
- ✅ `index.php` router: nové TV routes registrované
- ✅ FILE_FEATURES_TV / FILE_POPULAR_TV / FILE_VERSION_TV konstanty v `_lib.php`
- ✅ GitHub release v0.1.0 s přiloženým APK

### 2026-05-08 — Session #3: Audit + Fix + v0.2.0 (KOMPLETNÍ)
- ✅ Audit TV kódu — 4 reálné bugy:
  - `runBlocking` na main threadu v TimerOverlayManager.chipLayoutParams → cachuji corner reaktivně
  - DownloadManager nemůže psát do `cacheDir` → `getExternalFilesDir(null)`
  - Toast leak z Activity → `applicationContext`
  - SleepTimerService scope bez `SupervisorJob` → přidáno
- ✅ Audit admin PHP — fiktivní `zh_pdo()` / `zh_db_kind()` → opraveno na skutečné `zh_sql()` / `zh_sql_driver()`
- ✅ Re-build s opravami — BUILD SUCCESSFUL
- ✅ **Schedule presets** — Schedule data class + ScheduleStore (DataStore JSON) + ScheduleScheduler (AlarmManager exact alarms) + ScheduleAlarmReceiver + SchedulesScreen + SchedulesViewModel. TimerBootReceiver re-armuje po rebootu.
- ✅ **Bedtime routine** — Routine model (4 kroky: volume_fade, start_timer, delay, webhook) + RoutineRunner + RoutineScreen + RoutineViewModel. Webhook používá injected OkHttpClient.
- ✅ **TV health monitor** — HealthSampler (čte /proc/stat, /sys/class/thermal, /sys/class/power_supply) + HealthScreen s real-time grafem teploty (Canvas Path) + HealthViewModel (5s sampling, 60-sample ring buffer).
- ✅ Nav: 3 nové destinace (Schedule, Routine, Health) v side rail.
- ✅ Manifest: SCHEDULE_EXACT_ALARM, USE_EXACT_ALARM, BATTERY_STATS perms; ScheduleAlarmReceiver registered.
- ✅ AppPrefs: schedulesJson, bedtimeRoutineJson, healthTempThreshold keys.
- ✅ versionCode 2 / versionName 0.2.0
- ✅ RELEASE_NOTES_0.2.0.md
- ✅ version_tv.json updated to 0.2.0

### Otevřené úkoly do v0.3.0
- [ ] **LocalSend receiver** — HTTP server na portu 53317 + mDNS announcement (`_localsend._tcp.local.`). Použít NanoHTTPD nebo Ktor Server.
- [ ] **Wake-up alarm** — opětovaný budík; AlarmManager exact + WAKE_LOCK + ACTION_WAKE_UP intent na launch zvolené aplikace.
- [ ] **Hue / Tasmota / Tuya remote** — HTTP API klient (Hue: bridge IP + username, Tasmota: REST commands, Tuya: cloud API). Settings sekce.
- [ ] **Watch later inbox** — sdílená queue přes ZeddiHub backend (`api/watchlater.php`); UI pro browse + open-with deep links.
- [ ] **Push notifikace overlay** — FCM listener (vyžaduje `google-services.json`), nebo polling endpoint.
- [ ] Otestovat 0.2.0 na Xiaomi TV Box S 3rd Gen — ověřit alarm + overlay flow
- [ ] Plex / Kodi real remote (nejen launcher) — JSON-RPC client

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
