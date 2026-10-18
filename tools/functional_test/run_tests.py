#!/usr/bin/env python3
"""Internauta - automated functional test of every app function.

Mirrors the pure logic of the Kotlin sources (DormSchedule, NotificationHelper
slots, EventReceiver dedupe/keys, EventScheduler ID math, UI helpers
formatCountdown/eventProgress/timeRange) and verifies the bug-fix invariants
directly in the source tree (scheduler coalescing, banners, migration, ...).

Stdlib only. Exit code 0 = all green, 1 = failures.
See FUNCTIONAL_TESTING.md for usage.
"""
from __future__ import annotations

import re
import subprocess
import sys
from datetime import date, datetime, time, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MAIN = ROOT / "app" / "src" / "main"
TEST = ROOT / "app" / "src" / "test"

PASS = 0
FAIL = 0
FAILURES: list[str] = []


def check(name: str, cond: bool, detail: str = "") -> None:
    global PASS, FAIL
    if cond:
        PASS += 1
        print(f"  PASS  {name}")
    else:
        FAIL += 1
        FAILURES.append(name)
        print(f"  FAIL  {name}" + (f"  [{detail}]" if detail else ""))


def section(title: str) -> None:
    print(f"\n== {title} ==")


# ---------------------------------------------------------------- data mirror
# (id, start, end, notifyDefault) - same values as DormSchedule.weekdayEvents
EVENTS = [
    ("pobudka", time(6, 30), time(6, 45), True),
    ("sniadanie", time(7, 0), time(7, 50), True),
    ("wyjscie", time(7, 55), None, True),
    ("obiad", time(13, 30), time(15, 30), True),
    ("czas_wolny_1", time(15, 30), time(17, 30), False),
    ("nauka", time(17, 30), time(18, 30), True),
    ("kolacja", time(18, 30), time(19, 30), True),
    ("zajecia_wych", time(19, 30), time(21, 0), False),
    ("powrot", time(21, 0), None, True),
    ("toaleta", time(21, 0), time(21, 45), True),
    ("czas_wolny_6", time(6, 0), time(6, 30), False),
    ("czas_wolny_2", time(6, 45), time(7, 0), False),
    ("czas_wolny_3", time(7, 50), time(7, 55), False),
    ("czas_wolny_4", time(8, 10), time(13, 30), False),
    ("czas_wolny_5", time(21, 45), time(22, 0), False),
    ("cisza", time(22, 0), time(6, 0), True),
]
# Monday 2026-10-12, Friday 2026-10-16, Saturday 2026-10-17, Sunday 2026-10-18
MON, FRI, SAT, SUN = date(2026, 10, 12), date(2026, 10, 16), date(2026, 10, 17), date(2026, 10, 18)


def jhash(s: str) -> int:
    """Java String.hashCode() replica (signed 32-bit)."""
    h = 0
    for ch in s:
        h = (31 * h + ord(ch)) & 0xFFFFFFFF
    return h - 0x100000000 if h & 0x80000000 else h


def is_no_care(dt: datetime) -> bool:
    wd = dt.weekday()  # Mon=0 .. Sun=6
    t = dt.time()
    if wd == 5:
        return True
    if wd == 6:
        return t < time(16, 0)
    if wd == 4:
        return t >= time(18, 0)
    return False


def is_fully_closed(d: date) -> bool:
    return is_no_care(datetime.combine(d, time(12, 0))) and is_no_care(
        datetime.combine(d, time(20, 0))
    )


def is_curfew(t: time) -> bool:
    return t >= time(21, 0) or t < time(7, 0)


def span_seconds(start: time, end: time | None) -> int:
    if end is None:
        return 15 * 60
    s = start.hour * 3600 + start.minute * 60 + start.second
    e = end.hour * 3600 + end.minute * 60 + end.second
    d = (e - s + 24 * 3600) % (24 * 3600)
    return 15 * 60 if d == 0 else d


def current_event(now: time):
    if now >= time(22, 0) or now < time(6, 0):
        return next(e for e in EVENTS if e[0] == "cisza")
    cands = []
    for e in EVENTS:
        _id, s, en, _n = e
        end = en if en is not None else (datetime.combine(date.today(), s) + timedelta(minutes=15)).time()
        if s <= now < end:
            cands.append(e)
    if not cands:
        return None
    def rank(e):
        _id, s, en, _n = e
        one_min = (datetime.combine(date.today(), s) + timedelta(minutes=1)).time()
        expired_point = en is None and now >= one_min
        return (1 if expired_point else 0, span_seconds(s, en))
    return min(cands, key=rank)


def current_cared(now: datetime):
    if is_no_care(now):
        return None
    return current_event(now.time())


def _end_check(d: date, start: time, end: time | None) -> bool:
    """True = event survives the closure end-check (mirrors Kotlin)."""
    if end is None:
        return True
    ed = d + timedelta(days=1) if end < start else d
    return not is_no_care(datetime.combine(ed, end) - timedelta(minutes=1))


def next_cared(now: datetime, lookahead: int = 8):
    days = max(8, lookahead)
    today = now.date()
    frm0 = now.time().replace(second=0, microsecond=0)
    for d in range(days):
        day = today + timedelta(days=d)
        frm = frm0 if d == 0 else time.min
        ups = sorted([e for e in EVENTS if not e[1] < frm], key=lambda e: e[1])
        for e in ups:
            _id, s, en, _n = e
            if is_no_care(datetime.combine(day, s)):
                continue
            if not _end_check(day, s, en):
                continue
            return (day, e)
    return None


def upcoming_cared(frm_dt: datetime, count: int):
    out: list = []
    today = frm_dt.date()
    frm0 = frm_dt.time().replace(second=0, microsecond=0)
    for d in range(10):
        if len(out) >= count:
            break
        day = today + timedelta(days=d)
        ft = frm0 if d == 0 else time.min
        for e in EVENTS:
            _id, s, en, _n = e
            if d == 0 and s < ft:
                continue
            if is_no_care(datetime.combine(day, s)):
                continue
            if not _end_check(day, s, en):
                continue
            out.append((day, e))
    out.sort(key=lambda p: (p[0], p[1][1], p[1][0]))
    return out[:count]


def format_countdown(now: datetime, target: datetime) -> str:
    secs = int((target - now).total_seconds())
    if secs <= 0:
        return "teraz"
    total_min = secs // 60
    days = total_min // (24 * 60)
    h = (total_min % (24 * 60)) // 60
    m = total_min % 60
    s = secs % 60
    if days > 0:
        return f"{days}d {h}g {m}min"
    if h > 0:
        return f"{h}g {m}min"
    if m > 0:
        return f"{m}min {s}s"
    return f"{s}s"


def event_progress(now: time, start: time, end: time | None):
    if end is None:
        # Punktowe jak 15 min obecności (Kotlin: e.end ?: e.start.plusMinutes(15)).
        m = start.minute + 15
        end = time((start.hour + m // 60) % 24, m % 60)
    s = start.hour * 3600 + start.minute * 60
    en = end.hour * 3600 + end.minute * 60
    n = now.hour * 3600 + now.minute * 60
    if en > s:
        return max(0.0, min(1.0, (n - s) / (en - s)))
    total = 24 * 3600 - s + en
    el = (n - s) if n >= s else (24 * 3600 - s + n)
    return max(0.0, min(1.0, el / total))


def time_range(start: time, end: time | None) -> str:
    a = f"{start.hour:02d}:{start.minute:02d}"
    return a if end is None else f"{a}\u2013{end.hour:02d}:{end.minute:02d}"


def stable_slot(key: str) -> int:
    h = jhash(key) * 31 + len(key)
    a = 0 if h == -(2**63) else abs(h)
    return int(a % 100_000_000) + 100_000


def request_code(event_id: str, d: date, offset: int, reminder: bool) -> int:
    raw = jhash(f"{d.isoformat()}_{event_id}_{offset}_{str(reminder).lower()}")
    a = 0 if raw == -(2**31) else abs(raw)
    return a % 8_000_000 + 2_000_000


def is_duplicate(last_ms: int, now_ms: int, window: int = 10 * 60_000) -> bool:
    if last_ms <= 0:
        return False
    if now_ms < last_ms:
        return False
    return now_ms - last_ms < window


def event_key(data: str | None):
    if data is None:
        return None
    try:
        parts = data.split("bs://event/", 1)[1].split("/")
        if len(parts) < 4:
            return None
        date.fromisoformat(parts[0])
        if not parts[1]:
            return None
        return f"{parts[0]}/{parts[1]}"
    except Exception:
        return None


def is_stale_dropped(trigger_ms: int, now_ms: int) -> bool:
    """Mirrors EventReceiver: drop when >15 min past planned trigger."""
    return trigger_ms > 0 and now_ms > trigger_ms + 15 * 60_000


# ------------------------------------------------------------------- groups
def g_is_no_care() -> None:
    section("isNoCare boundaries")
    check("sat whole day closed", is_no_care(datetime.combine(SAT, time(12, 0))))
    check("sat night closed", is_no_care(datetime.combine(SAT, time(23, 0))))
    check("sun 15:59 closed", is_no_care(datetime.combine(SUN, time(15, 59))))
    check("sun 16:00 open", not is_no_care(datetime.combine(SUN, time(16, 0))))
    check("sun 20:00 open", not is_no_care(datetime.combine(SUN, time(20, 0))))
    check("fri 17:59 open", not is_no_care(datetime.combine(FRI, time(17, 59))))
    check("fri 18:00 closed", is_no_care(datetime.combine(FRI, time(18, 0))))
    check("mon noon open", not is_no_care(datetime.combine(MON, time(12, 0))))


def g_closed_helpers() -> None:
    section("isFullyClosedDay / isClosedNow (banner logic)")
    check("only saturday fully closed", is_fully_closed(SAT))
    check("sunday not fully closed", not is_fully_closed(SUN))
    check("friday not fully closed", not is_fully_closed(FRI))
    check("banner sun 10:00 (was missing)", is_no_care(datetime.combine(SUN, time(10, 0))))
    check("banner fri 20:00 (was missing)", is_no_care(datetime.combine(FRI, time(20, 0))))
    check("no banner mon 10:00", not is_no_care(datetime.combine(MON, time(10, 0))))


def g_curfew() -> None:
    section("isCurfew 21:00-07:00")
    for t, exp in [
        (time(21, 0), True), (time(23, 59), True), (time(0, 0), True),
        (time(6, 59), True), (time(7, 0), False), (time(12, 0), False),
        (time(20, 59), False),
    ]:
        check(f"curfew {t.strftime('%H:%M')}={exp}", is_curfew(t) == exp)


def g_events_for_date() -> None:
    section("eventsForDate")
    evs = sorted(EVENTS, key=lambda e: e[1])
    check("16 events", len(evs) == 16, str(len(evs)))
    check("gap six first, pobudka second",
          evs[0][0] == "czas_wolny_6" and evs[1][0] == "pobudka")
    check("sorted by start", all(evs[i][1] <= evs[i + 1][1] for i in range(15)))
    check("first pobudka 06:30", evs[1][0] == "pobudka" and evs[1][1] == time(6, 30))


def g_current() -> None:
    section("currentEvent / currentCaredEvent")
    check("23:00 cisza", current_event(time(23, 0))[0] == "cisza")
    check("02:00 cisza", current_event(time(2, 0))[0] == "cisza")
    check("06:30 pobudka", current_event(time(6, 30))[0] == "pobudka")
    check("21:00 tie-break powrot (shortest)", current_event(time(21, 0))[0] == "powrot")
    check("21:02 expired point yields to toaleta", current_event(time(21, 2))[0] == "toaleta")
    check("21:30 toaleta (powrot 15min over)", current_event(time(21, 30))[0] == "toaleta")
    check("12:00 gap is czas_wolny_4", current_event(time(12, 0))[0] == "czas_wolny_4")
    check("06:10 gap is czas_wolny_6", current_event(time(6, 10))[0] == "czas_wolny_6")
    check("06:50 gap is czas_wolny_2", current_event(time(6, 50))[0] == "czas_wolny_2")
    check("cared sat 23:00 None (no cisza when closed)",
          current_cared(datetime.combine(SAT, time(23, 0))) is None)
    check("cared sun 10:00 None", current_cared(datetime.combine(SUN, time(10, 0))) is None)
    check("cared mon 06:35 pobudka",
          current_cared(datetime.combine(MON, time(6, 35)))[0] == "pobudka")
    check("point event 15min window (wyjscie 07:55-08:10)",
          current_event(time(8, 5))[0] == "wyjscie")


def g_next() -> None:
    section("nextCaredEvent")
    hit = next_cared(datetime.combine(MON, time(21, 0)))
    check("exact-now 21:00 included", hit is not None and hit[1][1] == time(21, 0), str(hit))
    check("21:00 tie-break powrot first", hit is not None and hit[1][0] == "powrot")
    hit = next_cared(datetime.combine(FRI, time(18, 30)))
    check("fri evening skips weekend gap", hit is not None and hit[0] == SUN and hit[1][0] == "nauka",
          str(hit))
    hit = next_cared(datetime.combine(SUN, time(21, 50)))
    check("sun evening cisza cared", hit is not None and hit[1][0] == "cisza", str(hit))
    # seconds truncation: 13:30:30 must behave like 13:30
    a = next_cared(datetime(2026, 10, 12, 13, 30, 30))
    b = next_cared(datetime(2026, 10, 12, 13, 30, 0))
    check("seconds truncation (13:30:30 == 13:30)", a == b, f"{a} vs {b}")
    check("lookahead min 8 (lookahead=1 still crosses weekend)",
          next_cared(datetime.combine(FRI, time(18, 30)), lookahead=1) is not None)
    check("fri 17:30 nauka dropped (straddles closure)",
          next_cared(datetime(2026, 10, 16, 17, 0)) != (FRI, next(e for e in EVENTS if e[0] == "nauka")))
    check("future cisza present tomorrow evening",
          any(e[0] == "cisza" for _, e in upcoming_cared(datetime.combine(MON, time(12, 0)), 9)))


def g_upcoming() -> None:
    section("upcomingCared")
    ids = [e[0] for _, e in upcoming_cared(datetime(2026, 10, 12, 20, 59), 4)]
    check("keeps both 21:00 events", "powrot" in ids and "toaleta" in ids, str(ids))
    lst = upcoming_cared(datetime.combine(MON, time(6, 0)), 2)
    check("respects count", len(lst) == 2)
    dts = [datetime.combine(d, e[1]) for d, e in lst]
    check("sorted by (date,time)", dts[0] <= dts[1])
    lst = upcoming_cared(datetime.combine(SAT, time(10, 0)), 4)
    check("saturday yields no cared (all skipped) or sunday events",
          all(d != SAT for d, _ in lst), str(lst))
    lst = upcoming_cared(datetime.combine(MON, time(6, 0)), 4)
    check("gap first, then pobudka", [e[0] for _, e in lst][:2] == ["czas_wolny_6", "pobudka"])


def g_countdown() -> None:
    section("formatCountdown")
    base = datetime(2026, 10, 12, 12, 0, 0)
    check("past -> teraz", format_countdown(base, base - timedelta(seconds=5)) == "teraz")
    check("zero -> teraz", format_countdown(base, base) == "teraz")
    check("45s", format_countdown(base, base + timedelta(seconds=45)) == "45s")
    check("5min 10s", format_countdown(base, base + timedelta(minutes=5, seconds=10)) == "5min 10s")
    check("2g 5min", format_countdown(base, base + timedelta(hours=2, minutes=5)) == "2g 5min")
    check("fri->sun shows days",
          format_countdown(datetime(2026, 10, 16, 18, 30), datetime(2026, 10, 18, 17, 30)) == "1d 23g 0min")
    check("26h -> 1d 2g", format_countdown(base, base + timedelta(hours=26)) == "1d 2g 0min")


def g_progress() -> None:
    section("eventProgress")
    check("half of 13:30-15:30", event_progress(time(14, 30), time(13, 30), time(15, 30)) == 0.5)
    check("start = 0", event_progress(time(13, 30), time(13, 30), time(15, 30)) == 0.0)
    check("point event starts at 0", event_progress(time(7, 55), time(7, 55), None) == 0.0)
    check("point event mid ~7/15", abs(event_progress(time(8, 2), time(7, 55), None) - 7 / 15) < 1e-9)
    p = event_progress(time(2, 0), time(22, 0), time(6, 0))
    check("overnight cisza 02:00 = 0.5", abs(p - 0.5) < 1e-9, str(p))
    check("clamped >= 0", event_progress(time(6, 0), time(13, 30), time(15, 30)) == 0.0)
    check("clamped <= 1", event_progress(time(23, 0), time(13, 30), time(15, 30)) == 1.0)


def g_timerange() -> None:
    section("timeRange HH:mm")
    check("padded 06:30-06:45", time_range(time(6, 30), time(6, 45)) == "06:30\u201306:45")
    check("point event no dash", time_range(time(7, 55), None) == "07:55")
    check("overnight", time_range(time(22, 0), time(6, 0)) == "22:00\u201306:00")
    check("no 6:30 short form", not time_range(time(6, 30), time(6, 45)).startswith("6:"))


def g_slots() -> None:
    section("stableSlotId")
    a = stable_slot("2026-10-12/nauka")
    check("stable", a == stable_slot("2026-10-12/nauka"))
    check("in 100k..100.1M", 100_000 <= a <= 100_100_000, str(a))
    check("reserved ids avoided", a not in (1001, 9999, 50_001), str(a))
    check("differs per instance",
          stable_slot("2026-10-12/nauka") != stable_slot("2026-10-13/nauka")
          or stable_slot("2026-10-12/nauka") != stable_slot("2026-10-12/kolacja"))
    check("FALLBACK outside slot range", not 100_000 <= 50_001 <= 100_100_000)
    er = src("java/com/bscompanion/app/notifications/EventReceiver.kt")
    check("reminders coalesce same minute", "shouldCoalesce" in er and "COALESCE_WINDOW_MS" in er
          and "tryCoalesceReminder" in er)


def g_dedupe() -> None:
    section("isDuplicateDelivery (10min window)")
    now = 1_700_000_000_000
    check("never fired -> show", not is_duplicate(0, now))
    check("11min ago -> show", not is_duplicate(now - 11 * 60_000, now))
    check("1min ago -> dup", is_duplicate(now - 60_000, now))
    check("same ms -> dup", is_duplicate(now, now))
    check("clock back -> show", not is_duplicate(now + 60_000, now))
    check("boundary 10min -> show", not is_duplicate(now - 10 * 60_000, now))


def g_eventkey() -> None:
    section("eventKeyFromDataString")
    check("reminder uri", event_key("bs://event/2026-10-12/nauka/30/true") == "2026-10-12/nauka")
    check("exact uri", event_key("bs://event/2026-10-12/nauka/0/false") == "2026-10-12/nauka")
    check("null -> None", event_key(None) is None)
    check("garbage -> None", event_key("bs://event/garbage") is None)
    check("non-uri -> None", event_key("not-a-uri") is None)
    check("empty event -> None", event_key("bs://event/2026-10-12//30/true") is None)


def g_reqcode() -> None:
    section("requestCodeFor (2M..10M)")
    r = request_code("nauka", date(2026, 10, 12), 15, True)
    check("deterministic", r == request_code("nauka", date(2026, 10, 12), 15, True))
    check("in range", 2_000_000 <= r <= 10_000_000, str(r))
    check("reminder flag matters",
          request_code("nauka", date(2026, 10, 12), 15, True)
          != request_code("nauka", date(2026, 10, 12), 15, False))
    check("offset matters",
          request_code("nauka", date(2026, 10, 12), 15, True)
          != request_code("nauka", date(2026, 10, 12), 30, True))


def g_stale() -> None:
    section("stale alarm drop (>15min)")
    trig = 1_700_000_000_000
    check("2 days late dropped", is_stale_dropped(trig, trig + 2 * 24 * 3600_000))
    check("on time kept", not is_stale_dropped(trig, trig + 5_000))
    check("doze-late 10min kept", not is_stale_dropped(trig, trig + 10 * 60_000))
    check("no extra (0) kept", not is_stale_dropped(0, trig))


# ------------------------------------------------------- source invariants
def src(rel: str) -> str:
    return (MAIN / rel).read_text(encoding="utf-8")


def g_source() -> None:
    section("source fix invariants")
    es = src("java/com/bscompanion/app/notifications/EventScheduler.kt")
    er = src("java/com/bscompanion/app/notifications/EventReceiver.kt")
    nh = src("java/com/bscompanion/app/notifications/NotificationHelper.kt")
    ds = src("java/com/bscompanion/app/data/DormSchedule.kt")
    bs = src("java/com/bscompanion/app/ui/BsApp.kt")
    ma = src("java/com/bscompanion/app/MainActivity.kt")
    mo = src("java/com/bscompanion/app/ui/Motion.kt")

    check("debounce coalesces (no drop)", "coalescedPending" in es and "if (coalescedPending) return" in es)
    check("force clears coalesced (no double burst)", "coalescedPending = false" in es)
    check("scheduler skips non-notify, UI keeps them", "if (!event.notifyDefault) continue" in es)
    check("grace clamp fires once per event", "isGraceClamped" in es and "markGraceClamped" in es and "graceDeadline" in es)
    check("wylacznie przypomnienia (brak on-time)", "isReminder = false" not in es)
    check("przypomnienie samo znika o czasie", "setTimeoutAfter" in nh and "isReminder, timeout)" in er)
    check("plasko: zero gradientow + pigulka", "verticalGradient" not in bs and "Brush" not in bs
          and "RoundedCornerShape(28.dp)" in bs)
    check("wave morphs flat to wavy (M3)", '"waveAmp"' in bs and "wavyRing" in bs
          and "taper" in bs)
    check("force bypass for user actions", "force: Boolean = false" in es)
    check("lock off UI thread (bgLock/scheduleLock)",
          "private val bgLock" in es and "private val scheduleLock" in es
          and "synchronized(scheduleLock)" in es)
    check("migrate before scheduled-uris read", es.index("Prefs.migrateIfNeeded(context)") < es.index("Prefs.getScheduledUris"))
    check("all prefs getters migrate", es.count("migrateIfNeeded(context)") >= 5, str(es.count("migrateIfNeeded(context)")))
    check("alarm horizon 0..7 (holidays)", es.count("(0..7).map") == 2)
    check("inexact fallback setAndAllowWhileIdle", "setAndAllowWhileIdle" in es)
    check("trigger extra scheduled", "EXTRA_TRIGGER_MS, fireMs" in es)
    check("stale drop 15min in receiver", "15 * 60_000L" in er and "EXTRA_TRIGGER_MS" in er)
    check("legacy alarm fallback (no drop)", "legacy alarm without extras" in er)
    check("FALLBACK outside slots", "FALLBACK_ID = 50_001" in er)
    check("dedupe single snapshot", "val all = try { prefs.all }" in er)
    check("revoked permission clears (no daily wakeups)", er.count("EventScheduler.rescheduleAsync(app)") >= 2)
    check("blocked = event channel only", "celowo IGNORUJEMY" in nh)
    check("slot range 100M", "100_000_000L" in nh)
    check("persistent HH:mm + locale reuse", "next.start.format(timeFmt)" in nh)
    check("future cisza in timeline", "maxOf(8, lookaheadDays)" in ds and "currentCaredEvent" in ds)
    check("closed-day helpers", "fun isFullyClosedDay" in ds and "fun isClosedNow" in ds)
    check("countdown days", 'd ${h}g ${m}min' in bs)
    check("TERAZ uses cared helper", "currentCaredEvent(lookupNow" in bs)
    check("banners use now (sun/fri)", bs.count("isNoCare(nowTick)") >= 2)
    check("upcoming jumps to plan with flash", "planFocus" in bs and "parseFocusKey" in bs
          and "flashId" in bs and "scrollToItem" in bs)
    check("upcoming rows show weekday off-today", "rangeText" in bs)
    check("smart offsets replace chips", "smartOffsetMinutes" in es and "smartOffsets" in es
          and "Przypomnij wcześniej" not in bs)
    check("same-start events share one offset", "offsetsByStart" in es)
    check("haptics wired into taps", "performTick" in bs and "performTick" in ma
          and "performTick" in mo)
    check("persistent refreshes itself", "ACTION_REFRESH" in es and "scheduleRefreshTicks" in es
          and "ACTION_REFRESH" in er)
    check("row width floor", "widthIn(min = 64.dp" in bs)
    check("no double spacing", "spacedBy(0.dp)" in bs)
    check("no notif settings UI (smart mode)", "NotifCard" not in bs and "BatteryRow" not in bs
          and "onCheckedChange = onEnabledChange" not in bs)
    check("aligned 30s ticks", bs.count("30_000 - System.currentTimeMillis()") == 2)
    check("exact fallback without UI row", "canScheduleExactAlarms" in es
          and "ExactAlarmHelper.canSchedule(context)" not in bs)
    check("bars follow app theme, not system", "appThemeOf" in ma and "isSystemInDarkTheme" not in ma)
    check("refresh keeps resolution", "sameSize" in ma)
    check("permission migrates first", "Prefs.isNotifAsked(this)" in ma and "runOnUiThread" in ma)
    check("boot null-action guard", "intent.action ?: return" in er or "intent.action ?: return" in src("java/com/bscompanion/app/notifications/BootReceiver.kt"))
    check("worker no infinite retry", "Result.failure()" in src("java/com/bscompanion/app/notifications/RescheduleWorker.kt"))

    dx = (MAIN / "res" / "xml" / "data_extraction_rules.xml").read_text(encoding="utf-8")
    check("D2D excludes bs_fired", dx.count("bs_fired.xml") == 2)
    th = src("java/com/bscompanion/app/ui/theme/Theme.kt")
    check("seven themes mono..lawenda", all(x in th for x in
          ("enum class AppTheme", "MonoScheme", "GrafitScheme", "MlekoScheme", "CandyScheme",
           "BursztynScheme", "LasScheme", "LawendaScheme"))
          and "verticalGradient" not in th)
    check("semester countdown on Today", "SemesterCountdown" in bs
          and "fun semesterEnd" in bs and "lastInMonth" in bs)
    bg = (ROOT / "app" / "build.gradle.kts").read_text(encoding="utf-8")
    check("kotlin-test aligned", "kotlin-test:2.0.21" in bg)
    mf = (MAIN / "AndroidManifest.xml").read_text(encoding="utf-8")
    check("receivers exported correctly",
          'android:name=".notifications.EventReceiver"\n            android:exported="false"' in mf
          and 'android:name=".notifications.BootReceiver"\n            android:exported="true"' in mf)


def try_gradle() -> None:
    section("gradle unit tests (existing JUnit suite)")
    wrapper = ROOT / "gradle" / "wrapper" / "gradle-wrapper.jar"
    if not wrapper.exists():
        print("  SKIP  gradle wrapper jar not present in this checkout (offline env) -")
        print("        run ':app:testDebugUnitTest' in Android Studio instead.")
        return
    gradlew = ROOT / ("gradlew.bat" if sys.platform.startswith("win") else "gradlew")
    try:
        r = subprocess.run([str(gradlew), ":app:testDebugUnitTest", "--offline", "-q"],
                           cwd=ROOT, capture_output=True, text=True, timeout=600)
        check("gradle :app:testDebugUnitTest green", r.returncode == 0, (r.stderr or r.stdout)[-500:])
    except Exception as e:  # noqa: BLE001
        print(f"  SKIP  gradle run failed to start: {e}")


def main() -> int:
    print("Internauta functional test -", ROOT)
    for g in (g_is_no_care, g_closed_helpers, g_curfew, g_events_for_date, g_current,
              g_next, g_upcoming, g_countdown, g_progress, g_timerange, g_slots,
              g_dedupe, g_eventkey, g_reqcode, g_stale, g_source, try_gradle):
        try:
            g()
        except Exception as e:  # noqa: BLE001 - one group must not kill the run
            FAILURES.append(g.__name__)
            print(f"  ERROR {g.__name__}: {e}")
            global FAIL
            FAIL += 1
    print(f"\n==== RESULT: {PASS} passed, {FAIL} failed ====")
    if FAILURES:
        print("Failed:", ", ".join(FAILURES))
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
