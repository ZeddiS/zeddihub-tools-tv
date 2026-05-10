# ZeddiHub TV 0.1.9 — Auto-update + Analytics + Admin editor + Visual polish

**Datum:** 2026-05-10
**Verze:** 0.1.9 (versionCode 10)
**Status:** Pending → Publish přes Admin panel

## 4 hlavní opravy / vylepšení

### 1. ✅ Auto-update prompt **při každém zapnutí aplikace**

Update se teď spouští v `onCreate` s `savedInstanceState == null` guardem (pouze cold start, ne rotace).
Místo Toastu se zobrazí plnohodnotný **PlayStation-style dialog**:

- Velký gradient ikon-badge (NewReleases) v primary→tertiary gradientu
- 36 sp Bold version banner + StatusPill `code N`
- Scrollovatelná sekce „Co je nového" s release notes
- 2 tlačítka: **⬇ Stáhnout a nainstalovat** (PsPrimary) + **Později** (PsSecondary)
- Pro `force = true` updaty zmizí tlačítko Později → uživatel nemůže přeskočit
- Dialog se zobrazí pokaždé při startu, dokud nebude aktualizováno

### 2. ✅ Analytics — TV události se konečně zobrazují v Admin panelu

**Bylo:** TV app nikdy nic neposlala na backend → `analytics_tv.php` zobrazoval prázdno.

**Teď:**

- **TV app:** nový `TelemetryRecorder` posílá POST na `/tools/telemetry.php` při každém cold startu
- **Backend:** `telemetry.php` čte `X-Client-Kind: tv` header a ukládá ho do `telemetry_log.json`
- **Admin:** `analytics_tv.php` filtr `client_kind == 'tv'` teď konečně dostává data
- Žádné PII — anon=true, žádná IP / device id, jen verze + Android API + event název

### 3. ✅ Admin panel — nastavovat pořadí a upravovat aplikaci

Nová admin stránka **TV → TV app config** (`/tools/admin/?page=tv_app_config`) edituje 3 sekce:

#### 🎬 Streamovací aplikace (8 výchozích)
- Reorder ▲▼ buttony
- Inline editor: název / package / brand color (color picker) / viditelnost
- Add nové appky (Crunchyroll, Apple TV, Skylink, …)

#### 🏠 Dashboard rychlé akce (4 výchozí)
- Reorder + edit popisku, route, ikony, barvy
- Route musí odpovídat existujícímu screenu (timer / schedule / wakeup / health / smarthome / atd.)

#### 🌐 Záložky prohlížeče (5 výchozích)
- Reorder + edit título a URL
- URL validace (musí začínat http:// nebo https://)

**Backend:** nový endpoint `/api/tv-config.php` (Cache-Control 60s) servíruje JSON s 3 listy.

**TV Kotlin:** `TvConfigRepository` při startu načte cached JSON z DataStore (instant), pak refreshne ze sítě. `MediaScreen`, `BrowserViewModel` a `DashboardViewModel` konzumují live config přes StateFlow.

**Offline fallback:** pokud `/api/tv-config.php` nefunguje, app použije in-app hardcoded `StreamingApps.all` + `DefaultBookmarks.all`.

### 4. ✅ Vizuální polish — aplikace vypadá výrazně lépe

#### Animované přechody mezi screeny
- 250 ms fade + jemný horizontal slide (12dp z pravé strany)
- 220 ms fade-out + slight pull (-18dp doleva) — feel of momentum, not a hard cut
- Nastaveno na **NavHost** přes `enterTransition` / `exitTransition` / `popEnter` / `popExit`

#### Dashboard hero — animovaná breathing gradient
- 7s infinite-repeat (LinearEasing, Reverse) — gradient pulzuje mezi surface → primary → tertiary
- Tří-barevný color stop místo prostého 2-stop linear ramp
- Weather chip zabalený do frosted card (surface alpha 0.6) — čitelné bez ohledu na pozici světla
- Time fontSize 92 → **96 sp** + letter-spacing -2 (tighter)
- Card height 180 → **200 dp**
- Border radius 20 → **24 dp**

#### Side rail — active item indikátor
- 3 dp orange left-edge bar u aktivní položky
- Viditelný i když je rail collapsed (jen ikony) — uživatel okamžitě ví kde je
- Background alpha selected 0.18, focused 0.30 (víc kontrastní)
- Inactive font weight Normal → **Medium**

## Technické detaily

| Komponenta | Změna |
|---|---|
| `MainActivity.kt` | onCreate guard + StartupUpdateDialog + TvConfig.start() + Telemetry.recordLaunch() |
| `data/update/StartupUpdateDialog.kt` | NEW — full PS-style update dialog |
| `data/update/UpdateChecker.kt` | (zachováno) checkNow + startInstall pro dialog |
| `data/telemetry/TelemetryRecorder.kt` | NEW — POST events to /tools/telemetry.php |
| `data/config/TvRemoteConfig.kt` | NEW — Moshi data classes |
| `data/config/TvConfigRepository.kt` | NEW — fetch + cache + StateFlow exposure |
| `data/prefs/AppPrefs.kt` | + `tvRemoteConfigJson` key |
| `media/MediaScreen.kt` + `MediaViewModel.kt` | reads from TvConfigRepository.streamingApps |
| `browser/BrowserViewModel.kt` | combines local + remote bookmarks |
| `dashboard/DashboardViewModel.kt` | exposes `streamingApps` from TvConfigRepository |
| `dashboard/DashboardScreen.kt` | HeroClockCard breathing gradient + frosted weather chip |
| `nav/AppScaffold.kt` | NavHost transitions + active rail bar |

| Backend | Změna |
|---|---|
| `tools/telemetry.php` | extracts `X-Client-Kind` header, writes `client_kind` field |
| `api/tv-config.php` | NEW — serves streaming_apps + dashboard_tiles + bookmarks |
| `tools/admin/tv_app_config.php` | NEW — 3-section editor with reorder/edit/add/delete/reset |
| `tools/admin/_lib.php` | + `FILE_TV_APP_CONFIG` constant + sidebar entry |
| `tools/admin/index.php` | + `tv_app_config` page route |

## Workflow pro release

1. Build APK ✅
2. Deploy do `zeddihub-tools-website/downloads/ZeddiHub-TV-0.1.9.apk` ✅
3. Update `staged_releases.json` (TV 0.1.9 = pending) ✅
4. Git commit + tag v0.1.9 + push ✅
5. GitHub release ✅
6. Admin panel: TV releases → 0.1.9 → Test APK → Publikovat
7. TV klienti na 0.1.7/0.1.8 dostanou prompt při dalším cold startu

## První spuštění po updatu

- Při cold startu se zobrazí update dialog s release notes (pokud je novější verze)
- Telemetry event `launch` se odešle s `client_kind=tv`, `os=android_tv_<API>`, `version=0.1.9`
- Admin → Analytics → 📺 TV začne ukazovat data
- Streaming app launcher na Dashboardu + sekce Média konzumují `/api/tv-config.php`
- Browser bookmarks merge admin defaults + lokální editace uživatele
- Dashboard hero breathing gradient + animované přechody mezi screeny
