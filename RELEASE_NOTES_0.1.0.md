# ZeddiHub TV 0.1.0 — První oficiální release

První publikovaný release Android TV companion aplikace. **Tohle je jediná verze, kterou musíš ručně nainstalovat** — od této chvíle veškeré další aktualizace zařídí aplikace sama (admin panel → publish → klient se zeptá).

---

## 📥 Instalace (jednorázově)

### Před instalací
- TV box: **Xiaomi TV Box S 3rd Gen**, **Google TV**, **Nvidia Shield** nebo jakýkoli Android TV s API 26+ (Android 8.0+)
- Stejná Wi-Fi síť jako PC nebo telefon (pro sideload)

### Krok 1 — Povolit instalaci z neznámých zdrojů

Na TV: **Nastavení → Zařízení a předvolby → Bezpečnost a omezení → Neznámé zdroje** → povolit pro app, ze které budeš instalovat (typicky **Downloader**, **Files**, nebo **Send Files to TV**).

### Krok 2 — Stáhnout APK

Stáhni `ZeddiHub-TV-0.1.0.apk` z přiložených souborů níže (nebo z [zeddihub.eu/tools/tv/](https://zeddihub.eu/tools/tv/)).

### Krok 3 — Sideload na TV

Vyber jednu z metod:

#### Metoda A — ADB (doporučeno, nejrychlejší)
Na TV zapni: **Nastavení → O zařízení → 7× klikni na Build number → Developer options → Network debugging (ADB) ON**.

Z PC ve stejné Wi-Fi:
```bash
adb connect <TV_IP>:5555
adb install ZeddiHub-TV-0.1.0.apk
```

#### Metoda B — Send Files to TV / LocalSend (z mobilu)
1. Stáhni APK do mobilu (z odkazu výše)
2. Pošli ho na TV přes **Send Files to TV** nebo **LocalSend**
3. Na TV otevři přijatý soubor → Nainstalovat

#### Metoda C — Downloader app (přímo z TV)
1. Nainstaluj **Downloader** z Google Play
2. URL pole: `zeddihub.eu/downloads/ZeddiHub-TV-0.1.0.apk`
3. Go → Install

---

## ⚙️ Nastavení po prvním spuštění

### 1) Permission „Zobrazení přes ostatní aplikace"
Aplikace tě hned vyzve. Bez tohoto:
- ❌ plovoucí odpočet Sleep Timeru se nezobrazí
- ❌ banner upozornění o serverech se nezobrazí

**Cesta:** `Časovač → Udělit povolení` (nebo `Nastavení → Zobrazení přes ostatní aplikace`)

### 2) Služba přístupnosti
Pro reakci na long-press tlačítka (rychlé možnosti časovače) a smart-detect spánku.

**Cesta:** `Nastavení → Přístupnost → ZeddiHub TV → Zapnout`

### 3) Volby v `Nastavení`
- **Spouštěč rychlých možností** — OK / Back / Home / Menu (default OK long-press 800 ms)
- **Pozice odpočtu** — 4 rohy obrazovky
- **Plynulé ztlumení v posledních 10 s** — fade-out audio před vypnutím

### 4) Volitelné — Home Assistant integrace
V sekci `Home Assistant`:
- Base URL (např. `http://homeassistant.local:8123`)
- Long-lived access token (HA → Profile → Long-lived tokens)
- Refresh stavů → Připnout entity → quick toggles z TV

---

## 🔄 Automatické aktualizace (po této 0.1.0 už nic neinstaluješ)

Aplikace každých startu volá `https://zeddihub.eu/api/app-version.php?kind=tv`. Když admin panel publikuje novou verzi:
1. Spustíš app → toast „Nová verze X.Y.Z"
2. `Nastavení → Zkontrolovat aktualizace` → `Aktualizovat teď`
3. APK se stáhne přes `DownloadManager` → systémový instalátor → instalace

**Workflow administrátora** (stejný jako u Mobile):
1. Build APK (lokálně z tohoto repa) → drop do `zeddihub-tools-website/downloads/`
2. Admin panel `https://zeddihub.eu/tools/admin/?page=tv_releases`
3. Klik **Přidat release** (vyplnit verzi, code, notes) → **Publikovat**
4. Auto-syncne `version_tv.json` + endpoint začne servírovat klientům

---

## 📦 Co tato 0.1.0 obsahuje (15 sekcí v sidebar)

### Časovač & spánek
- ⏰ **Sleep Timer** — plovoucí odpočet (SYSTEM_ALERT_WINDOW) přes všechny apps; rychlé možnosti pauza/stop/vypnout přes long-press; vypnutí TV+box v 0:00; volitelný 10s audio fade
- 📅 **Plán časovače** — opakovaný timer podle dnů (Po-Pá 22:30 atd.); přežije reboot
- 🌙 **Bedtime routine** — řetěz akcí (volume_fade → start_timer → webhook); GET/POST volání pro Hue/HA/Tasmota
- ⏰ **Wake-up alarm** — opačný timer; ráno zapne TV s vybranou app
- 😴 **Smart-detect spánku** — nudge banner po N min nečinnosti + audio silent

### Síť & diagnostika
- 🌐 **Síťové nástroje** — Wake-on-LAN, paralelní ping batch, Cloudflare speed test
- 🌐 **Test sítě** — pre-flight DNS / latence / jitter / throughput s actionable rady
- 📊 **Stav TV boxu** — CPU temp, load, RAM, storage, uptime + 5-min temperature graf

### Smart home
- 💡 **Smart Home** — Hue / Tasmota / Tuya / generic webhook quick toggles
- 🏠 **Home Assistant** — dedikovaná Bearer-token integrace; pinned entity quick toggles; scene/script tester

### Sdílení & média
- 📥 **LocalSend příjem** — embedded HTTP server (port 53317) + mDNS announcement; mobilní LocalSend appka tě uvidí bez zadávání IP
- 🔖 **Watch Later** — sdílená queue z mobile/desktop; deep link routing do YT/Plex/Netflix/Twitch/Spotify
- 🎬 **Media** — quick launcher 8 streamovacích apps (Netflix, YT, Disney+, HBO, Spotify, Plex, Kodi, Twitch)

### Komunita & monitoring
- 🎮 **Herní servery** — live status z ZeddiHub backend
- 📣 **Upozornění** — banner overlay z `/api/alerts.php` (server-down auto-detect, admin push)

### Personalizace
- 🔊 **Audio výstup** — seznam výstupů (HDMI/ARC/BT/sluchátka), volume + mute control
- ♿ **Přístupnost** — Universal CC toggle, dyslektický font, system shortcuts
- 👪 **Rodičovská** — PIN keypad, bedtime window per-app, preset pravidla
- ⚙️ **Nastavení** — téma, jazyk, trigger button, pozice, fade audio, oprávnění

### Auto-update
- ⬆️ **Release-gating client** — startup poll `/api/app-version.php?kind=tv`, manuální tlačítko v Nastavení, jednoklikové instalace přes FileProvider

---

## 🔧 Technické detaily

| Pole | Hodnota |
|---|---|
| Min Android | 8.0 (API 26) |
| Target Android | 14 (API 34) |
| Tech stack | Kotlin + Jetpack Compose for TV + Material 3 + Hilt + Retrofit + DataStore + NanoHTTPD |
| APK size | ~2.5 MB |
| Signing | Release keystore (4096-bit RSA, 10 000 dní) |
| Telemetrie | Anonymizovaná (jen X-Client-Kind header) |
| FCM | Není v 0.1.0 (bude v budoucí verzi); polling alerts dnes funguje jako fallback |

---

## 🐛 Known limitations (nebudou opraveny v 0.1.0, protože už release existuje)

- **Tuya local-key AES** — odloženo (custom AES protokol)
- **HDMI-CEC bridge** — odloženo (Android nemá public CEC API)
- **Hard usage caps v Parental** — Parental sekce zatím soft-block; UsageStatsManager hard-cap přijde později
- **CC overlay napříč apps** — toggle persistuje, ale efekt napříč jinými apps (Netflix atd.) zatím není

Všechno výše bude v některé z budoucích verzí — automaticky se ti to pak nahraje, **už nic ručně neinstaluješ**.

---

## 🔗 Odkazy

- 🌐 Web: [zeddihub.eu/tools/tv/](https://zeddihub.eu/tools/tv/)
- 🛠 Admin: [zeddihub.eu/tools/admin/?page=tv_releases](https://zeddihub.eu/tools/admin/?page=tv_releases)
- 💬 Discord: [dsc.gg/zeddihub](https://dsc.gg/zeddihub)
- 📱 Mobile: [github.com/ZeddiS/zeddihub-tools-mobile](https://github.com/ZeddiS/zeddihub-tools-mobile)
