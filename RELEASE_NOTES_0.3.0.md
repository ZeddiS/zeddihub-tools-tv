# ZeddiHub TV 0.3.0 — Wake-up, Smart Home, LocalSend & Watch Later

Třetí release rozšiřuje aplikaci o **4 velké nové sekce** v sidebaru.

## Nové funkce

### ⏰ Wake-up alarm (Budík)
Opačná funkce ke Sleep Timeru — TV se ráno **automaticky probudí**.
- Konfigurovatelný čas + dny v týdnu (Po-Pá / So-Ne / Každý den)
- Volitelně spustí konkrétní aplikaci po probuzení (Netflix, YouTube, Spotify, atd.)
- Nastavitelná hlasitost při probuzení (20-80% z max)
- Použití: ranní budík s YouTube Morning News místo nudy
- Přežije reboot (TimerBootReceiver re-armuje budíky)

### 💡 Chytrá domácnost (Smart Home)
Dedikované klienty pro nejběžnější DIY chytré domácnosti:
- **Hue světlo** (PUT na bridge `/api/<user>/lights/<id>/state`)
- **Hue místnost** (group action endpoint)
- **Tasmota** (REST `/cm?cmnd=Power%20ON` s volitelnou autentizací)
- **Tuya local** (zatím přes webhook — full LAN protokol v 0.4.0)
- **Webhook** (catch-all pro Home Assistant / IFTTT / vlastní bridge)
- ON/OFF přímo z karty zařízení; chyby zobrazené jako toast

### 📥 LocalSend příjem
Přijímej soubory z mobilu / desktop přes lokální Wi-Fi.
- Embedded NanoHTTPD server na portu 53317
- Implementuje LocalSend v2 protokol (`/api/localsend/v2/info`, `/prepare-upload`, `/upload`)
- Discovery přes mDNS přijde v 0.4.0; zatím manuální zadání IP v sender appce
- Zobrazí "pošli na IP:port" hint při zapnutém serveru
- Soubory ukládá do `Android/data/com.zeddihub.tv/files/Download/LocalSend/`
- Historie posledních 50 přijatých souborů

### 🔖 Watch Later inbox
Sdílená fronta odkazů ze všech ZeddiHub klientů → otevři na TV jedním klikem.
- Backend endpoint `/api/watchlater.php` (POST z mobile/desktop, GET z TV)
- Auto-detekce zdroje (YouTube / Plex / Netflix / Twitch / Spotify) podle URL hostname
- Klik na položku → `Intent.ACTION_VIEW` → otevře v cílové appce přes deep link
- Označit jako zhlédnuté (mizí ze seznamu)
- Lokální cache — funguje i offline

## Backend rozšíření

- Nový endpoint `/api/watchlater.php` v `zeddihub-tools-website` (JSON-file storage v `tools/data/watchlater.json`)
- Auth: stejný `X-App-Secret` pattern jako ostatní client endpoints

## Kód

- **6 nových modulů**: `timer/wakeup/`, `smarthome/`, `localsend/`, `watchlater/`
- **8 nových obrazovek** v navigaci: Schedule, Routine, **WakeUp**, **SmartHome**, **WatchLater**, **LocalSend**, Health, …
- **Závislost**: NanoHTTPD 2.3.1 (~75 KB) pro LocalSend server
- **Permissions**: žádné nové (LocalSend používá existující `INTERNET`)

## Technické

- versionCode: 2 → 3
- versionName: 0.2.0 → 0.3.0
- APK velikost: ~2.5 MB (NanoHTTPD nepřispěl moc)

## Roadmap → 0.4.0

- mDNS discovery pro LocalSend (NSD)
- Tuya LAN (AES protokol)
- HDMI-CEC bridge
- Home Assistant integrace s autodetekcí
- Server down overlay alert (FCM / polling)
- Push notifikace overlay
- Connection test (pre-flight)
