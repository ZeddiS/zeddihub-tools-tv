# ZeddiHub TV 0.1.5 — Hero Dashboard + Timer presets

Pokračování PlayStation-style refaktoru. Dashboard a Timer dostaly velký vizuální upgrade.

## 📊 Dashboard

**Před:** „Holé" číslo času 88sp na pozadí + 3 info dlaždice + řada streaming aplikací.

**Teď:**
- **Hero Clock Card** — gradient surface (180dp výška) zakotvuje obrazovku, čas 92sp Bold + datum 16sp v levém spodním rohu, weather chip v pravém horním
- **Quick actions row** — 4 velké `PsBigTile` (Sleep Timer / Plán / Bedtime / Stav TV) se scale-on-focus
- **App launcher row** — 6 streamovacích aplikací jako `PsBigTile` s individual brand colored shadows na focus (Netflix red, Disney+ blue, Spotify green, atd.)
- Sjednocený pattern napříč Dashboard

## ⏰ Časovač

**Před:** Malé pill chipy „15 min" / „30 min" / atd. v jedné řadě.

**Teď:**
- **Velké preset karty** — 2 řady × 3 karty (15/30/45/60/90/120 min), každá s 24sp Bold textem ve středu, fill z weight, oranžové pozadí na focus
- **Velký status card** — čas 64sp Bold v primary color, 28dp padding
- **PS-style buttony** pro ovládání — `PsPrimaryButton` (pozastavit/pokračovat) a `PsSecondaryButton` (zastavit / vypnout teď) s glow shadow
- Permission warnings v `ZhCard` s error tone

## 📦 Tech

| | |
|---|---|
| versionCode | 5 → 6 |
| versionName | 0.1.4 → 0.1.5 |
| APK velikost | ~2.7 MB |

## 📲 Co teď

Tvoje 0.1.4 instalace dostane prompt sama → toast „Nová verze 0.1.5" → aktualizovat.

## Roadmap → 0.1.6

- Schedule + Routine refaktor na PS-style choice cards
- Watch Later jako PsBigTile grid
- Smart Home + Home Assistant rozhraní polished
