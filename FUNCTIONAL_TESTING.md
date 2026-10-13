# Functional testing — BS Companion

Automated test of **all app functions**: one program checks every piece of
scheduling / notification / UI-helper logic plus the bug-fix invariants in
the source tree.

## What gets tested

`tools/functional_test/run_tests.py` (stdlib only, ~126 checks, 0 deps):

| Group | Functions covered |
|---|---|
| `isNoCare` boundaries | Sat all-day, Sun 15:59/16:00, Fri 17:59/18:00, Mon |
| Banner logic | `isFullyClosedDay`, `isClosedNow` (Sun morning, Fri evening) |
| `isCurfew` | 21:00–07:00 edges |
| `eventsForDate` | count, sort order, first event |
| `currentEvent` / `currentCaredEvent` | overnight cisza, 21:00 tie-break, gaps, closed→null |
| `nextCaredEvent` | exact-now, Fri→Sun gap, Sun cisza, seconds truncation, min-8d lookahead, straddle drop |
| `upcomingCared` | both 21:00 events, count, sorting, Sat skip, future cisza |
| `formatCountdown` | teraz, seconds, minutes, hours, **days** (Fri→Sun) |
| `eventProgress` | halves, edges, clamping, overnight, point events |
| `timeRange` | `HH:mm` padding everywhere |
| `stableSlotId` | stability, range, reserved IDs |
| `isDuplicateDelivery` | 10-min window incl. clock-back edge |
| `eventKeyFromDataString` | valid / legacy / garbage / null |
| `requestCodeFor` | determinism, range, flag/offset sensitivity |
| stale-alarm drop | >15 min late dropped, doze-late kept |
| source invariants (~37) | debounce coalescing, `force`, split locks, migration order, 0..7 horizon, inexact fallback, trigger extra, fallback ID, single-snapshot dedupe, event-channel-only block check, 100M slots, banners on `now`, aligned ticks, theme/worker/manifest/backup rules |
| gradle suite | tries `:app:testDebugUnitTest` when the wrapper is present, else SKIP with instructions |

## How to use

Requirements: **Python 3.8+**, nothing else.

```powershell
# from the repo root
python3 tools/functional_test/run_tests.py
```

- Exit code `0` = all green, `1` = failures (names listed at the end).
- Each line prints `PASS <name>` / `FAIL <name> [detail]`; groups are headed by `== ... ==`.
- The `gradle unit tests` group auto-skips in checkouts without
  `gradle/wrapper/gradle-wrapper.jar` (e.g. this offline env).

Run the on-device/JVM suite separately in Android Studio:

```
./gradlew :app:testDebugUnitTest
```

That executes the JUnit tests under `app/src/test`
(`DormScheduleTest`, `EventDedupeTest`).

## Adding new checks

1. Pure logic: add a mirror function next to the existing mirrors and a
   `g_*()` group using `check(name, cond, detail)`, then register it in
   `main()`'s group tuple.
2. Bug-fix invariants: add a `check(...)` with a unique substring needle in
   `g_source()` (keep needles stable — prefer identifiers over prose).
3. Re-run the program; the final `RESULT` line must stay `<n> passed, 0 failed`.

## CI snippet

```yaml
- name: functional tests
  run: python3 tools/functional_test/run_tests.py
- name: unit tests
  run: ./gradlew :app:testDebugUnitTest
```

## Notes / limits

- The Python file **mirrors** the Kotlin logic (values copied from
  `DormSchedule.weekdayEvents`); if the plan changes, update both.
- Android-framework behavior (AlarmManager delivery, channels, doze) is
  covered via source invariants + JUnit, not executed here.
- `requestCodeFor` collisions are harmless by design (PendingIntent identity
  includes the data URI); the test pins determinism/range, not uniqueness.
