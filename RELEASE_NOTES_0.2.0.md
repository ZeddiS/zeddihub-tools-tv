# ZeddiHub TV 0.2.0 — Schedule, Routine & Health

Druhý release, primárně rozšíření Sleep Timer ekosystému + nový diagnostický panel.

## Nové funkce

### 📅 Plán časovače (Schedule presets)
Konečně nemusíš spouštět časovač každý večer ručně.
- Vytvoř plán: čas spuštění, dny v týdnu, doba trvání.
- Presets pro Po-Pá / So-Ne / Každý den.
- Plán běží přes AlarmManager (`setExactAndAllowWhileIdle`) — přesné na vteřinu i při Doze.
- Přežije reboot (TimerBootReceiver re-armuje alarmy při startu).
- Vyžaduje permission `SCHEDULE_EXACT_ALARM` (Android 12+); pokud není, fallback na inexact.

### 🌙 Bedtime routine (řetěz akcí)
Jeden press → posloupnost akcí:
- **Volume fade** — postupně ztlumí zvuk (konfigurovatelné % a doba)
- **Start timer** — spustí Sleep Timer s vybranou dobou
- **Pauza** — vyčkat N sekund mezi akcemi
- **Webhook** — GET/POST na URL (Hue / Home Assistant / Tasmota / cokoli s HTTP)
- Krokový editor v aplikaci, výchozí routina pre-loaded.

### 📊 Stav TV boxu (Health monitor)
Real-time monitoring boxu:
- Teplota CPU (z `/sys/class/thermal/thermal_zoneN/temp`, automaticky vybere CPU zónu)
- CPU load (delta z `/proc/stat`)
- RAM usage / Storage usage / Uptime
- Baterie (pokud je) — pro tablety s Android TV módem
- 5-minutový graf teploty s barevnou škálou (zelená/žlutá/červená)
- Vzorky každých 5 sekund

## Opravy

- **runBlocking na main threadu** — `TimerOverlayManager.chipLayoutParams()` četl DataStore přes `runBlocking`. Cachuji teď roh reaktivně přes flow collector v init bloku, žádné main-thread blokování.
- **DownloadManager → cacheDir nemůže psát** — `UpdateChecker.startInstall()` ukládal APK do `ctx.cacheDir`, ale DM s privátní storage nepracuje. Teď se používá `getExternalFilesDir(null)`.
- **Toast leak z Activity kontextu** — `UpdateChecker.checkOnStartup()` držel referenci na MainActivity. Teď captures `applicationContext`.
- **SupervisorJob v SleepTimerService** — pokud spadl jeden child coroutine (např. fade-out), cancelnul celý scope. Teď izolováno přes SupervisorJob.

## Technické detaily

- **versionCode**: 1 → 2
- **versionName**: 0.1.0 → 0.2.0
- **APK velikost**: ~2.5 MB (přidání health/routine/schedule modulů)
- **Nová oprávnění**: `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`
- **Nové obrazovky v sidebar**: Plán, Bedtime, Stav TV
- **Bundled toolchain**: stále sdílený s `zeddihub_tools_mobile`

## Roadmap → 0.3.0

- LocalSend (cross-device file receive)
- Wake-up alarm
- Hue / Tasmota / Tuya remote
- Watch later inbox
