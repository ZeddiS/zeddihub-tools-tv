# ZeddiHub TV 0.1.6 — Schedule, Routine & Watch Later polish

Třetí vlna PS-style refaktoru — sjednocené tři důležité screeny pod nový design language.

## 📅 Plán (Schedule)

- **Schedule rows** s velkým časem 28sp Bold vlevo (110dp wide column), title + dny + délka spánku, akce jako `PsSecondaryButton`
- **Schedule editor** kompletně přepsán:
  - Time picker s `StepBtn` ± tlačítky a 28sp Bold display, řádek vystředěný
  - Duration row jako Pill chips (15/30/45/60/90/120 min) — selected = primary fill, focused = primary glow
  - Days picker — Po-Ne pills s same selection pattern
  - Day presets row (Po-Pá / So-Ne / Každý den)
  - Footer s `PsPrimaryButton` (Uložit) + `PsSecondaryButton` (Zrušit / Smazat)
- **EmptyState** karta když je seznam prázdný

## 🌙 Bedtime routine

- **Step cards** s **numbered circle** (40dp, accent-colored) + emoji icon (24sp) + title (16sp Bold) + description
- Per-step accent: volume_fade = info purple, start_timer = primary orange, delay = muted gray, webhook = success green
- Header s `PsPrimaryButton` „▶ Spustit teď" vpravo
- Add-step row s `PsSecondaryButton` chips dole (↺ Výchozí / + Fade out / + Spustit timer / + Pauza / + Webhook)
- **EmptyState** placeholder když routine nemá kroky

## 🔖 Watch Later

- **Item cards** se source pill barevně podle origin: YouTube red / Plex orange / Netflix red / Twitch purple / Spotify green / generic = info
- Title 16sp Bold + URL 11sp pod ním (1 řádek max), watched items dimmed
- `PsSecondaryButton` „✓ Označit" pro mark-as-watched, nahrazený `StatusPill` „shlédnuto" když už je
- **EmptyState** když je fronta prázdná

## 📦 Tech

| | |
|---|---|
| versionCode | 6 → 7 |
| versionName | 0.1.5 → 0.1.6 |
| APK velikost | ~2.7 MB |

## Roadmap → 0.1.7

- Smart Home a Home Assistant na PS-style device tiles
- Wake-up alarm karty
- Network / Files / Browser polish
- Konečné sjednocení všech screenů na shared komponenty
