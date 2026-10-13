package com.bscompanion.app.notifications

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

/** Zwolnienie z optymalizacji baterii: systemowy dialog + awaryjne ustawienia. */
object BatteryHelper {
    fun isExempt(context: Context): Boolean {
        return try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isIgnoringBatteryOptimizations(context.packageName)
        } catch (e: SecurityException) {
            android.util.Log.w("BatteryHelper", "isExempt security", e)
            false
        } catch (e: Exception) {
            android.util.Log.w("BatteryHelper", "isExempt failed", e)
            false
        }
    }

    /** Systemowy dialog "Zezwól na używanie baterii bez ograniczeń". Tylko z akcji użytkownika. */
    fun requestExempt(activity: Activity) {
        try {
            activity.startActivity(
                Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:${activity.packageName}")
                )
            )
        } catch (e: Exception) {
            android.util.Log.w("BatteryHelper", "direct request failed, fallback to settings", e)
            try {
                activity.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            } catch (e2: Exception) {
                android.util.Log.w("BatteryHelper", "settings fallback failed", e2)
                throw e2
            }
        }
    }
}

/** Odwija ContextThemeWrapper itd. aż do Activity (albo null). */
fun Context.findActivity(): Activity? {
    var c: Context = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}
