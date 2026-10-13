package com.bscompanion.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bscompanion.app.notifications.EventScheduler
import com.bscompanion.app.notifications.NotificationHelper
import com.bscompanion.app.notifications.Prefs
import com.bscompanion.app.notifications.RescheduleWorker
import com.bscompanion.app.ui.BsApp
import com.bscompanion.app.ui.SplashScreen
import com.bscompanion.app.ui.theme.BSCompanionTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val notifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        try { Prefs.setNotifAsked(this) } catch (e: Exception) {
            android.util.Log.w("MainActivity", "setNotifAsked failed", e)
        }
        if (granted) {
            NotificationHelper.ensureChannel(this)
            EventScheduler.rescheduleAsync(this, force = true)
        } else {
            EventScheduler.rescheduleAsync(this, force = true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyRefreshRate()
        try {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            )
        } catch (_: Exception) { }
        NotificationHelper.ensureChannel(this)
        NotificationHelper.ensureSilentChannel(this)
        EventScheduler.rescheduleAsync(this)
        enqueueSafetyNet()
        requestNotifPermissionIfNeeded()

        val activity = this
        setContent {
            LaunchedEffect(Unit) {
                activity.enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
                    navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                )
                try {
                    activity.window.setBackgroundDrawable(
                        android.graphics.drawable.ColorDrawable(0xFF131210.toInt())
                    )
                } catch (e: Exception) {
                    android.util.Log.w("MainActivity", "window bg failed", e)
                }
            }
            BSCompanionTheme(darkTheme = true) {
                var showSplash by remember { mutableStateOf(savedInstanceState == null) }
                LaunchedEffect(Unit) {
                    if (showSplash) {
                        delay(800)
                        showSplash = false
                    }
                }
                Crossfade(targetState = showSplash, animationSpec = tween(200), label = "splash") { splash ->
                    if (splash) SplashScreen() else BsApp()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applyRefreshRate()
        EventScheduler.rescheduleAsync(this)
    }

    private fun applyRefreshRate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val disp = try { display } catch (_: Exception) { null } ?: return
                val current = try { disp.mode } catch (_: Exception) { null }
                val modes = try { disp.supportedModes?.toList() } catch (_: Exception) { null } ?: return
                if (modes.isEmpty()) return
                val sameSize = if (current != null) {
                    modes.filter { it.physicalWidth == current.physicalWidth && it.physicalHeight == current.physicalHeight }
                } else emptyList()
                val pool = sameSize.ifEmpty { listOfNotNull(current) }
                if (pool.isEmpty()) return
                val mode = pool.maxByOrNull { it.refreshRate } ?: return
                if (current != null && mode.modeId == current.modeId) return
                window.attributes = window.attributes.apply {
                    preferredDisplayModeId = mode.modeId
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "applyRefreshRate failed", e)
        }
    }

    private fun requestNotifPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try { Prefs.migrateIfNeeded(this) } catch (_: Exception) {}
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            val asked = try { Prefs.isNotifAsked(this) } catch (_: Exception) { false }
            if (!granted && !asked) {
                try {
                    notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } catch (e: Exception) {
                    android.util.Log.w("MainActivity", "permission launch failed", e)
                }
            }
        }
    }

    private fun enqueueSafetyNet() {
        try {
            val req = PeriodicWorkRequestBuilder<RescheduleWorker>(6, java.util.concurrent.TimeUnit.HOURS).build()
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "bs-reschedule", ExistingPeriodicWorkPolicy.KEEP, req
            )
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "safety net failed", e)
        }
    }
}
