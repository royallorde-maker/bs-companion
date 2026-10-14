# BS Companion

Polished Material 3 companion app for **Internat ZST Szczecinek** (ul. Koszalińska 81),
built from `Regulamin_Internatu_ZST_2025_2026`.

## Features
- **Home (Dziś):** live phone clock (date + time, 1s refresh), current event, next-event countdown.
- **Plan dnia:** full daily order from Załącznik nr 6 (pobudka → cisza nocna 22:00–6:00), current event highlighted (suppressed when dorm closed).
- **Posiłki:** meal hours + stołówka rules (Zał. nr 3).
- **Zmiany:** about + changelog (bez wyboru motywu — od 1.14.0 tylko AMOLED).
- **Notifications:** alarms via `AlarmManager` — reminder N min before + on-time for key events.
  Toggle + 5/10/15/30 min chips, test button with Toast feedback, auto-reschedule on boot / time change / app open.
  Weekends (no care: sob all day, ndz to 16:00, pt from 18:00) are skipped.
  Exact alarms when granted (`SCHEDULE_EXACT_ALARM`); otherwise inexact `setWindow` fallback (may be late) with in-app "Zezwól" row.
- **Material 3:** AMOLED mono (czerń + szarości + biel, od 1.14.0), no dynamic color, bottom navigation, cards, countdown.
- **Icon:** adaptive `@mipmap/ic_launcher` on `#2B2B2B` background, white bell `ic_notification` for status bar.

## Schedule source
Porządek dnia, posiłki, nauka własna (17:30–18:30), cisza nocna, powrót do 21:00,
toaleta do 21:45 — all from the Regulamin.

## Build
1. Open this folder in **Android Studio Ladybug+** (JDK 17).
2. Let it download Gradle 8.7 + AGP 8.5.2 + SDK 35.
3. Run on a device/emulator (API 26+).
4. Accept the notification permission to receive reminders. Battery exemption only via "Praca w tle → Zezwól".

Package: `com.bscompanion.app` • minSdk 26 • target/compile 35 • Compose BOM 2024.06.00.

## Notifications — how it works
- `EventScheduler.rescheduleAll()` plans today +3 days from `DormSchedule` (covers Fri→Sun gap).
- `EventReceiver` shows the notification (fast, sync); reschedule goes to debounced background executor. `BootReceiver` re-plans after reboot/time change (MY_PACKAGE_REPLACED verified by package).
- Prefs: `bs_settings` (`notifications_enabled`, `minutes_before`) backed up; `bs_schedule_cache` (`scheduled_uris`) excluded from backup/transfer.
- Needs `POST_NOTIFICATIONS` (Android 13+) and exact-alarm (falls back to inexact `setWindow` if denied).

## Release signing
Release tasks fail fast without `keystore.properties` (debug/tests work keyless).
Create `keystore.properties` (gitignored) with `storeFile/storePassword/keyAlias/keyPassword` to sign.
Same key forever — cert SHA-256 `0fd77ec8…`; a new key forces users to uninstall.
