# ZeddiHub TV 0.1.4 — PlayStation-style UI

Velká vizuální iterace zaměřená na první dojem (Setup Wizard) + nová sada PS-stylových komponent pro budoucí refaktor zbytku aplikace.

## 🎮 PlayStation-style komponenty (`ui/components/PsComponents.kt`)

| Komponenta | Účel |
|---|---|
| **`PsHeroFrame`** | Full-screen scaffold pro hero pages — header (title 36sp + subtitle + step indicator) + scrollable content + sticky bottom action bar |
| **`PsBigChoice`** | Velký vyběrový tile (icon + title 20sp + description) — scale 1.04 + glow shadow při focus, oranžové vyplnění při selected |
| **`PsBigTile`** | Tile pro app launchery — scale 1.06 + accent-colored shadow při focus |
| **`PsPrimaryButton`** | Velký primární button s oranžovým glow (Next/Save/Continue) |
| **`PsSecondaryButton`** | Outlined sekundární button (Back/Skip/Cancel) |
| **`PsStepIndicator`** | Modern progress dots — aktuální dot 14dp, ostatní 10dp, "X / N" caption |

Design tokens: padding 32dp+, scale-on-focus animations (150ms tween), shadow elevation 14-18dp při focus, typo ramp 14-44sp.

## 🚀 Setup Wizard — kompletní rewrite

**Před:** Tlačítka „nesedla", layout cramped, vše v `Column { padding(8.dp) }`, malé pill-style buttony.

**Teď:**
- **Full-screen layout** s 64dp horizontal + 56dp top padding
- **Header s title 36sp Bold** + subtitle + step indicator vpravo
- **Velké výběrové karty** místo pill rows — každá má icon (56dp v zaobleném boxu), title 20sp Bold, description, ✓ při selected
- **Sticky action bar** dole — Back / Skip vlevo, Next / Finish vpravo, primární orange button, secondary outlined
- **Scale + glow** na všechny choice cards a buttony při focus
- **Welcome step** s logem (120dp) vlevo + text vpravo
- **Permission steps** s big icon (48dp) + bullet list co potřebuje + primární CTA + hint text
- **Done step** s velkým „Hotovo!" 28sp orange + recap rows

## 📦 Tech

| | |
|---|---|
| versionCode | 4 → 5 |
| versionName | 0.1.3 → 0.1.4 |
| APK velikost | ~2.7 MB |

## 📲 Co teď

Tvoje 0.1.3 instalace dostane prompt sama — restart app → toast „Nová verze 0.1.4" → Nastavení → Aktualizovat teď. Po reinstalaci, pokud ještě nedokončíš wizard nebo si přidáš `?reset_wizard` přes nastavení (v 0.1.5+), uvidíš plně nový PS-style flow.

## Roadmap → 0.1.5

- Refaktor Dashboard / Schedule / Routine na PS-style komponenty
- App launcher tiles (Dashboard) přepsat na `PsBigTile` se scale-on-focus
- Hero clock card větší (full-width banner)
- Quick actions row pod hero clock
