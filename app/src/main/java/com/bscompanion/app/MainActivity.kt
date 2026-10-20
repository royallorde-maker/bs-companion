package com.bscompanion.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import com.bscompanion.app.ui.performTick
import com.bscompanion.app.ui.SplashScreen
import com.bscompanion.app.ui.theme.AppTheme
import com.bscompanion.app.ui.theme.BSCompanionTheme
import com.bscompanion.app.ui.theme.appThemeOf
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    companion object {
        const val ACTION_OPEN_PLAN = "com.bscompanion.app.action.OPEN_PLAN"
        const val ACTION_OPEN_MEALS = "com.bscompanion.app.action.OPEN_MEALS"
    }

    // Zakładka żądana skrótem launchera (null = zwykły start).
    private val tabRequest = mutableStateOf<Int?>(null)

    private fun tabForIntent(intent: android.content.Intent?): Int? = when (intent?.action) {
        ACTION_OPEN_PLAN -> 1
        ACTION_OPEN_MEALS -> 0
        else -> null
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        tabRequest.value = tabForIntent(intent)
    }

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
        // Wczesne bary wg zapisanego motywu — bez flesza złych ikon przed kompozycją.
        try {
            val early = appThemeOf(Prefs.appTheme(this))
            enableEdgeToEdge(
                statusBarStyle = if (early.darkBars) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                navigationBarStyle = if (early.darkBars) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
            )
        } catch (_: Exception) { }
        NotificationHelper.ensureChannel(this)
        NotificationHelper.ensureSilentChannel(this)
        // Asynchronicznie: pełne przeplanowanie to setki IPC, nie wolno na UI.
        // Debounced w schedulerze — potrójne wołanie (onCreate/onResume/callback) jest tanie.
        EventScheduler.rescheduleAsync(this)
        enqueueSafetyNet()
        // Tylko zgoda na powiadomienia przy starcie. Baterii nie proponujemy
        // wcale (brak UI) — auto-prompt łamał Play policy i odpalał dwa dialogi
        // systemowe pod rząd; działa inexact fallback.
        requestNotifPermissionIfNeeded()

        // Skrót launchera przy zimnym starcie; akcję kasujemy, żeby rotacja
        // nie odtwarzała żądania w kółko.
        tabRequest.value = tabForIntent(intent)
        try { intent.action = null } catch (_: Exception) { }

        val activity = this
        setContent {
            val haptics = LocalHapticFeedback.current
            // Motyw żyje TU — wybór w Ustawieniach przebudowuje apkę natychmiast.
            var appTheme by rememberSaveable { mutableStateOf(Prefs.appTheme(this@MainActivity)) }
            val theme = appThemeOf(appTheme)
            LaunchedEffect(theme) {
                activity.enableEdgeToEdge(
                    statusBarStyle = if (theme.darkBars) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                    navigationBarStyle = if (theme.darkBars) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                )
                try {
                    activity.window.setBackgroundDrawable(
                        android.graphics.drawable.ColorDrawable(theme.windowBg.toInt())
                    )
                } catch (e: Exception) {
                    android.util.Log.w("MainActivity", "window bg failed", e)
                }
            }
            BSCompanionTheme(theme = theme) {
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
                // Motyw zmienia się W MIEJSCU — bez przebudowy BsApp, więc wybrana
                // zakładka i rozwinięta lista motywów zostają tam, gdzie były.
                // Wcześniejszy AnimatedContent(appTheme) tworzył nowy BsApp od zera
                // i po dotknięciu motywu wyrzucał z Ustawień do Dziś.
                Crossfade(targetState = showSplash, animationSpec = tween(200), label = "splash") { splash ->
                    if (splash) SplashScreen() else BsApp(
                        tabRequest = tabRequest.value,
                        onTabRequestConsumed = { tabRequest.value = null },
                        appTheme = appTheme,
                        onThemeChange = {
                            Prefs.setAppTheme(this@MainActivity, it)
                            appTheme = it
                            haptics.performTick()
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
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) return
            // Jednorazowa migracja prefs (blocking commit) + odczyt flagi — poza UI thread.
            // Reszta czyta SharedPreferences z pamięci, więc jest tania.
            Thread {
                val asked = try { Prefs.isNotifAsked(this) } catch (_: Exception) { false }
                // Pytamy tylko raz — przy trwałej odmowie wracamy przez wiersz w ustawieniach.
                if (!asked) {
                    runOnUiThread {
                        try {
                            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } catch (e: Exception) {
                            android.util.Log.w("MainActivity", "permission launch failed", e)
                        }
                    }
                }
            }.start()
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
