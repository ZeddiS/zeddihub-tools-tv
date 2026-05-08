# ZeddiHub TV 0.4.0 — mDNS, Connection test, Audio, Alerts

Čtvrtý release dokončuje LocalSend integraci (auto-discovery z mobilu) a přidává tři nové diagnostické / utility sekce.

## Nové funkce

### 📡 mDNS discovery pro LocalSend
- TV se sama hlásí jako `ZeddiHub TV` na `_localsend._tcp.` Bonjour service
- Mobilní LocalSend appka (Android/iOS) ji teď uvidí v device-list **bez ručního zadání IP**
- NsdManager registrace + auto-cleanup při stop/destroy
- LocalSendScreen ukazuje mDNS stav (registered/not)

### 🌐 Test sítě (pre-flight diagnostika)
Před streamem zkontroluje 4 metriky paralelně:
- **DNS resolve** — 3 hosty (zeddihub.eu, google, cloudflare), průměrná latence
- **Latence** — TCP ping na 1.1.1.1 / 8.8.8.8 / 1.0.0.1
- **Jitter** — 10 pings, σ směrodatná odchylka
- **Throughput** — 1 MB blob z Cloudflare speedtest

Verdikt (GOOD/OK/BAD/OFFLINE) + per-step actionable rady ("σ = 80 ms — přejdi blíž k routeru").

### 🔊 Audio výstup
- Seznam připojených audio výstupů (HDMI, ARC, Bluetooth, sluchátka, USB) s ikonkou
- Aktuální volume v % + tlačítka -10/+10/Mute
- Typ device label (cs): Reproduktor, HDMI ARC, Bluetooth headset, atd.
- Auto-refresh každé 2 s (reaguje na BT připojení)
- Shortcut tlačítka do Bluetooth a Sound systémových settings

### 📣 Upozornění (Alerts)
- Polling endpoint `/api/alerts.php?kind=tv` každých 60 s
- Banner overlay přes všechny aplikace (TYPE_APPLICATION_OVERLAY)
- Severita info / warn / error → modrá / oranžová / červená
- TTL-based auto-dismiss + dismiss button
- Seen-IDs persistované v DataStore (200-entry ring buffer) → žádné duplicate showy
- Server-down detection v backendu (auto-generuje alert pokud server > 2 min offline)
- "Test overlay" tlačítko v AlertsScreen pro vizuální verifikaci

## Backend

- **`api/alerts.php`** — public read-only endpoint, kombinuje 2 zdroje:
  - `tools/data/alerts.json` (admin-curated, push composer write target)
  - `tools/data/server_status.json` (server-monitor auto)
- Kind filtering, severity sorting, TTL filtering, fail-open na missing files
- 30s cache header

## Architektura

```
com.zeddihub.tv/
├── alerts/          ← AlertsPoller + AlertOverlayManager + Screen
├── audio/           ← AudioOutputs (AudioManager wrapper) + Screen
├── diag/            ← ConnectionTest (4 paralelní probes) + Screen
├── localsend/       ← + NsdManager mDNS registration
└── ...
```

## Versioning

- versionCode 3 → 4
- versionName 0.3.0 → 0.4.0
- 3 nové route: `alerts`, `audio`, `conn_test`
- 1 nová DataStore klíč: `alerts_seen_json`

## Roadmap → 0.5.0

Zbývající z původního výběru:
- Tuya local-key AES protokol (vlastní LAN integrace bez cloud)
- HDMI-CEC bridge přes vlastní protokol (TV ↔ desktop ↔ AVR)
- Home Assistant dedikovaná integrace (long-lived token, scenes autodiscovery)
- Smart auto-detect spánku
- Screen time / parental
- Universal CC + dyslektický font
- FCM (až google-services.json bude k dispozici) — alerts poller funguje jako fallback
