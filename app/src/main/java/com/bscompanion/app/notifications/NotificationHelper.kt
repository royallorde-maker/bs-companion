package com.bscompanion.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bscompanion.app.MainActivity
import com.bscompanion.app.R
import com.bscompanion.app.data.DormEvent
import com.bscompanion.app.data.DormSchedule
import java.time.LocalDate
import java.time.LocalDateTime

object NotificationHelper {
    const val CHANNEL_ID = "bs_events"
    const val CHANNEL_NAME = "Nadchodzące wydarzenia"
    const val SILENT_CHANNEL_ID = "bs_persistent"
    // Stałe ID trwałego powiadomienia — NIE używać do zwykłych (kolizja nadpisze).
    const val PERSISTENT_ID = 1001
    private val plLocale = java.util.Locale("pl", "PL")
    private val timeFmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm")

    // Druga linia „co dalej" — WYŁĄCZNIE przez dalejLine(); receiver zdejmuje
    // ją po tym znaczniku przy łączeniu pary, żeby w scalonym wierszu była raz.
    const val DALEJ_SEP = "\nDalej: "

    /** Czy tytuł ma już w środku godzinę („Cisza nocna 22:00–6:00")? */
    private fun titleHasTime(title: String, t: java.time.LocalTime): Boolean {
        val hhmm = t.format(timeFmt)                                     // „06:00"
        val hmm = if (hhmm.startsWith("0")) hhmm.substring(1) else hhmm  // „6:00"
        return title.contains(hhmm) || title.contains(hmm)
    }

    /** Znacznik dnia: „" dziś, „ jutro" nazajutrz, inaczej skrót dnia tygodnia. */
    private fun dayTag(today: LocalDate, date: LocalDate): String = when (date) {
        today -> ""
        today.plusDays(1) -> " jutro"
        else -> " ${date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, plLocale)}"
    }

    /**
     * Druga linia „co dalej" z wiodącym \n — jeden format dla dzwonka i
     * „czuwa"; godzina i dzień doklejane tylko, gdy tytuł niesie je sam.
     */
    fun dalejLine(today: LocalDate, date: LocalDate, e: DormEvent): String {
        val stamp = if (titleHasTime(e.title, e.start)) "" else
            " •${dayTag(today, date)} ${e.start.format(timeFmt)}"
        return "$DALEJ_SEP${e.title}$stamp"
    }

    /** Dokleja drugą linię „Dalej: …" do trwałego — gdy jest co pokazać. */
    private fun withDalej(text: String, from: LocalDateTime, today: LocalDate): String {
        val next = DormSchedule.nextNotifiedEvent(from) ?: return text
        return text + dalejLine(today, next.first, next.second)
    }

    fun ensureChannel(context: Context) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val existing = try { nm.getNotificationChannel(CHANNEL_ID) } catch (_: Exception) { null }
                if (existing != null && existing.importance != NotificationManager.IMPORTANCE_NONE) return
            }
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Przypomnienia o posiłkach, nauce własnej, ciszy nocnej i innych punktach planu dnia."
            }
            nm.createNotificationChannel(channel)
        } catch (e: Exception) {
            android.util.Log.w("NotifHelper", "ensureChannel failed", e)
        }
    }

    fun canPost(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** Prawda, gdy użytkownik zablokował powiadomienia (globalnie lub kanał wydarzeń). */
    fun notificationsBlocked(context: Context): Boolean {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!nm.areNotificationsEnabled()) return true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val ch = try { nm.getNotificationChannel(CHANNEL_ID) } catch (_: Exception) { null }
                if (ch != null && ch.importance == NotificationManager.IMPORTANCE_NONE) return true
                // Cichy kanał trwały celowo IGNORUJEMY: jego blokada nie wstrzymuje
                // przypomnień z CHANNEL_ID — ostrzeżenie tylko dla kanału wydarzeń.
            }
            return false
        } catch (e: Exception) {
            android.util.Log.w("NotifHelper", "notificationsBlocked check failed", e)
            return false
        }
    }

    /** Prawda, gdy użytkownik zablokował kanał wydarzeń (IMPORTANCE_NONE). */
    fun channelBlocked(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.getNotificationChannel(CHANNEL_ID)?.importance == NotificationManager.IMPORTANCE_NONE
        } catch (_: Exception) {
            false
        }
    }

    fun openAppNotificationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.w("NotifHelper", "open settings failed", e)
        }
    }

    fun showEventNotification(
        context: Context,
        notificationId: Int,
        title: String,
        text: String,
        isReminder: Boolean,
        timeoutAfterMs: Long? = null
    ) {
        if (!canPost(context)) return
        ensureChannel(context)
        // SINGLE_TOP + CLEAR_TOP zamiast CLEAR_TASK: tap nie niszczy Activity
        // (nie odtwarza splashy ani nie zabijała planisty).
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val fullTitle = if (isReminder) "Za chwilę: $title" else title
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(fullTitle)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            // Jeden slot, jedno budzenie — nic nie wraca po timeoutcie.
            .setOnlyAlertOnce(true)
            .setContentIntent(pending)
            .apply { if (timeoutAfterMs != null && timeoutAfterMs > 0) setTimeoutAfter(timeoutAfterMs) }
            .build()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notificationId, notification)
    }

    // --- Ciche trwałe powiadomienie ("Internauta czuwa") ---

    fun ensureSilentChannel(context: Context) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val existing = try { nm.getNotificationChannel(SILENT_CHANNEL_ID) } catch (_: Exception) { null }
                if (existing != null) return
            }
            val channel = NotificationChannel(
                SILENT_CHANNEL_ID,
                "Internauta czuwa",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ciche, trwałe powiadomienie z następnym wydarzeniem. Bez dźwięku."
                setShowBadge(false)
            }
            nm.createNotificationChannel(channel)
        } catch (e: Exception) {
            android.util.Log.w("NotifHelper", "ensureSilentChannel failed", e)
        }
    }

    /** Odświeża (lub usuwa, gdy powiadomienia wyłączone) trwałe powiadomienie. */
    fun refreshPersistent(context: Context) {
        if (!Prefs.isEnabled(context) || !canPost(context)) {
            cancelPersistent(context)
            return
        }
        val now = LocalDateTime.now()
        val nowDate = now.toLocalDate()
        val line = if (DormSchedule.isNoCare(now)) {
            // Przerwa w opiece też mówi, co pierwsze po powrocie opieki.
            withDalej("Przerwa w opiece • internat czynny ndz 16:00 – pt 18:00", now, nowDate)
        } else {
            // Wydarzenie w trakcie (np. Obiad 13:30–15:30 o 14:00) to nie "Następne".
            val ongoing = DormSchedule.currentCaredEvent(
                now.truncatedTo(java.time.temporal.ChronoUnit.MINUTES),
                DormSchedule.eventsForDate(nowDate)
            )
            if (ongoing != null) {
                // Godzina końca tylko, gdy tytuł nie ma jej już w środku
                // („Cisza nocna 22:00–6:00" nie dubluje „do 06:00").
                val till = if (ongoing.end != null && !titleHasTime(ongoing.title, ongoing.end)) {
                    " do ${ongoing.end.format(timeFmt)}"
                } else ""
                // Teraz + Dalej razem: drugi wiersz mówi, co konkretnego czeka.
                withDalej("Teraz: ${ongoing.title}$till", now, nowDate)
            } else {
                val cared = DormSchedule.nextCaredEvent(now.truncatedTo(java.time.temporal.ChronoUnit.MINUTES))
                if (cared != null) {
                    val (date, next) = cared
                    "Następne: ${next.title} •${dayTag(nowDate, date)} ${next.start.format(timeFmt)}"
                } else "Plan na dziś zakończony"
            }
        }
        ensureSilentChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, SILENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Internauta czuwa")
            .setContentText(line)
            .setStyle(NotificationCompat.BigTextStyle().bigText(line))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSilent(true)
            .setShowWhen(false)
            .setOngoing(true)
            .setContentIntent(pending)
            .build()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(PERSISTENT_ID, notification)
    }

    fun cancelPersistent(context: Context) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(PERSISTENT_ID)
        } catch (e: Exception) {
            android.util.Log.w("NotifHelper", "cancelPersistent failed", e)
        }
    }

    /**
     * Stabilny slot powiadomienia dla jednego wystąpienia (klucz "data/eventId").
     * Powtórzona dostawa tego samego wystąpienia NADPISUJE jeden wiersz w odcieniu
     * zamiast stackować dwa identyczne (zakres 100k..100,1M — z dala od PERSISTENT_ID
     * 1001 i testowego 9999; requestCode'y alarmów 2M..10M to inna przestrzeń).
     * Szeroki zakres 100M → kolizja dwóch różnych wystąpień pomijalna.
     */
    fun stableSlotId(eventKey: String): Int {
        // Podwójny hash (String.hashCode + długość) zamiast gołego hashCode —
        // deterministyczny, ale mniej skupiony w 1,8M kubełkach.
        val h = eventKey.hashCode().toLong() * 31L + eventKey.length
        val a = if (h == Long.MIN_VALUE) 0L else kotlin.math.abs(h)
        return (a % 100_000_000L).toInt() + 100_000
    }
}
