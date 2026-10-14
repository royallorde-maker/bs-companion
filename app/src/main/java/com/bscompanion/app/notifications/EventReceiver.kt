package com.bscompanion.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Odpala o czasie alarmu: pokazuje powiadomienie, odświeża trwałe i PRZESUWA
// kroczące okno harmonogramu (bez tego alarmy kończyłyby się po ~2 dniach).
class EventReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_TEXT = "text"
        const val EXTRA_IS_REMINDER = "is_reminder"
        const val EXTRA_OFFSET_MIN = "offset_min"
        const val EXTRA_ID = "nid"
        // Stabilny klucz wystąpienia (data/eventId) — jeden slot powiadomienia.
        const val EXTRA_EVENT_KEY = "event_key"
        // Czas planowanego odpalenia (ms) — odrzucamy dostawy przeterminowane (power-off).
        const val EXTRA_TRIGGER_MS = "trigger_ms"
        // Poza zakresem slotów powiadomień (100k..100,1M) i z dala od 1001/9999.
        const val FALLBACK_ID = 50_001

        // Okno ignorowania powtórzonej dostawy tego samego alarmu
        // (zdublowany PendingIntent sprzed aktualizacji, podwójna dostawa OEM).
        // Alarm jest jednorazowy, więc ta sama tożsamość nigdy legalnie nie wraca.
        const val DEDUPE_WINDOW_MS = 10 * 60_000L
        private const val FIRED_FILE = "bs_fired"
        private const val FIRED_PREFIX = "fire_"
        private const val FIRED_PRUNE_THRESHOLD = 300

        /** Czysta funkcja: czy dostawa to duplikat (testowalna bez Androida). */
        fun isDuplicateDelivery(lastFiredMs: Long, nowMs: Long, windowMs: Long = DEDUPE_WINDOW_MS): Boolean {
            if (lastFiredMs <= 0L) return false
            if (nowMs < lastFiredMs) return false // cofnięty zegar — nie tłumimy
            return nowMs - lastFiredMs < windowMs
        }

        /** Klucz wystąpienia z data URI (kompatybilność ze starymi alarmami bez EXTRA_EVENT_KEY). */
        fun eventKeyFromDataString(dataString: String?): String? {
            if (dataString == null) return null
            return try {
                // Format: bs://event/<date>/<eventId>/<offset>/<isReminder>
                val parts = dataString.substringAfter("bs://event/").split("/")
                if (parts.size < 4) return null
                java.time.LocalDate.parse(parts[0]) // walidacja formatu daty
                if (parts[1].isEmpty()) return null
                "${parts[0]}/${parts[1]}"
            } catch (_: Exception) {
                null
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != EventScheduler.ACTION_EVENT) return
        val app = context.applicationContext
        if (!Prefs.isEnabled(context)) {
            try { NotificationHelper.cancelPersistent(app) } catch (e: Exception) {
                android.util.Log.w("EventReceiver", "cancel persistent failed", e)
            }
            return
        }
        if (!NotificationHelper.canPost(context)) {
            // Cofnięta zgoda: nie budź urządzenia codziennie na darmo — wyczyść
            // trwałe i dopnij puste okno (czyści zapisane URI).
            try { NotificationHelper.cancelPersistent(app) } catch (e: Exception) {
                android.util.Log.w("EventReceiver", "cancel persistent failed", e)
            }
            EventScheduler.rescheduleAsync(app)
            return
        }
        // Brak EXTRA_ID = alarm sprzed aktualizacji: nie gubimy, tylko fallback-slot.
        val hasId = intent.hasExtra(EXTRA_ID)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val text = intent.getStringExtra(EXTRA_TEXT) ?: ""
        val isReminder = intent.getBooleanExtra(EXTRA_IS_REMINDER, false)
        val offsetMin = intent.getIntExtra(EXTRA_OFFSET_MIN, 0)
        val nid = intent.getIntExtra(EXTRA_ID, FALLBACK_ID)
        if (!hasId) {
            android.util.Log.w("EventReceiver", "legacy alarm without extras, fallback slot")
        }
        val nowMs = System.currentTimeMillis()
        // Przerminowane RTC_WAKEUP po wyłączeniu urządzenia: dostarczone przy boocie,
        // zanim BootReceiver zdąży anulować. Odrzucamy (>15 min po planowanym czasie).
        val triggerMs = try { intent.getLongExtra(EXTRA_TRIGGER_MS, 0L) } catch (_: Exception) { 0L }
        if (triggerMs > 0L && nowMs > triggerMs + 15 * 60_000L) {
            android.util.Log.w("EventReceiver", "dropping stale alarm trigger=$triggerMs now=$nowMs")
            EventScheduler.rescheduleAsync(app)
            return
        }
        // Idempotentność: ten sam alarm dostarczony 2x pokazuje się RAZ.
        val alarmKey = intent.dataString ?: "nid-$nid"
        if (checkAndMarkFired(context, alarmKey, nowMs)) return
        // Stabilny slot: nawet gdyby przeszły dwie różne instancje tego samego
        // wystąpienia, druga NADPISZE wiersz zamiast dokładać identyczny.
        val eventKey = intent.getStringExtra(EXTRA_EVENT_KEY)
            ?: eventKeyFromDataString(intent.dataString)
            ?: "nid-$nid"
        val slotId = NotificationHelper.stableSlotId(eventKey)
        // Sync: przypomnienie na N minut przed kasuje się samo dokładnie o czasie wydarzenia.
        val timeout = if (isReminder && offsetMin > 0) offsetMin * 60_000L else null
        // Tekst liczony W CHWILI DZWONKA: spóźniony alarm (doza, inexact, dopięcie
        // przy otwarciu) pokazywałby "Za 30 min" na 14 min przed startem.
        val body = if (isReminder && offsetMin > 0) refreshReminderText(eventKey, text) else text
        // Szybkie (jeden notify) synchronicznie; ciężkie przeplanowanie w tle z debounce.
        NotificationHelper.showEventNotification(app, slotId, title, body, isReminder, timeout)
        try { NotificationHelper.refreshPersistent(app) } catch (e: Exception) {
            android.util.Log.w("EventReceiver", "refresh failed", e)
        }
        EventScheduler.rescheduleAsync(app)
    }

    /**
     * Przelicza "Za N min:" na faktyczną resztę do startu. Spadek z nieba:
     * alarm spóźniony (doza, inexact, dopięty przy otwarciu apki) inaczej kłamie.
     * Zwraca oryginał, gdy klucza nie da się rozpoznać.
     */
    private fun refreshReminderText(eventKey: String, fallback: String): String {
        return try {
            val sep = eventKey.indexOf('/')
            if (sep < 0) return fallback
            val date = java.time.LocalDate.parse(eventKey.substring(0, sep))
            val id = eventKey.substring(sep + 1)
            val e = com.bscompanion.app.data.DormSchedule.eventsForDate(date)
                .firstOrNull { it.id == id } ?: return fallback
            val remain = java.time.temporal.ChronoUnit.MINUTES.between(
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.of(date, e.start)
            )
            val prefix = Regex("^Za \\d+ min: ")
            if (!prefix.containsMatchIn(fallback)) return fallback
            if (remain <= 0) prefix.replace(fallback, "Teraz: ")
            else prefix.replace(fallback, "Za $remain min: ")
        } catch (_: Exception) {
            fallback
        }
    }

    /**
     * Zwraca true, gdy ten alarm już odpalono w oknie DEDUPE (duplikat → pomiń).
     * Znacznik stawiamy PRZED notify: wolimy (teoretycznie) zgubić jeden dzwonek
     * przy padzie procesu w mikro-oknie niż budzić użytkownika podwójnie.
     */
    private fun checkAndMarkFired(context: Context, alarmKey: String, nowMs: Long): Boolean {
        val prefs = context.getSharedPreferences(FIRED_FILE, Context.MODE_PRIVATE)
        val prefKey = FIRED_PREFIX + alarmKey
        val last = try {
            prefs.getLong(prefKey, 0L)
        } catch (e: Exception) {
            android.util.Log.w("EventReceiver", "read fired failed", e)
            0L
        }
        if (isDuplicateDelivery(last, nowMs)) return true
        try {
            val edit = prefs.edit().putLong(prefKey, nowMs)
            // Okazjonalne sprzątanie, żeby plik nie rósł w nieskończoność.
            // Jeden snapshot all (bez TOCTOU dwóch odczytów).
            val all = try { prefs.all } catch (e: Exception) {
                android.util.Log.w("EventReceiver", "snapshot failed", e)
                emptyMap<String, Any?>()
            }
            if (all.size > FIRED_PRUNE_THRESHOLD) {
                val cutoff = nowMs - 24 * 60 * 60_000L
                for ((k, v) in all) {
                    if (k.startsWith(FIRED_PREFIX) && v is Long && v < cutoff) edit.remove(k)
                }
            }
            edit.apply()
        } catch (e: Exception) {
            android.util.Log.w("EventReceiver", "mark fired failed", e)
        }
        return false
    }
}
