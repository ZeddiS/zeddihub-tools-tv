# ZeddiHub TV 0.1.3 — Vizuální polish

Sjednocení vzhledu napříč aplikací — všechny screeny teď vypadají jako součást stejné rodiny, ne jako 20 nesouvisejících obrazovek od různých developerů.

## 🎨 Nové shared UI komponenty (`ui/components/Components.kt`)

| Komponenta | Účel |
|---|---|
| **`ZhPageScaffold`** | Jednotný kontejner pro každý screen — vertikální 16dp gap, end padding 8dp |
| **`PageHeader`** | Title 26sp Bold + subtitle 13sp + volitelná leading kruhová ikona + trailing slot pro tlačítko |
| **`ZhCard`** | Jednotný surface — RoundedCornerShape 14dp, padding 18dp, themable container |
| **`SectionTitle`** | Malá UPPERCASE 11sp sekce hlavička, letterSpacing 0.6sp |
| **`EmptyState`** | Vystředěná karta s ikonou + nadpisem + hint pro prázdné seznamy |
| **`StatusPill`** | Mini barevné pill s tone (success/warn/error/info) |
| **`KpiTile`** | Statistická karta s ikonou + label + value + tone |
| **`Tone`** | Sjednocené barvy: Success #22C55E / Warning #F59E0B / Error #EF4444 / Info #A78BFA / Muted #6B7280 |

## 📺 Refactored screens (9 hlavních)

Každý nyní používá `ZhPageScaffold` + `PageHeader` + `ZhCard`:

- **Dashboard** — větší clock (88sp), kompaktnější WeatherCard, lehčí padding
- **Nastavení** — Section helper, rovnoměrné rozestupy, sjednocené ChoiceRow
- **Herní servery** — EmptyState pro prázdný backend, StatusDot s Tone barvami
- **Síťové nástroje** — sjednocené Card pattern, ping výsledky barevně (zelená/červená)
- **Audio** — KpiTile-like volume row, EmptyState pro žádné výstupy, StatusPill „aktivní"
- **Stav TV boxu** — KpiTile pro CPU/temp/RAM/storage/baterii, jednodušší graf card
- **Test sítě** — verdict banner přes ZhCard, per-step rows s ✓/× ikonkami
- **Soubory** — EmptyState per zdroj (LocalSend/SMB/External), kompaktnější FileRow
- **Upozornění** — sjednocený layout s SectionTitle + EmptyState

## 🐛 Konzistentní oprávnění

Audit našel mírné nesoulady — opraveny:
- Všechny barvy ze sdíleného `Tone.*` (předtím inline `Color(0xFF...)` na 7 místech)
- `formatRemaining`, `humanSize`, `formatUptime` zůstávají per-package, ale budou centralizovány v 0.2.x

## 🔧 Tech

| | |
|---|---|
| versionCode | 3 → 4 |
| versionName | 0.1.2 → 0.1.3 |
| APK velikost | ~2.7 MB |

## 📲 Co teď

Tvoje 0.1.2 instalace dostane prompt sama — restart app → toast „Nová verze 0.1.3" → Nastavení → Aktualizovat teď.

## Roadmap

Další iterace bude tedy zaměřená na zbývajících 11 screenů (Browser, WatchLater, LocalSend, WakeUp, Schedule, Routine, Timer, Accessibility, Parental, SmartHome, HomeAssistant, Media) — funkčně OK, ale ještě nepoužívají shared komponenty. V 0.1.4.
