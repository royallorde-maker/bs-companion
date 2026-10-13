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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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

    // Zgoda na powiadomienia (Android 13+). Wynik: przy zgodzie dopinamy alarmy.
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
            // Odmowa: wyczyść wiszące alarmy, żeby nie budziły na darmo.
            EventScheduler.rescheduleAsync(this, force = true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Prośba o 120 Hz dopóki apka jest na wierzchu.
        // display (getDisplay) istnieje od API 30 — na starszych pomijamy.
        // Dokładny tryb z systemu (nie sztywne 120f), odnawiane też w onResume
        // (w onCreate okno bywa jeszcze niepodpięte i display == null).
        applyRefreshRate()
        // Wczesne bary wg zapisanego themeMode (nie systemu) — bez flesza
        // złych ikon przed pierwszą kompozycją.
        try {
            val earlyMode = Prefs.getThemeMode(this)
            val sysNight = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
            val earlyDark = when (earlyMode) {
                1 -> true
                2 -> false
                else -> sysNight
            }
            enableEdgeToEdge(
                statusBarStyle = if (earlyDark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                navigationBarStyle = if (earlyDark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
            )
        } catch (_: Exception) { }
        NotificationHelper.ensureChannel(this)
        NotificationHelper.ensureSilentChannel(this)
        // Asynchronicznie: pełne przeplanowanie to setki IPC, nie wolno na UI.
        // Debounced w schedulerze — potrójne wołanie (onCreate/onResume/callback) jest tanie.
        EventScheduler.rescheduleAsync(this)
        enqueueSafetyNet()
        // Tylko zgoda na powiadomienia przy starcie. Baterię proponujemy wyłącznie
        // z wiersza "Praca w tle" (BatteryRow) — auto-prompt łamał Play policy
        // i odpalał dwa dialogi systemowe pod rząd.
        requestNotifPermissionIfNeeded()

        val activity = this
        setContent {
            // 0 = system, 1 = ciemny, 2 = jasny. Stan TU (nie w zakładce),
            // więc wybór przebudowuje motyw natychmiast, bez restartu.
            var themeMode by rememberSaveable { mutableStateOf(Prefs.getThemeMode(this@MainActivity)) }
            val useDark = when (themeMode) {
                1 -> true
                2 -> false
                else -> isSystemInDarkTheme()
            }
            // Ikony systemowe wg efektywnego motywu apki, nie systemu.
            // Tło okna też pod motyw apki (bez flesza systemowego przy Jasny/Ciemny).
            LaunchedEffect(useDark) {
                activity.enableEdgeToEdge(
                    statusBarStyle = if (useDark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                    navigationBarStyle = if (useDark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                )
                try {
                    val bg = if (useDark) 0xFF0A1728.toInt() else 0xFFCBDDF1.toInt()
                    activity.window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(bg))
                } catch (e: Exception) {
                    android.util.Log.w("MainActivity", "window bg failed", e)
                }
            }
            BSCompanionTheme(darkTheme = useDark) {
                // Splash tylko przy zimnym starcie procesu. Celowo `remember`, nie
                // `rememberSaveable` — saveable odtwarzałby true po rotacji z bundla
                // zapisanego gdy splash jeszcze wisiał i logo grałoby od nowa.
                var showSplash by remember { mutableStateOf(savedInstanceState == null) }
                LaunchedEffect(Unit) {
                    if (showSplash) {
                        delay(800)
                        showSplash = false
                    }
                }
                Crossfade(targetState = showSplash, animationSpec = tween(200), label = "splash") { splash ->
                    if (splash) SplashScreen() else BsApp(
                        themeMode = themeMode,
                        onThemeChange = {
                            Prefs.setThemeMode(this@MainActivity, it)
                            themeMode = it
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applyRefreshRate()
        // Powrót z ustawień systemowych (zgody!) = przeliczenie alarmów od nowa.
        // Debounce 2 s w schedulerze chroni przed potrójnym stawianiem.
        EventScheduler.rescheduleAsync(this)
    }

    private fun applyRefreshRate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val disp = try { display } catch (_: Exception) { null } ?: return
                val current = try { disp.mode } catch (_: Exception) { null }
                val modes = try { disp.supportedModes?.toList() } catch (_: Exception) { null } ?: return
                if (modes.isEmpty()) return
                // Ten sam rozmiar co obecnie (nie spychamy do niskiej rozdzielczości
                // dla Hz), spośród nich najwyższy refresh. Bez zmiany, gdy już ustawiony.
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
            // Pytamy tylko raz — przy trwałej odmowie wracamy przez wiersz w ustawieniach.
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

    /** Siatka bezpieczeństwa: dopnij okno co 6 h, gdyby jakiś alarm wypadł. */
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
