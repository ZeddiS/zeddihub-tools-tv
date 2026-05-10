# ZeddiHub TV 0.1.7 — Smart Home, Home Assistant & Budík polish

Čtvrtá vlna PS-style refaktoru — chytrá domácnost a budík sjednocené pod nový design language.

## 💡 Chytrá domácnost

- **Device cards** s velkým 36sp emoji vlevo, name 16sp Bold + `StatusPill` typu (Hue / Tasmota / Tuya / Webhook), per-kind accent barva (Hue žlutá, Tasmota info, Tuya zelená, Webhook orange)
- **Power buttons** ON/OFF jako `Surface` s tone-colored background (success green pro ON, muted gray pro OFF), focused = full color
- **PsSecondaryButton** Upravit / Smazat
- **Editor** s velkým title 22sp + KindPill chips s 5 typy zařízení + read-only details v ZhCard + `PsPrimaryButton` Uložit
- **EmptyState** placeholder když žádná zařízení

## 🏠 Home Assistant

- **Connection status card** se zeleným/šedým dot indikátorem + URL/Token info + ping result `StatusPill` (success/error)
- **Entity cards** s **velkým emoji per-domain** (💡 light, 🔌 switch, 🌀 fan, 🎬 media_player, 🪟 cover, 🔒 lock, 🌡 climate)
- ON/OFF dot indicator + `StatusPill` ON/OFF + Odepnout button
- Card focus = entity-status accent glow (success green pro ON, muted gray pro OFF)
- **Quick scene/script tester** v ZhCard se 2 PsSecondaryButton (script.movie_mode / scene.evening)
- **EmptyState** když žádné připnuté entity

## ⏰ Budík

- **Wake-up cards** s 28sp Bold časem ráno vlevo (110dp), name + dny + hlasitost + launch app
- **Editor** s StepBtn ± time picker (28sp Bold display)
- Days picker / Volume picker / Launch-app picker — všechny jako Pill chips (selected = primary fill)
- Footer s `PsPrimaryButton` 💾 Uložit + `PsSecondaryButton` Zrušit/Smazat
- **EmptyState** placeholder

## 📦 Tech

| | |
|---|---|
| versionCode | 7 → 8 |
| versionName | 0.1.6 → 0.1.7 |
| APK velikost | ~2.7 MB |

## Backend (sdílené v této session)

- **App Releases auto-import** propaguje `published → archived/pending` downgrade z `staged_releases.json` (3-LIVE TV trio fix)
- TV staged entries nyní landují jako `pending` — admin gate manual click Publikovat
- Promotions na published zůstávají admin-only

## Roadmap → 0.1.8

- Network / Files / Browser / Alerts / Audio / ConnectionTest / Servers — drobné polish (header/spacing)
- Accessibility / Parental → PsBigChoice cards
- Settings → PS-style ChoiceRows
