package com.bscompanion.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import com.bscompanion.app.data.DormSchedule
import com.bscompanion.app.data.DormEvent
import com.bscompanion.app.data.EventCategory
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
    const val KEY_APP_THEME = "app_theme"

    fun appTheme(context: Context): String {
        return try {
            context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
                .getString(KEY_APP_THEME, "MONO") ?: "MONO"
        } catch (_: Exception) {
            "MONO"
        }
    }

    fun setAppTheme(context: Context, value: String) {
        try {
            context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
                .edit().putString(KEY_APP_THEME, value).apply()
        } catch (_: Exception) { }
    }

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
                    // Martwy klucz theme_mode pomijamy: od 1.14.0 jest tylko AMOLED.
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
    const val ACTION_REFRESH = "com.bscompanion.app.PERSISTENT_REFRESH"
    // Schemat data w PendingIntent: rozróżnia alarmy (tożsamość IGNORUJE extras!).
    private const val URI_SCHEME = "bs"
    // Ciche tyknięcia odświeżające trwałe powiadomienie na granicach wydarzeń.
    private const val REFRESH_BASE = 910_000
    const val REFRESH_SLOTS = 8
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
                // force=true: natychmiast + kasuj wiszące doschedule — inaczej
                // egzekucja spod UI i zaplanowana w tle robią to samo podwójnie.
                coalescedPending = false
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

    /**
     * Mądre wyprzedzenie przypomnienia (min): apka dobiera je sama wg wagi wydarzenia.
     * Pobudka to krótkie szturchnięcie przed porannym budzikiem, posiłki dają czas
     * na dojście do stołówki, nauka czas na powrót do pokoju, cisza dyskretne
     * 10 minut, a punkty-deadline'y (wyjście, powrót) krótko i konkretnie.
     */
    fun smartOffsetMinutes(e: DormEvent): Int = when (e.category) {
        EventCategory.WAKEUP -> 5
        EventCategory.MEAL -> 30
        EventCategory.STUDY -> 20
        EventCategory.QUIET -> 10
        EventCategory.ORG -> if (e.end == null) 15 else 20
        else -> 20
    }

    /**
     * Wspólny offset dla wydarzeń o tym samym starcie (max) — jeden dzwonek na moment.
     * Czysta funkcja (testowalna bez Androida): przyjmuje listę eventów, zwraca
     * mapę start -> offset do wspólnego użycia.
     */
    fun offsetsByStart(events: List<DormEvent>): Map<LocalTime, Int> =
        events.filter { it.notifyDefault }
            .groupBy { it.start }
            .mapValues { (_, grp) -> grp.maxOf { smartOffsetMinutes(it) } }

    /** Wszystkie możliwe wyprzedzenia — do obronnego wyliczania w cancelAll. */
    private val smartOffsets = setOf(0, 5, 10, 15, 20, 30)

    fun rescheduleAll(context: Context) {
        // Migracja + odczyty I/O POZA zamkiem (nie trzymamy monitora na dysku).
        Prefs.migrateIfNeeded(context)
        val stored = try { Prefs.getScheduledUris(context) } catch (_: Exception) { emptySet() }
        val enabled = try { Prefs.isEnabled(context) } catch (_: Exception) { true }
        val canPost = try { NotificationHelper.canPost(context) } catch (_: Exception) { false }
        // notificationsBlocked łapie też globalny wyłącznik powiadomień apki (areNotificationsEnabled),
        // nie tylko zablokowany kanał — bez tego planista stawiał budziki, które nic nie pokażą.
        val blocked = try { NotificationHelper.notificationsBlocked(context) } catch (_: Exception) { false }
        val offsets = smartOffsets.sorted()
        // Martwe znaczniki łaski (wydarzenie już minęło) nie rosną w nieskończoność.
        try { pruneGraceMarkers(context, System.currentTimeMillis()) } catch (_: Exception) { }
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
            // Bez zgody na powiadomienia albo ze zblokowanym kanałem nie stawiamy
            // alarmów, które i tak nic nie pokażą (oszczędza ~700 Binder IPC na każdym fire).
            // Dopniemy po granted / odblokowaniu w callbacku.
            if (!canPost || blocked) {
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
                // Jeden dzwonek na moment: wydarzenia o tym samym starcie dzwonią RAZEM
                // (wspólny offset = max), inaczej smart-offsety rozsuwają je o minuty
                // i łączenie "jeden dzwonek" nie łapie pary (powrót 21:00 + toaleta 21:00).
                val offsetByStart = offsetsByStart(DormSchedule.eventsForDate(date))
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
                    val offset = offsetByStart[event.start] ?: smartOffsetMinutes(event)
                    scheduleOne(context, date, event.id, event.title, event.description, event.start, offset, zone, isReminder = true)?.let { wanted += it }
                    // Celowo BRAK alarmu punktualnego: przypomnienie znika o czasie
                    // wydarzenia (timeout) i nic nie wraca. Bez re-buzzu.
                }
            }
            try { Prefs.setScheduledUris(context, wanted) } catch (e: Exception) {
                android.util.Log.w("EventScheduler", "save uris failed", e)
            }
            // Ciche tyknięcia trwałego powiadomienia — tylko gdy przypomnienia żyją.
            try { scheduleRefreshTicks(context, zone) } catch (e: Exception) {
                android.util.Log.w("EventScheduler", "refresh ticks failed", e)
            }
        }
    }

    /**
     * Ciche tyknięcia na granice wydarzeń: trwałe "Internauta czuwa" odświeża się
     * SAMO, bez otwierania apki. Inexact (oszczędza baterię — wiersz i tak
     * pokazuje następne wydarzenie, nie sekundnik). Każde tyknięcie przy okazji
     * przeplanowuje całość, więc łańcuch nie urywa się po 8 slotach.
     */
    private fun scheduleRefreshTicks(context: Context, zone: ZoneId) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nowMs = System.currentTimeMillis()
        val times = mutableListOf<Long>()
        val today = LocalDate.now()
        for (d in 0..2) {
            val date = today.plusDays(d.toLong())
            for (e in DormSchedule.eventsForDate(date)) {
                val ms = LocalDateTime.of(date, e.start).atZone(zone).toInstant().toEpochMilli()
                if (ms > nowMs) times += ms
                if (times.size >= REFRESH_SLOTS) break
            }
            if (times.size >= REFRESH_SLOTS) break
        }
        // Północ: granica dni bez wydarzeń też odświeża wiersz.
        val midnight = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        if (midnight > nowMs) times += midnight
        times.sorted().take(REFRESH_SLOTS).forEachIndexed { i, fireMs ->
            val intent = Intent(context, EventReceiver::class.java).apply {
                action = ACTION_REFRESH
                data = Uri.parse("$URI_SCHEME://refresh/$i")
                putExtra(EventReceiver.EXTRA_TRIGGER_MS, fireMs)
            }
            val pi = PendingIntent.getBroadcast(
                context, REFRESH_BASE + i, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            try {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireMs, pi)
            } catch (e: Exception) {
                android.util.Log.w("EventScheduler", "refresh tick failed", e)
            }
        }
    }

    /** Znaczniki jednorazowej łaski: alarmKey -> startMs wydarzenia. */
    private const val GRACE_FILE = "bs_fired"
    private const val GRACE_PREFIX = "grace_"
    private const val GRACE_FIRE_PREFIX = "grace_fire_"

    private fun isGraceClamped(context: Context, alarmKey: String, startMs: Long): Boolean {
        return try {
            context.getSharedPreferences(GRACE_FILE, Context.MODE_PRIVATE)
                .getLong(GRACE_PREFIX + alarmKey, -1L) == startMs
        } catch (_: Exception) {
            false
        }
    }

    private fun markGraceClamped(context: Context, alarmKey: String, startMs: Long, fireMs: Long = 0L) {
        try {
            context.getSharedPreferences(GRACE_FILE, Context.MODE_PRIVATE).edit()
                .putLong(GRACE_PREFIX + alarmKey, startMs)
                .putLong(GRACE_FIRE_PREFIX + alarmKey, fireMs).apply()
        } catch (_: Exception) { }
    }

    private fun graceDeadline(context: Context, alarmKey: String): Long {
        return try {
            context.getSharedPreferences(GRACE_FILE, Context.MODE_PRIVATE)
                .getLong(GRACE_FIRE_PREFIX + alarmKey, 0L)
        } catch (_: Exception) {
            0L
        }
    }

    private fun pruneGraceMarkers(context: Context, nowMs: Long) {
        try {
            val prefs = context.getSharedPreferences(GRACE_FILE, Context.MODE_PRIVATE)
            // Kasujemy 5 s PO terminie (nie w terminie): dzięki temu tolerancja
            // dozbrojenia w scheduleOne (deadline + 5 s) jest osiągalna, a wpisy
            // sprzed pół minuty nie zostają na zawsze.
            val stale = prefs.all
                .filter { (k, v) -> k.startsWith(GRACE_PREFIX) && v is Long && v <= nowMs - 5_000 }
                .map { it.key }
            if (stale.isNotEmpty()) {
                prefs.edit().also { ed -> stale.forEach { ed.remove(it) } }.apply()
            }
        } catch (_: Exception) { }
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
        val uri = "$URI_SCHEME://event/$date/$eventId/$offsetMinutes/$isReminder"
        // Czas przypomnienia minął o włos (wybudzenie 1 s po), a wydarzenie daleko:
        // strzel za sekundę, ale TYLKO RAZ na wystąpienie — każde kolejne
        // rescheduleAsync w tym oknie by stawiał budzik od nowa (patrz raport QA).
        val fireMs = if (millis <= nowMs) {
            if (isGraceClamped(context, uri, startMs)) {
                // Alarm łaski mógł paść w cancelAll z równoległego force-reschedule
                // (okno ~1 s): dozbrój, ale tylko póki wisi jego termin — po nim zero re-armów.
                val deadline = graceDeadline(context, uri)
                if (deadline <= 0L || nowMs > deadline + 5_000) return null
                deadline
            } else {
                val fire = nowMs + 1_000
                markGraceClamped(context, uri, startMs, fire)
                fire
            }
        } else millis

        val requestCode = requestCodeFor(eventId, date, offsetMinutes, isReminder)
        // „Dalej: …" — co konkretnego czeka po wydarzeniu. Pary o tym samym starcie
        // dostają ten sam dopisek, a receiver przy scaleniu zostawia go tylko raz.
        // dalejLine() dokleja od razu wiodący \n — nigdy nie składaj go ręcznie.
        val dalej = if (isReminder) {
            DormSchedule.nextNotifiedEvent(LocalDateTime.of(date, start).plusMinutes(1))
                ?.let { (nd, ne) -> NotificationHelper.dalejLine(date, nd, ne) }
                ?: ""
        } else ""
        val intent = Intent(context, EventReceiver::class.java).apply {
            action = ACTION_EVENT
            // Data rozróżnia PendingIntenty (tożsamość ignoruje extras).
            data = Uri.parse(uri)
            putExtra(EventReceiver.EXTRA_TITLE, title)
            putExtra(EventReceiver.EXTRA_TEXT, if (isReminder && offsetMinutes > 0) "Za $offsetMinutes min: $text$dalej" else text)
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
        // 1b) Ciche tyknięcia trwałego powiadomienia (stałe sloty).
        for (i in 0 until REFRESH_SLOTS) {
            val intent = Intent(context, EventReceiver::class.java).apply {
                action = ACTION_REFRESH
                data = Uri.parse("$URI_SCHEME://refresh/$i")
            }
            val pi = PendingIntent.getBroadcast(
                context, REFRESH_BASE + i, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            ) ?: continue
            try { am.cancel(pi) } catch (e: Exception) {
                android.util.Log.w("EventScheduler", "refresh cancel failed", e)
            }
        }
        // 2) Wyliczenie obronne (bieżące + historyczne offsety, horyzont 0..+7 jak w rescheduleAll).
        // Bez -1: wczorajsze alarmy już wygasły; ogranicza IPC o ~20%.
        // Offsety przekazane z rescheduleAll (bez odczytu prefs pod zamkiem).
        val today = LocalDate.now()
        val dates = (0..7).map { today.plusDays(it.toLong()) }
        val useOffsets = offsets ?: smartOffsets.sorted()
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
