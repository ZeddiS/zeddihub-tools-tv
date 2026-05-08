# ZeddiHub TV 0.1.2 — Sjednocené releases v admin panelu + auto-update fix

**Toto je verze, kterou tvoje TV uvidí jako první auto-update prompt.**

## 🐛 Proč 0.1.1 nepřišel

Backend `/api/app-version.php?kind=tv` čte data ze dvou míst:
1. **DB** `zh_app_releases WHERE platform='tv' AND status='published'`
2. **Fallback** `tools/data/staged_releases.json` filtrovaný na `platform='tv'`

Žádný z těchto zdrojů neměl 0.1.1 záznam — DB byla prázdná pro TV (admin nikdy nepublikoval) a staged_releases měl jen mobile/watch řádky. Endpoint vracel `version_code=0` → app interpretovala jako „žádný update".

`version_tv.json` který jsem aktualizoval čte jen **landing stránka** (statický manifest pro browser CTA), nikoli `/api/app-version.php`.

## 🛠 Fix v 0.1.2

### Backend
- `staged_releases.json` přidán TV řádek pro 0.1.2 → endpoint okamžitě začne servírovat
- `/api/app-version.php` polymorphic (z předchozí session): `?kind=tv` ↔ `?platform=tv` jako alias, response obsahuje `live: {...}` wrapper i flat fields

### Admin panel
- **TV releases sjednocené pod App Releases** — platform tabs (📱 Mobile | 📺 TV) na vrcholu obou stránek
- `?page=app_releases&platform=tv` deleguje na TV pipeline (zachovává funkční mobile flow bez refaktoru)
- TV releases entry skryt v sidebaru (nyní dostupný přes tabs z App Releases)
- Tabs persistované styly: aktivní záložka oranžovou, neaktivní šedou s hover

### App
- Všechny features z 0.1.1 (které jsi neviděl):
  - 🎨 Oficiální ZeddiHub logo (banner, launcher, side menu)
  - 🟠 Oranžová brand barva napříč celou app
  - 🧭 Collapsible navigace s 7 skupinami (žádné off-screen tlačítko)
  - 🚀 Setup wizard (10 kroků: jazyk, téma, perms, trigger, roh, počasí, HA)
  - 🌐 Embedded prohlížeč s bookmarks
  - 📁 Files explorer (Local + USB + LocalSend + SMB placeholder)

## 📲 Co teď

Tvoje 0.1.0 instalace na TV při dalším startu:
1. UpdateChecker poll → endpoint vrátí `version_code=3` (>1)
2. Toast „Nová verze 0.1.2 — otevřete Nastavení pro instalaci"
3. **Nastavení → Zkontrolovat aktualizace → Aktualizovat teď** → DownloadManager + system installer

## 🔧 Tech

| | |
|---|---|
| versionCode | 2 → 3 |
| versionName | 0.1.1 → 0.1.2 |
| APK velikost | ~2.7 MB |
| Min Android | 8.0 (API 26) |

## Pro budoucnost

Workflow pro další releases (admin panel teď funguje plně):
1. Bump versionCode + versionName v `app/build.gradle.kts`
2. `./gradlew assembleRelease` → APK do `downloads/`
3. Admin: **App Releases → TV tab → Přidat → Test APK → Publikovat**
4. Auto-archive starších, sync `version_tv.json`, GitHub release auto-create, cleanup APK
5. TV klient dostane prompt při dalším startu
