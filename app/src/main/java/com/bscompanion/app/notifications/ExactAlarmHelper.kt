package com.bscompanion.app.notifications

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/** Dokładne alarmy (Android 12+): sprawdzenie zgody + ekran systemowy. */
object ExactAlarmHelper {
    fun canSchedule(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return try {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.canScheduleExactAlarms()
        } catch (e: Exception) {
            android.util.Log.w("ExactAlarm", "canSchedule failed", e)
            false
        }
    }

    /** Ekran systemowy "Alarmy i przypomnienia" dla tej apki. */
    fun openSettings(activity: Activity) {
        try {
            activity.startActivity(
                Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:${activity.packageName}")
                )
            )
        } catch (e: Exception) {
            android.util.Log.w("ExactAlarm", "exact settings failed, fallback", e)
            try {
                activity.startActivity(Intent(Settings.ACTION_SETTINGS))
            } catch (e2: Exception) {
                android.util.Log.w("ExactAlarm", "settings fallback failed", e2)
            }
        }
    }
}
