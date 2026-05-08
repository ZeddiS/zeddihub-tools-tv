# ZeddiHub TV 0.1.1 — Brand, navigace, wizard, prohlížeč, soubory

První update — **automatická aktualizace** ti to nahraje sama, instalovat ručně nemusíš.

## 🎨 Branding

- Aplikace má teď oficiální **ZeddiHub logo** všude — TV banner v Leanback launcheru, adaptive launcher icon, header postranního menu
- **Oranžová brand barva** (matching logo) napříč celou app — sjednocené s desktop / mobile / webem
- Setup wizard začíná logem na úvodní obrazovce

## 🧭 Nová navigace (fix off-screen menu)

- **Collapsible side rail** — ze začátku úzký se samými ikonami (88dp), při fokusu se rozbalí na 280dp s labels + group headers
- 7 logických skupin: **Domů / Časovač & spánek / Chytrá domácnost / Síť & audio / Sdílení / Média & servery / Osobní**
- LazyColumn → D-pad up/down auto-scrolluje (žádné už nic mimo obraz)
- Overscan-safe padding (32dp horizontal / 16dp vertical) — nic neořízne i na TV s 5% bezelem

## 🚀 First-run setup wizard

Při prvním spuštění tě provede 10 kroky:
1. Welcome
2. Jazyk (CS / EN / podle systému)
3. Téma (tmavé / AMOLED / podle systému)
4. Zobrazení přes ostatní aplikace (deep link na Settings)
5. Služba přístupnosti (deep link)
6. Trigger button časovače (OK / Back / Home / Menu)
7. Pozice odpočtu (4 rohy)
8. Město pro počasí (Praha / Brno / Ostrava / Plzeň)
9. Home Assistant URL + token (skip OK)
10. Recap + Finish

`Přeskočit` na jakémkoli kroku → defaulty + flag `wizardCompleted = true`. Pozdější změny v sekci **Nastavení**.

## 🌐 Internet prohlížeč (nová sekce **Prohlížeč**)

- Embedded WebView se záložkami v rychlém panelu
- Default bookmarks: ZeddiHub, ZeddiHub Tools, DuckDuckGo, YouTube TV, Wikipedia, Counter-Strike
- Vlastní bookmarks persistované v DataStore
- Desktop User-Agent (mobile UA na TV způsobuje broken layouts)
- Žádné persistent cookies (TV je sdílený, leak risk) — pro logged-in zážitek použij dedikované apps

## 📁 Průzkumník souborů (nová sekce **Soubory**)

Čtyři zdroje:
- **Lokální** — primární storage
- **USB / SD** — auto-detekce externí storage v `/storage/`
- **LocalSend** — soubory přijaté přes LocalSend HTTP receiver (z mobilu / desktopu)
- **SMB / NAS** — placeholder, plnohodnotná implementace v 0.5+

Tap soubor → ACTION_VIEW + content:// URI + MIME → libovolný nainstalovaný přehrávač/viewer.
Ikony rozlišují typy: 🖼 obrázky / 🎬 video / 🎵 audio / 📄 docs / 📦 ostatní.

## 🐛 Opravy

- Layout: poslední tlačítko v menu už není mimo obraz (LazyColumn + scroll)
- Overscan: padding kolem obsahu pro starší TV s ořízlým bezelem
- Theme: odstraněno míchání TV blue (#3B82F6) s brand orange — všechno teď orange

## 🔧 Tech

| | |
|---|---|
| versionCode | 1 → 2 |
| versionName | 0.1.0 → 0.1.1 |
| APK velikost | ~2.7 MB |
| Min Android | 8.0 (API 26) |
| Permissions přidané | READ_EXTERNAL_STORAGE (≤32) + READ_MEDIA_{IMAGES,VIDEO,AUDIO} pro Files browser |

## 📲 Jak dostaneš tuto verzi

**Nic neděláš.** Při dalším spuštění aplikace na TV:
1. UpdateChecker zavolá `/api/app-version.php?kind=tv`
2. Uvidí `version_code=2` (vyšší než tvoje 1) → toast „Nová verze 0.1.1"
3. Otevři Nastavení → Zkontrolovat aktualizace → **Aktualizovat teď**
4. APK se stáhne přes DownloadManager → systémový instalátor → instalace

Žádný sideload, žádný ADB. **Tohle je důvod, proč jsme tě nutili nainstalovat 0.1.0 ručně** — od teď to jede samo.
