# ZeddiHub TV 0.1.11 — Side rail PRYČ + Wizard persistence + Back nav

**Datum:** 2026-05-10
**Verze:** 0.1.11 (versionCode 12)
**Status:** Pending → Publish přes Admin panel

## 🎮 Side rail úplně odstraněn — Google TV / PS5 native

Uživatel řekl: „nechci aplikaci jako seznam, chci PlayStation nebo Google TV". 0.1.10 přidal horizontální řady na Dashboard, ale **sidebar zůstal vedle nich** — výsledek vypadal pořád jako settings menu.

**Teď v 0.1.11:**
- **Side rail úplně odstraněn** z `AppScaffold`. Žádný menu list.
- **Dashboard = home screen** — přes celou obrazovku, 5 horizontálních řad
- Všech **22 destinací** pokryto dlaždicemi v řadách:
  - ⚡ Rychlé akce (Sleep Timer / Plán / Bedtime / Budík / Stav TV)
  - 🎬 Streamování (8 apps z `/api/tv-config.php`)
  - 🛠 Nástroje (Síť / Diagnostika / Média / Prohlížeč / Soubory / Audio)
  - 🏠 Domácnost & sdílení (SmartHome / HA / Watch later / LocalSend / Servery)
  - ⚙️ Systém (Upozornění / Přístupnost / Rodičovská / Nastavení)

**Navigace:**
- **Klik na dlaždici** → `NavController.navigate()` → podstránka
- **Hardware Back** na TV ovladači → zpět na Dashboard
- **Floating „← Domů" pill** v levém horním rohu každé podstránky — fallback když nemůžeš najít Back na ovladači. Primary fill on focus, OK = pop back stack
- **Page transitions:** 220ms fade + slide z pravé strany

## ✨ Wizard persistence opravena

Uživatel řekl: „když jsem dal přeskočit, chci aby se zobrazoval znovu při dalším spuštění dokud to nevyplním".

**Bug:** `SetupWizardViewModel.persist()` ukládalo prefs **A ZÁROVEŇ** flipla `wizardCompleted = true`. Skip volal `persist()` → wizard se navždy schoval.

**Fix:**
- `persist()` rozdělen na 2 metody:
  - **`saveValues()`** — uloží jen filled-in hodnoty (jazyk, téma, počasí…). Žádný completion flag.
  - **`complete()`** — uloží hodnoty + flag `wizardCompleted = true`
- **„✕ Přeskočit (teď)"** volá pouze `saveValues()` → wizard se schová pro **TENTO** session, ale příští cold start ho **zobrazí znovu**
- **„Dokončit ✓"** (poslední krok) volá `complete()` → wizard je hotový natrvalo
- **Bonus:** `saveValues()` uchovává cokoli uživatel vyplnil — žádný data loss při Skip

Plus **MainActivity:**
- `collectAsState(initial = true)` → **`initial = false`** — předtím se na first-run cold start na zlomek vteřiny ukázal AppScaffold než DataStore odpověděl. Teď wizard zobrazí okamžitě.

## 📁 Změněné soubory

| Soubor | Změna |
|---|---|
| `app/src/main/java/com/zeddihub/tv/nav/AppScaffold.kt` | Úplný přepis — side rail PRYČ, NavHost full width s overscan padding, `subScreen()` wrapper s HomePill |
| `app/src/main/java/com/zeddihub/tv/setup/SetupWizardViewModel.kt` | `persist()` rozdělen na `saveValues()` + `complete()` |
| `app/src/main/java/com/zeddihub/tv/setup/SetupWizard.kt` | Skip button volá `saveValues()` místo `persist()` |
| `app/src/main/java/com/zeddihub/tv/MainActivity.kt` | `initial = false` v collectAsState |
| `app/build.gradle.kts` | versionCode 11→12, versionName 0.1.10→0.1.11 |

## 📦 Build info

- **APK:** `ZeddiHub-TV-0.1.11.apk` (2.7 MB, signed)
- **BUILD SUCCESSFUL** (~3 min)

## 🎯 Co teď udělat — DŮLEŽITÉ

**Můj fix funguje JEN po deploye na PROD.** Pokud máš starší APK nainstalovanou v TV:

1. **Nahraj `ZeddiHub-TV-0.1.11.apk`** do `/downloads/` na zeddihub.eu (přes FTP/SFTP/cPanel)
2. **Nahraj nový `app_releases.php`** + `staged_releases.json` (nebo deploy celý repo)
3. **V admin panelu:** TV releases → 0.1.11 → **Test APK** → **Publikovat**
4. **Na TV:** otevři aplikaci. Update dialog by se měl objevit. Klik → Stáhnout a nainstalovat → app se restartuje s **novým GTV-style layoutem**
5. **Test:** zkus stisknout dlaždici — měla by tě navést na sub-screen. Stiskni Back na ovladači — vrátí tě na Dashboard

Pokud po deploye stále vidíš side rail / starý layout — pravděpodobně TV cachuje starou APK. Ručně odinstaluj a nainstaluj 0.1.11 přímo (přes ADB nebo Downloader app).
