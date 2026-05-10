# ZeddiHub TV 0.1.10 — App Releases fix + Google TV home + Wizard + Theme

**Datum:** 2026-05-10
**Verze:** 0.1.10 (versionCode 11)
**Status:** Pending → Publish přes Admin panel

## 4 hlavní změny

### 1. 🛠 App Releases — kompletní oprava

**Problém:** „Spousta pending verzí, které už dávno vyšly".

**Root cause:** `ar_import_staged()` měla footgun. Staged entries bez `status` pole defaultovaly na `pending`. Při každém otevření admin stránky import logika downgradovala archived rows zpět na pending — tím se pending nekonečně rozšiřovaly.

**Fix — true Hybrid SOT:**
- `staged_releases.json` definuje **co existuje** + release notes
- DB drží **runtime stav** (LIVE / pending / archived)
- Staged může pouze **RETIRE** verze (signál `archived`), nikdy je revivnout
- Existing DB row = autoritativní; staged ho nedotýká pokud nezavolá explicit archived

**Nové features v admin panelu:**
- 🧹 **Globální „Vyčistit všechny platformy"** — gradient orange/purple button, viditelný jen když je drift
- 🧹 **Per-platform „Vyčistit (N)"** — rudý ghost button na každé platformě s drift > 0
- ⚠ **Drift detection** — automatický počet zastaralých pending; UI ukazuje rudý badge `⚠ N ZASTARALÝCH PENDING` nebo zelený `✓ ČISTÝ STAV`
- 🟢 **Status pills** — LIVE green / PENDING amber / archived gray; rudý border + glow shadow na karty s driftem

### 2. 🏠 Google TV / PlayStation 5 style Dashboard

**Konec seznam-like UI.** Hlavní obrazovka teď vypadá jako moderní TV launcher:

- **Hero clock card 220 dp** s breathing gradient (7s infinite-repeat), ZeddiHub logo top-left, frosted weather chip top-right, 96 sp tighter time bottom-left
- **5 horizontálních řad** (LazyRow se scroll):
  - ⚡ **Rychlé akce** (5 dlaždic) — Sleep Timer / Plán / Bedtime / Budík / Stav TV
  - 🎬 **Streamování** (admin-curated 8 apps z `/api/tv-config.php`) — Netflix / YouTube / Disney+ / HBO / Spotify / Plex / Kodi / Twitch
  - 🛠 **Nástroje** (6 dlaždic) — Síť / Diagnostika / Média / Prohlížeč / Soubory / Audio
  - 🏠 **Domácnost & sdílení** (5 dlaždic) — Smart Home / HA / Watch later / LocalSend / Servery
  - ⚙️ **Systém** (4 dlaždice) — Upozornění / Přístupnost / Rodičovská / Nastavení
- **Velké barevné dlaždice 180×120 dp** s scale-on-focus 1.08 + per-tile glow shadow
- **Streaming dlaždice 220×130 dp** s brand-tinted glow (Netflix red, Spotify green, Plex orange…)
- **D-pad navigace:** vertikálně mezi řadami, horizontálně v řadě
- **Klikání funguje** — opraveny `onClick = { }` placeholdery; každá dlaždice teď navrátí přes NavController

### 3. ✨ Wizard Skip button

**Problém:** „Přeskočit" a „← Zpět" vypadaly identicky (oba PsSecondary), uživatel nedokázal rozlišit při pohledu z gauče.

**Fix:** Nová **`PsTertiaryButton`** (red ghost outline):
- 1.5 dp slate outline na default, **2 dp red outline + red-tinted background** na focus
- Muted slate text default, **soft red text** na focus
- Používá se v wizardu jako `✕ Přeskočit setup`
- Vizuálně okamžitě rozeznatelná od `← Zpět` (PsSecondary, šedá ghost) a `Další →` (PsPrimary, oranžová full)

### 4. 🎨 Theme — text visibility fix

- **tertiary** explicitně definovaný `#A78BFA` (fialová) — komplement k brand orange. Předtím TV M3 default fightoval s gradientem v hero kartě a update dialog
- **onSurfaceVariant** zjasněn z `#A0A0B8` → `#B8B8CC` pro lepší čitelnost caption textu napříč aplikací
- Přidány: `secondaryContainer`, `onSecondaryContainer`, `onTertiary`, `tertiaryContainer`, `onTertiaryContainer`

## Workflow pro release

1. ✅ Build APK (3m 41s) → `ZeddiHub-TV-0.1.10.apk` (2.7 MB, signed)
2. ✅ Deploy do `zeddihub-tools-website/downloads/`
3. ✅ `staged_releases.json` — TV 0.1.10 = pending, 0.1.9 demoted na archived
4. ✅ Git commit + tag v0.1.10 + push
5. ✅ GitHub release s APK asset
6. **Admin:** TV releases → 0.1.10 → Test APK → Publikovat

## Co dělat po updatu

1. Po publishi 0.1.10 jdi do **Admin → System → App releases**
2. Uvidíš nahoře drift count (např. `⚠ 23 ZASTARALÝCH PENDING`)
3. Klikni **🧹 Vyčistit všechny platformy** → confirm → vše zastaralé → archived
4. **TV klient** dostane prompt na 0.1.10 při příštím cold startu
5. Po prvním spuštění uvidíš nový GTV-style home screen — D-pad nahoru/dolů mezi řadami, vlevo/vpravo v řadě
6. Klikni na libovolnou dlaždici — naviguje na příslušnou sekci (předtím nedělala nic)
