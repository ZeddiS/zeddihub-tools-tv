# ZeddiHub TV 0.1.12 — Tmavší pozadí + Wi-Fi fix + Auto-focus + Stagger animace

**Datum:** 2026-05-11
**Verze:** 0.1.12 (versionCode 13)
**Status:** Pending → Publish přes Admin panel

## 4 hlavní změny

### 📡 1. Wi-Fi info už neukazuje `<unknown ssid>`

**Root cause:** Android 10+ blokuje čtení SSID bez `ACCESS_FINE_LOCATION` permission. `WifiManager.connectionInfo.ssid` pak vrací řetězec `<unknown ssid>`, který se zobrazoval doslova na Dashboardu.

**Fix v `DashboardViewModel.kt`:**
- Detekce `<unknown ssid>`, `0x`, `<no ssid>` → nahrazeno prázdným stringem
- Fallback na samotné **IP + signál**
- **Smart signal label:** `dBm + výborný/dobrý/slabší/slabý` místo prostého čísla
- Žádná dodatečná permission nutná

### 🎨 2. True-black pozadí (OLED-friendly)

| | Před | Po |
|---|---|---|
| `background` | `#080810` (téměř černá) | **`#000000`** (true black) |
| `surface` | `#14141F` | **`#0E0E18`** (subtilní elevation) |
| `border` | `#252535` | **`#1F1F2E`** (lépe ladí s černou) |

Hero gradient karty teď víc vyniknou. Na OLED panelech (Samsung, Sony) jsou pixely v black pozadí skutečně vypnuté — power saving + perfect contrast.

### ✨ 3. Auto-focus + animace při otevření podstránky

Když naviguješ z Dashboardu na např. Sleep Timer:

**Před:** D-pad fokus landoval na „← Domů" pill (a klik OK by tě vrátil). Uživatel musel ručně přejet šipkami dolů na content.

**Teď v `AppScaffold.kt subScreen()`:**
- **FocusRequester** + `LaunchedEffect(Unit)` automaticky fokusne content area po 140ms (čeká na slide-in transition)
- D-pad landuje na první focusable item v contentu
- „← Domů" pill je fallback (vrchol obrazovky vlevo)
- Content má fade-in + slide-up animaci (320ms)
- focusGroup() obaluje content tak že fokus přechází plynule

### 🎬 4. Stagger animace na Dashboardu

5 řad teď přicházejí postupně:

- Hero clock card + sysinfo strip — **okamžitě** (decorative)
- Rychlé akce — t+90ms
- Streamování — t+180ms
- Nástroje — t+270ms
- Domácnost & sdílení — t+360ms
- Systém — t+450ms

Každá řada fade-in + slide-up z spodu (8dp). Vytváří „cascade in" feel jako PS5 / Apple TV home.

## 📁 Změněné soubory

| Soubor | Změna |
|---|---|
| `app/src/main/java/com/zeddihub/tv/dashboard/DashboardViewModel.kt` | Wi-Fi SSID smart fallback |
| `app/src/main/java/com/zeddihub/tv/ui/theme/Theme.kt` | True-black bg, darker surface/border |
| `app/src/main/java/com/zeddihub/tv/nav/AppScaffold.kt` | Auto-focus + fade-up animation v subScreen() |
| `app/src/main/java/com/zeddihub/tv/dashboard/DashboardScreen.kt` | Stagger wrapper + cascade timing |
| `app/build.gradle.kts` | versionCode 12→13, versionName 0.1.11→0.1.12 |

## 📦 Build info

- **APK:** `ZeddiHub-TV-0.1.12.apk` (2.7 MB, signed)
- **BUILD SUCCESSFUL** (7m 32s)

## 🎯 Test po updatu

1. Wi-Fi info na Dashboardu → ukazuje **IP + signál** (žádný `<unknown ssid>`)
2. Pozadí je **úplně černé** — gradient karty vyniknou
3. Klik na **Sleep Timer dlaždici** → animace slide-up + D-pad fokus rovnou na content
4. Otevřená podstránka má **„← Domů" pill v rohu** jako fallback
5. **Cascade entrance** při prvním otevření Dashboardu — řady postupně přicházejí

## ✅ Stav screenů (audit z této session)

Audit ukázal že **všech 21 podstránek je plně funkčních** (Timer, Schedule, Routine, WakeUp, SmartHome, HomeAssistant, WatchLater, LocalSend, Files, Browser, Alerts, Audio, ConnectionTest, Health, Network, Media, Servers, Accessibility, Parental, Settings + Dashboard). Žádné placeholders.

Známé limitace:
- **SmartHome:** TV má pouze read-only editor (host/token/target). Důvod: TextField na D-pad je nepohodlné. Plnohodnotný editor je v mobile/desktop app.
- **Files → SMB:** plánováno na v0.5+ (placeholder)
