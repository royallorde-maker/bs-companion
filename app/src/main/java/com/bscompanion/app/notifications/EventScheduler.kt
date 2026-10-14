package com.bscompanion.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import com.bscompanion.app.data.DormSchedule
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object Prefs {
    private const val FILE = "bs_settings"
    private const val CACHE_FILE = "bs_schedule_cache"
    private const val LEGACY_FILE = "bs_prefs"
    const val KEY_ENABLED = "notifications_enabled"
    const val KEY_MINUTES_BEFORE = "minutes_before"
    const val KEY_BATTERY_ASKED = "battery_asked"
    const val KEY_NOTIF_ASKED = "notif_asked"
    const val KEY_SCHEDULED = "scheduled_uris"
    const val KEY_THEME = "theme_mode"

    fun isEnabled(context: Context): Boolean {
        migrateIfNeeded(context)
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, true)
    }

    fun setEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ENABLED, value).apply()
    }

    fun minutesBefore(context: Context): Int {
        migrateIfNeeded(context)
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getInt(KEY_MINUTES_BEFORE, 15)
    }

    fun setMinutesBefore(context: Context, value: Int) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putInt(KEY_MINUTES_BEFORE, value).apply()
    }

    fun isBatteryAsked(context: Context): Boolean {
        migrateIfNeeded(context)
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getBoolean(KEY_BATTERY_ASKED, false)
    }

    fun isNotifAsked(context: Context): Boolean {
        migrateIfNeeded(context)
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getBoolean(KEY_NOTIF_ASKED, false)
    }

    fun setNotifAsked(context: Context) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_NOTIF_ASKED, true).apply()
    }

    fun setBatteryAsked(context: Context) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_BATTERY_ASKED, true).apply()
    }

    fun getScheduledUris(context: Context): Set<String> {
        migrateIfNeeded(context)
        return context.getSharedPreferences(CACHE_FILE, Context.MODE_PRIVATE)
            .getStringSet(KEY_SCHEDULED, emptySet())?.toSet() ?: emptySet()
    }

    fun setScheduledUris(context: Context, uris: Set<String>) {
        context.getSharedPreferences(CACHE_FILE, Context.MODE_PRIVATE)
            .edit().putStringSet(KEY_SCHEDULED, uris.toSet()).apply()
    }

    fun getThemeMode(context: Context): Int {
        migrateIfNeeded(context)
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getInt(KEY_THEME, 0)
    }

    fun setThemeMode(context: Context, mode: Int) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putInt(KEY_THEME, mode).apply()
    }

    @Volatile
    private var migrated = false

    /** Jednorazowa migracja bs_prefs.xml → bs_settings / bs_schedule_cache (v1.1.x). */
    fun migrateIfNeeded(context: Context) {
        if (migrated) return
        synchronized(this) {
            if (migrated) return
            try {
                val legacy = context.getSharedPreferences(LEGACY_FILE, Context.MODE_PRIVATE)
                if (legacy.all.isNotEmpty()) {
                    val dst = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
                    // Scalanie brakujących kluczy (nie tylko gdy dst puste) — częściowe
                    // nowe prefs + niepuste legacy nie gubią ani nowych, ani starych wartości.
                    val ed = dst.edit()
                    var dirty = false
                    if (!dst.contains(KEY_ENABLED) && legacy.contains(KEY_ENABLED)) {
                        ed.putBoolean(KEY_ENABLED, legacy.getBoolean(KEY_ENABLED, true)); dirty = true
                    }
                    if (!dst.contains(KEY_MINUTES_BEFORE) && legacy.contains(KEY_MINUTES_BEFORE)) {
                        ed.putInt(KEY_MINUTES_BEFORE, legacy.getInt(KEY_MINUTES_BEFORE, 15)); dirty = true
                    }
                    if (!dst.contains(KEY_BATTERY_ASKED) && legacy.contains(KEY_BATTERY_ASKED)) {
                        ed.putBoolean(KEY_BATTERY_ASKED, legacy.getBoolean(KEY_BATTERY_ASKED, false)); dirty = true
                    }
                    if (!dst.contains(KEY_NOTIF_ASKED) && legacy.contains(KEY_NOTIF_ASKED)) {
                        ed.putBoolean(KEY_NOTIF_ASKED, legacy.getBoolean(KEY_NOTIF_ASKED, false)); dirty = true
                    }
                    if (!dst.contains(KEY_THEME) && legacy.contains(KEY_THEME)) {
                        ed.putInt(KEY_THEME, legacy.getInt(KEY_THEME, 0)); dirty = true
                    }
                    if (dirty) { try { ed.commit() } catch (_: Exception) { try { ed.apply() } catch (_: Exception) {} } }
                    val uris = legacy.getStringSet(KEY_SCHEDULED, null)?.toSet()
                    if (uris != null) {
                        val cache = context.getSharedPreferences(CACHE_FILE, Context.MODE_PRIVATE)
                        if (!cache.contains(KEY_SCHEDULED)) {
                            try {
                                cache.edit().putStringSet(KEY_SCHEDULED, uris).commit()
                            } catch (_: Exception) {
                                try { cache.edit().putStringSet(KEY_SCHEDULED, uris).apply() } catch (_: Exception) {}
                            }
                        }
                    }
                    try { legacy.edit().clear().commit() } catch (_: Exception) {
                        try { legacy.edit().clear().apply() } catch (_: Exception) {}
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("Prefs", "migrateIfNeeded failed", e)
            }
            migrated = true
        }
    }
}

object EventScheduler {
    const val ACTION_EVENT = "com.bscompanion.app.EVENT_ALARM"
    // Schemat data w PendingIntent: rozróżnia alarmy (tożsamość IGNORUJE extras!).
    private const val URI_SCHEME = "bs"
    // Dwa zamki: bgLock tylko na szybką zamianę executora (nie blokuje UI),
    // scheduleLock na właściwe cancel/schedule w tle.
    private val bgLock = Any()
    private val scheduleLock = Any()
    // Jeden wątek tła: setki wywołań Binder (cancel/schedule) NIE blokują UI.
    // (Dowód z logcat: reschedule na głównym wątku = ~140 klatek Hitcha przy starcie.)
    // Executor jest procesowy (object) — NIE wolno go zamykać z Activity.onDestroy
    // (rekreacja przy rotacji zabiłaby planowanie na stałe). Lazily odtwarzany.
    @Volatile
    private var bg: java.util.concurrent.ScheduledExecutorService = newExecutor()
    @Volatile
    private var lastAsyncMs = 0L
    @Volatile
    private var coalescedPending = false

    private fun newExecutor(): java.util.concurrent.ScheduledExecutorService {
        return java.util.concurrent.Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "bs-scheduler").apply { isDaemon = true }
        }
    }

    /**
     * Wersja nieblokująca — do wołania z onCreate/onResume/callbacków UI.
     * Debounce 2 s z koalescencją: wołanie w oknie NIE jest gubione, tylko
     * doschedulowane na koniec okna. force=true omija okno (akcje użytkownika).
     */
    fun rescheduleAsync(context: Context, force: Boolean = false) {
        val app = context.applicationContext
        val now = SystemClock.uptimeMillis()
        var delayMs = 0L
        synchronized(bgLock) {
            if (bg.isShutdown || bg.isTerminated) {
                bg = newExecutor()
                coalescedPending = false
            }
            if (!force && now - lastAsyncMs < 2000) {
                if (coalescedPending) return
                coalescedPending = true
                delayMs = (lastAsyncMs + 2000) - now
            } else {
                lastAsyncMs = now
            }
        }
        if (delayMs > 0) {
            try {
                bg.schedule({
                    synchronized(bgLock) {
                        coalescedPending = false
                        lastAsyncMs = SystemClock.uptimeMillis()
                    }
                    rescheduleAll(app)
                }, delayMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            } catch (e: Exception) {
                android.util.Log.w("EventScheduler", "coalesced schedule failed", e)
                synchronized(bgLock) { coalescedPending = false }
            }
            return
        }
        val exec: java.util.concurrent.ExecutorService
        synchronized(bgLock) { exec = bg }
        try {
            exec.execute { rescheduleAll(app) }
        } catch (e: Exception) {
            android.util.Log.w("EventScheduler", "rescheduleAsync dropped", e)
        }
    }

    /**
     * Historycznie wołane z Activity.onDestroy — obecnie celowo no-op.
     * Executor żyje tak długo jak proces; zamykanie go przy rekreacji Activity
     * powodowało trwałe zatrzymanie alarmów. Zachowane dla kompatybilności.
     */
    fun shutdown() {
    }

    fun rescheduleAll(context: Context) {
        // Migracja + odczyty I/O POZA zamkiem (nie trzymamy monitora na dysku).
        Prefs.migrateIfNeeded(context)
        val stored = try { Prefs.getScheduledUris(context) } catch (_: Exception) { emptySet() }
        val enabled = try { Prefs.isEnabled(context) } catch (_: Exception) { true }
        val canPost = try { NotificationHelper.canPost(context) } catch (_: Exception) { false }
        val minutesBefore = try { Prefs.minutesBefore(context) } catch (_: Exception) { 15 }
        val offsets = (setOf(0, 5, 10, 15, 30) + minutesBefore).sorted()
        synchronized(scheduleLock) {
            cancelAll(context, stored, offsets)
            try { NotificationHelper.refreshPersistent(context) } catch (e: Exception) {
                android.util.Log.w("EventScheduler", "refreshPersistent failed", e)
            }
            if (!enabled) {
                try { Prefs.setScheduledUris(context, emptySet()) } catch (e: Exception) {
                    android.util.Log.w("EventScheduler", "clear uris failed", e)
                }
                return
            }
            // Bez zgody na powiadomienia nie stawiamy alarmów, które i tak nic nie pokażą
            // (oszczędza ~700 Binder IPC na każdym fire). Dopniemy po granted w callbacku.
            if (!canPost) {
                try { Prefs.setScheduledUris(context, emptySet()) } catch (e: Exception) {
                    android.util.Log.w("EventScheduler", "clear uris failed", e)
                }
                return
            }
            val zone = ZoneId.systemDefault()
            val wanted = mutableSetOf<String>()

            // Horyzont dziś..+7: przykrywa weekendową dziurę pt 18:00 → ndz 16:00
            // i dłuższe zamknięcia; zgodny z horyzontem UI (DormSchedule 8 dni).
            val today = LocalDate.now()
            val dates = (0..7).map { today.plusDays(it.toLong()) }
            for (date in dates) {
                for (event in DormSchedule.eventsForDate(date)) {
                    if (!event.notifyDefault) continue
                    val startDt = LocalDateTime.of(date, event.start)
                    // Precyzyjne godziny opieki: sobota / nd do 16:00 / pt od 18:00 - pomijamy.
                    // Sprawdzamy i start, i koniec (wydarzenie zahaczające o zamknięcie odpada).
                    if (DormSchedule.isNoCare(startDt)) continue
                    val end = event.end
                    if (end != null) {
                        // Koniec może wypadać następnego dnia (cisza 22:00-06:00):
                        // ndz 22:00 ma koniec pn 06:00, a nie ndz 06:00.
                        val endDate = if (end < event.start) date.plusDays(1) else date
                        if (DormSchedule.isNoCare(LocalDateTime.of(endDate, end).minusMinutes(1))) continue
                    }
                    scheduleOne(context, date, event.id, event.title, event.description, event.start, minutesBefore, zone, isReminder = true)?.let { wanted += it }
                    // Celowo BRAK alarmu punktualnego: przypomnienie znika o czasie
                    // wydarzenia (timeout) i nic nie wraca. Bez re-buzzu.
                }
            }
            try { Prefs.setScheduledUris(context, wanted) } catch (e: Exception) {
                android.util.Log.w("EventScheduler", "save uris failed", e)
            }
        }
    }

    /** Planuje jeden alarm; zwraca identyfikator URI albo null, gdy czas minął. */
    private fun scheduleOne(
        context: Context,
        date: LocalDate,
        eventId: String,
        title: String,
        text: String,
        start: LocalTime,
        offsetMinutes: Int,
        zone: ZoneId,
        isReminder: Boolean
    ): String? {
        val dateTime = LocalDateTime.of(date, start).minusMinutes(offsetMinutes.toLong())
        val millis = dateTime.atZone(zone).toInstant().toEpochMilli()
        val nowMs = System.currentTimeMillis()
        val startMs = LocalDateTime.of(date, start).atZone(zone).toInstant().toEpochMilli()
        // Wydarzenie całe w przeszłości — nic do planowania.
        if (startMs <= nowMs) return null
        // Czas przypomnienia minął o włos (wybudzenie 1 s po), a wydarzenie daleko:
        // strzel za sekundę zamiast gubić przypomnienie do następnego punktu.
        val fireMs = if (millis <= nowMs) nowMs + 1_000 else millis

        val requestCode = requestCodeFor(eventId, date, offsetMinutes, isReminder)
        val uri = "$URI_SCHEME://event/$date/$eventId/$offsetMinutes/$isReminder"
        val intent = Intent(context, EventReceiver::class.java).apply {
            action = ACTION_EVENT
            // Data rozróżnia PendingIntenty (tożsamość ignoruje extras).
            data = Uri.parse(uri)
            putExtra(EventReceiver.EXTRA_TITLE, title)
            putExtra(EventReceiver.EXTRA_TEXT, if (isReminder && offsetMinutes > 0) "Za $offsetMinutes min: $text" else text)
            // Stabilny klucz wystąpienia (data/event) — odbiornik mapuje go na jeden slot
            // powiadomienia, więc zdublowana dostawa nadpisuje zamiast stackować.
            putExtra(EventReceiver.EXTRA_EVENT_KEY, "$date/$eventId")
            putExtra(EventReceiver.EXTRA_IS_REMINDER, isReminder)
            putExtra(EventReceiver.EXTRA_OFFSET_MIN, offsetMinutes)
            putExtra(EventReceiver.EXTRA_ID, requestCode)
            // Czas planowanego odpalenia — odbiornik odrzuca dostawy przeterminowane
            // (urządzenie wyłączone przez weekend) zamiast budzić starym tytułem.
            putExtra(EventReceiver.EXTRA_TRIGGER_MS, fireMs)
        }
        val pending = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                // Świadomy fallback inexact bez 60 s okna: setAndAllowWhileIdle
                // (batched, ale ciaśniej niż setWindow 60 s). UI pokazuje
                // "Dokładne alarmy: Wyłączone". Nie używamy USE_EXACT_ALARM
                // (Play ogranicza do zegarów/kalendarzy).
                try {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireMs, pending)
                } catch (_: Exception) {
                    am.setWindow(AlarmManager.RTC_WAKEUP, fireMs, 60_000L, pending)
                }
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireMs, pending)
            }
        } catch (e: SecurityException) {
            android.util.Log.w("EventScheduler", "exact denied, inexact fallback", e)
            try {
                try {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireMs, pending)
                } catch (_: Exception) {
                    am.setWindow(AlarmManager.RTC_WAKEUP, fireMs, 60_000L, pending)
                }
            } catch (e2: Exception) {
                android.util.Log.w("EventScheduler", "fallback schedule failed", e2)
            }
        }
        return uri
    }

    // Id usuniętych pozycji (sprzątanie po starej instalacji).
    // Zostaw na stałe: kasuje zaległe alarmy z poprzednich wersji planu.
    private val legacyIds = listOf("dyzur_hol_1", "dowoz", "sprzatanie_laz")

    fun cancelAll(context: Context, extraUris: Set<String> = emptySet(), offsets: List<Int>? = null) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // 1) Dokładnie wcześniej zaplanowane (zapisane URI).
        for (uri in extraUris) {
            val rc = requestCodeFromUri(uri) ?: continue
            val intent = Intent(context, EventReceiver::class.java).apply {
                action = ACTION_EVENT
                data = Uri.parse(uri)
            }
            val pi = PendingIntent.getBroadcast(
                context, rc, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            ) ?: continue
            try { am.cancel(pi) } catch (e: Exception) {
                android.util.Log.w("EventScheduler", "cancel failed", e)
            }
        }
        // 2) Wyliczenie obronne (bieżące + historyczne offsety, horyzont 0..+7 jak w rescheduleAll).
        // Bez -1: wczorajsze alarmy już wygasły; ogranicza IPC o ~20%.
        // Offsety przekazane z rescheduleAll (bez odczytu prefs pod zamkiem).
        val today = LocalDate.now()
        val dates = (0..7).map { today.plusDays(it.toLong()) }
        val useOffsets = offsets ?: run {
            val mb = try { Prefs.minutesBefore(context) } catch (_: Exception) { 15 }
            (setOf(0, 5, 10, 15, 30) + mb).sorted()
        }
        for (date in dates) {
            val ids = DormSchedule.eventsForDate(date).map { it.id } + legacyIds
            for (eventId in ids) {
                for (offset in useOffsets) {
                    for (reminder in listOf(true, false)) {
                        val rc = requestCodeFor(eventId, date, offset, reminder)
                        val intent = Intent(context, EventReceiver::class.java).apply {
                            action = ACTION_EVENT
                            data = Uri.parse("$URI_SCHEME://event/$date/$eventId/$offset/$reminder")
                        }
                        val pi = PendingIntent.getBroadcast(
                            context, rc, intent,
                            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                        ) ?: continue
                        try { am.cancel(pi) } catch (e: Exception) {
                            android.util.Log.w("EventScheduler", "cancel failed", e)
                        }
                    }
                }
            }
        }
    }

    // Tożsamość PendingIntent to (requestCode + filterEquals(action,data,component)),
    // więc kolizja samego requestCode dla różnych data-URI NIE nadpisuje alarmu.
    // Szeroki zakres i tak trzymamy dla czytelności logów.

    private fun requestCodeFor(eventId: String, date: LocalDate, offset: Int, reminder: Boolean): Int {
        val raw = "${date}_${eventId}_${offset}_${reminder}".hashCode()
        // Szeroki zakres 2M..10M, z dala od 1001 (trwałe), 9999 (test) i 999013 (fallback).
        return (if (raw == Int.MIN_VALUE) 0 else kotlin.math.abs(raw) % 8_000_000) + 2_000_000
    }

    private fun requestCodeFromUri(uri: String): Int? {
        return try {
            // Format: bs://event/<date>/<eventId>/<offset>/<isReminder>
            val seg = Uri.parse(uri).pathSegments
            val date = LocalDate.parse(seg[0])
            val eventId = seg[1]
            val offset = seg[2].toInt()
            val reminder = seg[3].toBooleanStrict()
            requestCodeFor(eventId, date, offset, reminder)
        } catch (e: Exception) {
            android.util.Log.w("EventScheduler", "bad uri $uri", e)
            null
        }
    }
}
