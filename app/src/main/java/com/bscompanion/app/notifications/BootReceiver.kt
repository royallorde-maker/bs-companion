package com.bscompanion.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Przeplanowanie po: reboocie, aktualizacji apki, zmianie czasu/strefy,
// zmianie zgody na dokładne alarmy. Force-stop i reboot przed odblokowaniem
// NIE odpalą tego odbiornika — apka dopina wszystko przy kolejnym starcie.
class BootReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_EXACT_ALARM_STATE =
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val a = intent.action ?: return
        // MY_PACKAGE_REPLACED jest protected broadcast — nada może TYLKO system, a
        // systemowy intent nie niesie ŻADNEGO URI. Dawniej wymagaliśmy package: —
        // to zabijało reschedulowanie po aktualizacji apki. Gdyby URI jednak
        // przyszedł, odrzucaj tylko obcy pakiet (obronnie).
        if (a == "android.intent.action.MY_PACKAGE_REPLACED") {
            val pkg = intent.data?.schemeSpecificPart
            if (pkg != null && pkg != context.packageName) return
        }
        // exported=true wymagane dla BOOT_COMPLETED; pozostałe akcje jawne od obcej
        // apki kończą się co najwyżej na zdedubowanym, idempotentnym reschedule.
        if (a == Intent.ACTION_BOOT_COMPLETED ||
            a == "android.intent.action.QUICKBOOT_POWERON" ||
            a == "android.intent.action.MY_PACKAGE_REPLACED" ||
            // Uwaga: ACTION_TIME_CHANGED ma wartość "android.intent.action.TIME_SET"
            // (historyczna nazwa) — obie strony opisują ten sam broadcast.
            a == Intent.ACTION_TIME_CHANGED ||
            a == Intent.ACTION_TIMEZONE_CHANGED ||
            a == ACTION_EXACT_ALARM_STATE
        ) {
            // Lekkie: show jest szybkie, reschedule idzie na debounced executor.
            EventScheduler.rescheduleAsync(context.applicationContext)
        }
    }
}
