# ZeddiHub TV 0.1.8 — PlayStation polish complete

**Datum:** 2026-05-10
**Verze:** 0.1.8 (versionCode 9)
**Status:** Pending → Publish přes Admin panel

## Hlavní změna

Dokončili jsme **PlayStation-style refactor napříč všemi obrazovkami**.
Každá obrazovka má teď:

- **PageHeader** s ikonou v primary-tinted kruhu (uniformní vzhled)
- **PsBigTile / PsBigChoice** karty se scale-on-focus 1.04-1.06 a glow shadow
- **PsPrimaryButton / PsSecondaryButton** velké hladké tlačítka (scale-on-focus, focus glow)
- **StatusPill** + **Tone** paleta (Success/Warning/Error/Info/Muted) sjednocená
- **EmptyState** s ikonou a hintem místo holého textu
- **SectionTitle** uppercase s letter-spacing pro vizuální hierarchii

## Co se v této verzi dotklo

### `AccessibilityScreen`
- 2× **PsBigChoice** karty pro Universal CC + dyslektický font (s ikonou v boxu, popisem stavu)
- Systémové zkratky jako **PsSecondaryButton** trio (Captioning / Accessibility / Display)

### `ParentalScreen`
- PIN sekce s 3×4 D-pad keypad (custom `PsKeyButton` 80×56 dp, scale-on-focus)
- PIN status header s **Lock/LockOpen** ikonou + **StatusPill** (aktivní/vypnuto)
- PIN input vizualizace: tečky • + podtržítka _ s primary highlight při validu (4-6 znaků)
- **RuleCard** s Lock ikonou, PIN/bedtime **StatusPill**, PsSecondaryButton akce
- 4 preset buttony pro Netflix / YouTube TV / Disney+ / Twitch

### `BrowserScreen`
- **PageHeader** s Public ikonou + **🏠 Domů** v trailing
- Status strip: 3× **StatusPill** (loading %, TV UA, Bez cookies)
- Záložky jako pill-shaped tiles (primary fill on focus, scale, focus glow)
- WebView panel zachován (TV UA, no persistent cookies)

### `MediaScreen`
- **4-col PsBigTile grid** — všech 8 streamovacích apps s brand-tinted glow shadow při focusu (Netflix red, YouTube red, Spotify green, Plex orange, atd.)
- Toast když app není nainstalován (přes `applicationContext`)

### `LocalSendScreen`
- **PageHeader** s Wi-Fi/WiFiOff ikonou (mění se podle stavu)
- Hero status card: primary gradient když běží, mute když ne
- 3× **StatusPill**: online/offline + mDNS ✓/off
- Velký 28sp Bold address při běhu serveru
- **EmptyState** pro "zatím nic nepřišlo"
- **PsPrimary/Secondary** akční tlačítka v headeru

### `SettingsScreen`
- **PageHeader** + **SectionTitle** sekce: Vzhled, Časovač, Aktualizace, Oprávnění, O aplikaci
- Aktualizace karta: velký 22sp Bold version + **StatusPill** code + **PsPrimaryButton** check
- Oprávnění: 3× **PsSecondaryButton** trio (Overlay / Přístupnost / Zvuk) s emoji
- Toggle row: **StatusPill** zapnuto/vypnuto + **PsSecondaryButton** přepínač
- O aplikaci: tinted **ZhCard**

## Technické

- **versionCode:** 8 → 9
- **versionName:** 0.1.7 → 0.1.8
- Žádné nové permissions, žádné nové dependencies
- Žádné změny v ViewModelech / repository / API vrstvě
- Cílem byl čistě UI polish — funkční chování zůstává

## Workflow

1. Build APK ✅ (3-5 min)
2. Deploy do `zeddihub-tools-website/downloads/ZeddiHub-TV-0.1.8.apk` ✅
3. Update `staged_releases.json` (TV 0.1.8 = pending) ✅
4. Update `version_tv.json` (manifest)
5. Git commit + tag v0.1.8 + push
6. GitHub release (přiloží APK)
7. Admin panel: TV releases → 0.1.8 → Test APK → Publikovat

## Status

**Pending** v admin panelu. Aktivuje se až přes „Publikovat".
TV klienti na 0.1.7 dostanou prompt při dalším startu.
