package com.bscompanion.app.ui

// EKRANY APLIKACJI (Compose, Material3).
// Zakładki: 0 = Posiłki, 1 = Plan dnia, 2 = Dziś, 3 = Zmiany, 4 = Ustawienia.
// Wspólny język wizualny: BsSurface (węgiel + włosek), StatusChip, Ring.
// Animacje: Appear (wejścia), splash (MainActivity), morph cyfr (AnimatedContent), płynny Ring (Animatable).

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.togetherWith
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bscompanion.app.data.DormEvent
import com.bscompanion.app.data.DormSchedule
import com.bscompanion.app.data.EventCategory
import com.bscompanion.app.notifications.NotificationHelper
import com.bscompanion.app.ui.theme.Montserrat
import com.bscompanion.app.ui.theme.AppTheme
import com.bscompanion.app.update.UpdateState
import com.bscompanion.app.update.checkForUpdate
import com.bscompanion.app.update.downloadAndInstall
import com.bscompanion.app.update.installApkFile
import com.bscompanion.app.update.queryDownloadStatus
import com.bscompanion.app.update.cancelDownload
import com.bscompanion.app.update.DlState
import com.bscompanion.app.update.ConnectionKind
import com.bscompanion.app.update.connectionKind
import com.bscompanion.app.update.rememberIsOnline
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val plLocale = Locale("pl", "PL")
private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
private val dateFmt = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(plLocale)

// Wejścia grają przy każdym pierwszym pokazaniu danej zakładki (bez globalnego stanu).
fun categoryIcon(cat: EventCategory): ImageVector = when (cat) {
    EventCategory.WAKEUP -> Icons.Filled.Alarm
    EventCategory.MEAL -> Icons.Filled.Restaurant
    EventCategory.STUDY -> Icons.Filled.MenuBook
    EventCategory.QUIET -> Icons.Filled.Bedtime
    EventCategory.DUTY -> Icons.Filled.CheckCircle
    EventCategory.FREE -> Icons.Filled.WbSunny
    EventCategory.ORG -> Icons.Filled.Schedule
}

/** Ikona konkretnego wydarzenia — żeby wiersze nie wyglądały identycznie. */
fun eventIcon(e: DormEvent): ImageVector = when (e.id) {
    "pobudka" -> Icons.Filled.Alarm
    "sniadanie" -> Icons.Filled.FreeBreakfast
    "wyjscie" -> Icons.Filled.School
    "obiad" -> Icons.Filled.LunchDining
    "czas_wolny_1" -> Icons.Filled.Weekend
    "nauka" -> Icons.Filled.MenuBook
    "kolacja" -> Icons.Filled.Restaurant
    "zajecia_wych" -> Icons.Filled.Group
    "powrot" -> Icons.Filled.Home
    "toaleta" -> Icons.Filled.WaterDrop
    "cisza" -> Icons.Filled.Bedtime
    "czas_wolny_2" -> Icons.Filled.WbSunny
    "czas_wolny_3" -> Icons.Filled.FlashOn
    "czas_wolny_4" -> Icons.Filled.Book
    "czas_wolny_5" -> Icons.Filled.Star
    else -> categoryIcon(e.category)
}

fun formatCountdown(now: LocalDateTime, target: LocalDateTime): String {
    val d = Duration.between(now, target)
    if (d.isNegative || d.isZero) return "teraz"
    val totalMin = d.toMinutes()
    val days = totalMin / (24 * 60)
    val h = (totalMin % (24 * 60)) / 60
    val m = totalMin % 60
    val s = d.seconds % 60
    return if (days > 0) "${days}d ${h}g ${m}min"
    else if (h > 0) "${h}g ${m}min"
    else if (m > 0) "${m}min ${s}s" else "${s}s"
}

/** Ułamek realizacji wydarzenia (0..1), z obsługą przedziału przez północ. */
fun eventProgress(now: LocalTime, e: DormEvent): Float? {
    // Punktowe (end = null) traktuj jak 15 min obecności — inaczej Ring stoi w miejscu.
    val end = e.end ?: e.start.plusMinutes(15)
    val s = e.start.toSecondOfDay()
    val en = end.toSecondOfDay()
    val n = now.toSecondOfDay()
    return if (en > s) {
        ((n - s).toFloat() / (en - s)).coerceIn(0f, 1f)
    } else {
        val total = 24 * 3600 - s + en
        val el = if (n >= s) n - s else 24 * 3600 - s + n
        (el.toFloat() / total).coerceIn(0f, 1f)
    }
}

fun timeRange(e: DormEvent): String =
    e.start.format(timeFmt) + (e.end?.let { "–${it.format(timeFmt)}" } ?: "")

/** Nieklikalny chip statusu (bez pułapki dla czytników ekranu). */
@Composable
fun StatusChip(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Info, null,
                Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Znaczek w jednolitym kole — ten sam środek optyczny w każdym wierszu/karcie. */
@Composable
fun IconDot(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    boxSize: Dp = 44.dp,
    iconSize: Dp = 24.dp
) {
    Box(
        modifier
            .size(boxSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, Modifier.size(iconSize), tint = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
fun IconDotOnContainer(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    boxSize: Dp = 44.dp,
    iconSize: Dp = 24.dp
) {
    // Znaczek kontrastowy do podświetlonego wiersza (onPrimary na primary).
    Box(
        modifier
            .size(boxSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onPrimary),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, Modifier.size(iconSize), tint = MaterialTheme.colorScheme.primary)
    }
}

/** Falisty tor pierścienia jak w M3 Expressive: promień pływa sinusem wokół koła
 *  (długość fali 15 dp, liczba fal stała na obwodzie).
 *  Faza sunie w kółko o pełną długość fali — wzór wędruje, szew niewidoczny.
 *  Amplituda wygaszana na końcach (taper): okrągłe capy siadają DOKŁADNIE
 *  na torze, bez odklejonych bąbli na starcie i mecie węża. */
private fun DrawScope.wavyRing(
    radius: Float,
    waves: Int,
    ampPx: Float,
    sweepDeg: Float,
    phase: Float = 0f
): Path {
    val path = Path()
    // Gęsto próbkowane (64 na falę) + okrągłe łączenia — gładkie jak okrąg.
    val steps = maxOf(64, (waves * sweepDeg / 360f * 64).toInt())
    val cx = size.width / 2
    val cy = size.height / 2
    repeat(steps + 1) { i ->
        val t = i / steps.toFloat()
        // Okno kosinusowe na ~7% z każdej strony: końce lądują dokładnie na torze,
        // ale fala nie spłaszcza się przez ćwierć krótkiego łuku.
        val edge = minOf(t, 1f - t)
        val taper = if (edge >= 0.07f) 1f else (0.5f - 0.5f * cos(PI * edge / 0.07f)).toFloat()
        val deg = -90f + sweepDeg * t
        val rad = Math.toRadians(deg.toDouble())
        val rr = radius + ampPx * taper * sin(rad * waves - phase).toFloat()
        val x = cx + rr * cos(rad).toFloat()
        val y = cy + rr * sin(rad).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    return path
}

/** Pierścień postępu: łuk dryfu płynnie, a przy zmianie wydarzenia SKACZE.
 *  alwaysWave: fala nigdy nie gaśnie (kotwice też) — tylko karta TERAZ. */
@Composable
fun Ring(
    progress: Float?,
    modifier: Modifier = Modifier,
    alwaysWave: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val track = MaterialTheme.colorScheme.outlineVariant
    val arc = MaterialTheme.colorScheme.primary
    val anim = remember { Animatable(0f) }
    val want = progress?.coerceIn(0f, 1f) ?: 0f
    val waveOn = alwaysWave || progress != null
    var inited by remember { mutableStateOf(false) }
    LaunchedEffect(want) {
        // Pierwsze pokazanie: SNAP (bez zamiatania od zera dla małych postępów).
        // Mikro-tyki co sekundę (~0.0003 dla godzinnego eventu) — snap, bez 900ms animacji.
        // Średnie dryfy — animuj krótko. Skok wydarzenia (>0.15) — snap (SKACZE wg speca).
        if (!inited) {
            anim.snapTo(want)
            inited = true
            return@LaunchedEffect
        }
        val diff = kotlin.math.abs(want - anim.value)
        if (diff > 0.15f || diff < 0.005f) anim.snapTo(want)
        else anim.animateTo(want, tween(600))
    }
    val progressSemantics = Modifier.clearAndSetSemantics {
        if (progress != null) {
            contentDescription = "Postęp ${(want * 100).toInt()} procent"
            progressBarRangeInfo = ProgressBarRangeInfo(want, 0f..1f)
        }
    }
    Box(progressSemantics.then(modifier).size(56.dp), contentAlignment = Alignment.Center) {
        // Fala wędruje o pełną długość co ~2,4 s — ruch jak w M3 Expressive.
        // Ręczny takt 30 fps: ta wersja Compose nie ma infiniteRepeatable.
        // Zwykłe kotwice (NASTĘPNE) stoją; TERAZ z alwaysWave nigdy nie gaśnie.
        var phase by remember { mutableFloatStateOf(0f) }
        LaunchedEffect(waveOn) {
            if (!waveOn) return@LaunchedEffect
            while (true) {
                delay(33)
                phase = (phase + (2 * PI / 72).toFloat()) % (2 * PI).toFloat()
            }
        }
        // Morfowanie jak w M3 Expressive: najpierw płaski łuk, fala narasta po chwili.
        // Amplituda jak token ActiveWaveAmplitude: 40% grubości piku (1,6 dp na 4 dp).
        val fullAmp = with(LocalDensity.current) { 1.8.dp.toPx() }
        val amp by animateFloatAsState(
            if (waveOn) fullAmp else 0f,
            tween(1200), label = "waveAmp"
        )
        Canvas(Modifier.fillMaxSize().padding(3.dp)) {
            val half = size.minDimension / 2
            val wormW = 4.5.dp.toPx()
            // Promień liczony dla PEŁNEJ amplitudy: fala rośnie w miejscu (środek
            // pierścienia stoi), a grzbiety i tak mieszczą się w Canvas.
            val wormR = half - fullAmp - wormW / 2 - 0.5.dp.toPx()
            val trackR = wormR + 1.dp.toPx()
            // Tor tej samej grubości co fala (jak w M3 Expressive): pod falą znika,
            // na resztę obwodu zostaje równym, spokojnym kołem.
            val trackD = trackR * 2
            drawArc(
                track, -90f, 360f, false,
                topLeft = Offset(
                    (size.width - trackD) / 2,
                    (size.height - trackD) / 2
                ),
                size = Size(trackD, trackD),
                style = Stroke(wormW)
            )
            if (waveOn) {
                // Postęp to wycinek TEJ SAMEJ fali. Długość fali 15 dp (M3 Expressive),
                // więc liczba fal na obwodzie jest STAŁA — krótki łuk pokazuje pełne,
                // szerokie fale zamiast grudki, a fala pod nim nie gęstnieje.
                // Pełne koło rysuje tylko brak postępu (progress == null); mierzony
                // postęp rośnie od zera — start wydarzenia to pusty pierścień, nie
                // pełne koło na pierwszym procencie (błąd: 22:00 świeciło pełnym kołem).
                val sweepDeg = if (progress != null) 360f * anim.value else 360f
                if (sweepDeg > 0f) {
                    val waves = (2 * PI * wormR / 15.dp.toPx()).roundToInt().coerceAtLeast(1)
                    val worm = wavyRing(
                        radius = wormR,
                        waves = waves,
                        ampPx = amp,
                        sweepDeg = sweepDeg,
                        phase = phase
                    )
                    drawPath(
                        worm, arc,
                        style = Stroke(wormW, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }
        content()
    }
}

@Composable
fun Appear(index: Int = 0, content: @Composable () -> Unit) {
    // Saveable — powrót na zakładkę nie odtwarza wejścia. Nie używać w leniwych
    // wierszach (LazyColumn kasuje kompozycję poza viewportem i tak odtworzy).
    // Fluent: fade + lekki scale + krótki slide na JEDNEJ krzywej ease-out,
    // dłużej i gęstszym staggerem — karty płyną jedna za drugą zamiast schodkowo.
    var visible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            tween(
                FluentMotion.CardEnterMs,
                delayMillis = index * FluentMotion.CardStaggerMs,
                easing = LinearOutSlowInEasing
            )
        ) +
            scaleIn(
                tween(
                    FluentMotion.CardEnterMs,
                    delayMillis = index * FluentMotion.CardStaggerMs,
                    easing = LinearOutSlowInEasing
                ), initialScale = 0.98f
            ) +
            slideInVertically(
                tween(
                    FluentMotion.CardEnterMs,
                    delayMillis = index * FluentMotion.CardStaggerMs,
                    easing = LinearOutSlowInEasing
                )
            ) { it / 10 }
    ) { content() }
}

@Composable
fun BsApp(
    tabRequest: Int? = null,
    onTabRequestConsumed: () -> Unit = {},
    appTheme: String = "MONO",
    onThemeChange: (String) -> Unit = {}
) {
    var tab by rememberSaveable { mutableStateOf(2) }
    val haptics = LocalHapticFeedback.current
    val titles = listOf("Posiłki", "Plan dnia", "Dziś", "Zmiany", "Ustawienia")
    val icons = listOf(Icons.Filled.LunchDining, Icons.Filled.Schedule, Icons.Filled.Home, Icons.Filled.History, Icons.Filled.Settings)
    // Cel z Nadchodzących ("data|id"): Plan pokazuje ten dzień i mignie wierszem.
    var planFocus by rememberSaveable { mutableStateOf<String?>(null) }
    // Back: z reszty wraca do Dziś (2), wyjście tylko z 2.
    BackHandler(enabled = tab != 2) {
        tab = 2
    }
    // Skrót launchera: wskocz na zakładkę raz, potem wyczyść żądanie.
    LaunchedEffect(tabRequest) {
        if (tabRequest != null && tabRequest in 0..4) {
            planFocus = null
            tab = tabRequest
            onTabRequestConsumed()
        }
    }
    val saveableHolder = rememberSaveableStateHolder()
    // Subtelny stan sieci dla całej apki — kropka offline, reszta działa lokalnie.
    val online = rememberIsOnline()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            PillBar(titles, icons, tab) { planFocus = null; tab = it; haptics.performTick() }
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(Modifier.fillMaxSize().padding(padding)) {
            // Tło płaskie — bez gradientów w całej apce.
            Column(Modifier.fillMaxSize()) {
                // Offline: jedna cienka linia, bez blokowania. Ikona CloudOff 14dp.
                AnimatedVisibility(visible = !online) {
                    Row(
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.CloudOff, null,
                            Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Brak połączenia — plan działa lokalnie",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                androidx.compose.foundation.layout.Box(Modifier.weight(1f).fillMaxWidth()) {
            // Miękkie przejście między zakładkami; treść od razu WIDOCZNA (bez przezroczystego otwarcia).
            // SaveableStateProvider trzyma scroll + saveable stany zakładek.
            // Fluent: boczny slide wg kierunku + fade; wyjście szybsze niż wejście.
            AnimatedContent(
                targetState = tab,
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(
                        tween(FluentMotion.EnterMs, easing = LinearOutSlowInEasing)
                    ) { dir * it / 5 } + fadeIn(tween(FluentMotion.EnterMs))) togetherWith
                        (slideOutHorizontally(
                            tween(FluentMotion.ExitMs, easing = FastOutLinearInEasing)
                        ) { -dir * it / 5 } + fadeOut(tween(FluentMotion.ExitMs)))
                },
                label = "tabs"
            ) { t ->
                saveableHolder.SaveableStateProvider(t) {
                    when (t) {
                        0 -> MealsScreen()
                        1 -> ScheduleScreen(
                            focusKey = planFocus,
                            onFocusConsumed = { planFocus = null }
                        )
                        2 -> HomeScreen(
                            // "Zobacz wszystko" i inne wejścia do Planu zawsze z czystym
                            // stanem — inaczej zaległy planFocus otwierał zły dzień.
                            onTab = { planFocus = null; tab = it },
                            onFocusEvent = { d, e -> planFocus = "$d|${e.id}"; tab = 1 }
                        )
                        3 -> ChangelogScreen()
                        4 -> SettingsScreen(appTheme, onThemeChange)
                    }
                }
            }
                }
            }
        }
    }
}

/** Płynne szkło w duchu Liquid Glass: półprzezroczysta tafla nad treścią,
 *  włosowy obrys, poświata, nieprzezroczysta wybrana pastylka,
 *  morfująca szerokość przy zmianie zakładki. Bez gradientów. */
@Composable
fun PillBar(
    titles: List<String>,
    icons: List<ImageVector>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    // Szkło z ról schematu (nie z twardej bieli): na Mleku tafla przyciemnia,
    // na ciemnych motywach jest mleczna jak dotąd.
    val glass = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
    val edge = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.20f)
    Box(
        Modifier.fillMaxWidth()
            .background(Color.Transparent)
            // Pigułka nad paskiem gestów — bez tego tonie pod systemową nawigację.
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = glass,
            border = BorderStroke(1.dp, edge),
            modifier = Modifier.shadow(
                20.dp,
                RoundedCornerShape(28.dp),
                spotColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.10f)
            )
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                    titles.forEachIndexed { i, t ->
                        key(i) {
                        val sel = selected == i
                        // Dotyk rozświetla ikonę — szkło reaguje na palec.
                        val interaction = remember { MutableInteractionSource() }
                        val pressed by interaction.collectIsPressedAsState()
                        val iconGlow by animateFloatAsState(
                            if (pressed || sel) 1f else 0.6f,
                            label = "press"
                        )
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(22.dp))
                                .background(if (sel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .selectable(
                                    selected = sel,
                                    onClick = { onSelect(i) },
                                    role = Role.Tab,
                                    interactionSource = interaction,
                                    indication = LocalIndication.current
                                )
                                .semantics(mergeDescendants = true) { contentDescription = t }
                                .animateContentSize()
                                .fluentScale(rememberFluentPress(interaction, 0.93f))
                                .padding(horizontal = if (sel) 16.dp else 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                icons[i], contentDescription = null,
                                Modifier.size(20.dp),
                                tint = if (sel) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = iconGlow)
                            )
                            // Etykieta tylko na wybranej — cztery pełne podpisy nie mieszczą się w rzędzie.
                            if (sel) {
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    t,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        }
                    }
                }
            }
        }
}

/** Klucz fokusu z Nadchodzących: "2026-10-13|cisza". */
fun parseFocusKey(key: String): Pair<LocalDate, DormEvent>? {
    val sep = key.indexOf('|')
    if (sep < 0) return null
    return try {
        val date = LocalDate.parse(key.substring(0, sep))
        val e = DormSchedule.eventsForDate(date).firstOrNull { it.id == key.substring(sep + 1) }
        if (e == null) null else date to e
    } catch (_: Exception) {
        null
    }
}

@Composable
fun HomeScreen(
    onTab: (Int) -> Unit,
    onFocusEvent: (LocalDate, DormEvent) -> Unit
) {
    // SEKUNDNIK: tyka co 1 s, ale czytają go tylko małe poddrzewa (HeroCard/HoldingSection)
    // przez referencję State — sama HomeScreen NIE przerysowuje się co takt.
    val nowState = remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        // Wyrównanie do granicy sekund, żeby zegar nie dryfował.
        while (true) {
            delay(1000 - System.currentTimeMillis() % 1000)
            nowState.value = LocalDateTime.now()
        }
    }
    // Odśwież zegar natychmiast po powrocie z tła (po dozowaniu mógł stać).
    LifecycleResumeEffect(Unit) {
        nowState.value = LocalDateTime.now()
        onPauseOrDispose { }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Appear(0) { HeroCard(nowState) }
        }
        item {
            Appear(1) { NowNextCards(nowState) }
        }
        item {
            Appear(2) {
                HoldingSection(
                    nowState,
                    onSeeAll = { onTab(1) },
                    onFocus = onFocusEvent
                )
            }
        }
        item {
            Appear(3) { SemesterCountdown(nowState) }
        }
    }
}

/** Karta-bohater: żywy zegar w kolorze primary. */
@Composable
fun HeroCard(nowState: State<LocalDateTime>) {
    val now by nowState
    // Kwantyzacja do minuty — AnimatedContent odpala się tylko przy zmianie "HH:mm".
    val minuteKey = remember(now) { now.truncatedTo(ChronoUnit.MINUTES) }
    val timeStr = remember(minuteKey) { minuteKey.format(timeFmt) }
    val dateStr = remember(now.toLocalDate()) {
        now.toLocalDate().format(dateFmt)
    }
    val closed = DormSchedule.isNoCare(now)
    // Płaski kolor primary — bez gradientów w całej apce.
    val onHero = MaterialTheme.colorScheme.onPrimary
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(Modifier.padding(22.dp)) {
            Text(
                "Internauta",
                style = MaterialTheme.typography.labelLarge,
                color = onHero.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            // Bębenek jak w flip/slot-clock: każda cyfra roluje osobno.
            RollingText(
                text = timeStr,
                style = MaterialTheme.typography.displayLarge.copy(fontFamily = Montserrat),
                fontWeight = FontWeight.Bold,
                color = onHero,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                dateStr.replaceFirstChar { it.uppercase(plLocale) },
                style = MaterialTheme.typography.bodyMedium,
                color = onHero.copy(alpha = 0.72f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (closed) {
                Spacer(Modifier.height(8.dp))
                HeroPill("Brak opieki wychowawczej")
            }
        }
    }
}

@Composable
private fun HeroPill(text: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f),
        modifier = modifier
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

/** Dwie osobne karty TERAZ / NASTĘPNE w języku Nexora — jak dawny EventsGrid. */
@Composable
fun NowNextCards(nowState: State<LocalDateTime>) {
    val now by nowState
    // Poza godzinami opieki pokazujemy stan zamknięcia zamiast ciszy z planu.
    val closed = DormSchedule.isNoCare(now)
    val today = now.toLocalDate()
    val events = remember(today) { DormSchedule.eventsForDate(today) }
    // Lookup obcięty do minut (12:00:00.5 nie gubi eventu 12:00); licznik pełny.
    val lookupNow = remember(now) { now.truncatedTo(ChronoUnit.MINUTES) }
    val current = remember(lookupNow, events) { DormSchedule.currentCaredEvent(lookupNow, events) }
    // nextCaredEvent trzyma świeżo startujące wydarzenie przez całą minutę (obcięcie
    // do minut) — a to już wisi w TERAZ. NASTĘPNE pokazuje więc KOLEJNE wydarzenie.
    val cared = remember(lookupNow, current) {
        val first = DormSchedule.nextCaredEvent(lookupNow)
        if (first != null && current != null && first.second.id == current.id) {
            DormSchedule.nextCaredEvent(
                LocalDateTime.of(first.first, first.second.start).plusMinutes(1)
            )
        } else {
            first
        }
    }
    val next = cared?.second
    val nextDate = cared?.first
    // Obok siebie: TERAZ | NASTĘPNE w jednym wierszu, zawsze równa wysokość.
    // Wąsko: mniejszy padding + countdown titleMedium, żeby licznik się mieścił.
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val compact = maxWidth < 380.dp
        val cardPadding = if (compact) 14.dp else 18.dp
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BsSurface(Modifier.weight(1f).fillMaxHeight(), contentPadding = cardPadding) {
                Text(
                    "TERAZ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                if (closed) {
                    // Ta sama kotwica co Ring — ikony w obu kartach w jednej linii.
                    // Fala zawsze: nawet zamknięty internat ma żywy pierścień.
                    Ring(progress = null, alwaysWave = true) {
                        Icon(
                            Icons.Filled.Bedtime, null,
                            Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Internat zamknięty",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "czynny ndz 16:00 – pt 18:00",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (current != null) {
                    Ring(eventProgress(now.toLocalTime(), current)) {
                        Icon(
                            eventIcon(current), null,
                            Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        current.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    // Tytuł ciszy niesie godziny w sobie ("Cisza nocna 22:00–6:00") —
                    // osobna linia zakresu dublowałaby się pod spodem.
                    if (!current.title.any { it.isDigit() }) {
                        Text(
                            timeRange(current),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip
                        )
                    }
                } else {
                    Ring(progress = null, alwaysWave = true) {
                        Icon(
                            Icons.Filled.WbSunny, null,
                            Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Czas wolny",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Spacer(Modifier.height(16.dp))
                }
            }
            BsSurface(Modifier.weight(1f).fillMaxHeight(), contentPadding = cardPadding) {
                Text(
                    "NASTĘPNE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                if (next != null && nextDate != null) {
                    val target = LocalDateTime.of(nextDate, next.start)
                    // Dzień tylko spoza dziś ("Obiad • 13:30–15:30" albo "Obiad • sob • ...").
                    val day = if (nextDate == today) "" else nextDate.dayOfWeek.getDisplayName(
                        java.time.format.TextStyle.SHORT, plLocale
                    )
                    // Kotwica jak Ring obok + ikona z kategorii.
                    Ring(progress = null) {
                        Icon(
                            eventIcon(next), null,
                            Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    // Licznik roluje cyframi jak zegar: 45→44 rusza tylko koniec.
                    RollingText(
                        text = formatCountdown(now, target),
                        style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (day.isEmpty()) next.title else "${next.title} • $day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Jak w TERAZ: tytuł z cyframi niesie godziny w sobie.
                    if (!next.title.any { it.isDigit() }) {
                        Text(
                            timeRange(next),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip
                        )
                    }
                } else {
                    Ring(progress = null) {
                        Icon(
                            Icons.Filled.FreeBreakfast, null,
                            Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Koniec planu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

/** Wiersze Nadchodzące w języku holdingów: ikona + tytuł/godziny + start/odliczanie. */
@Composable
fun HoldingSection(
    nowState: State<LocalDateTime>,
    onSeeAll: () -> Unit,
    onFocus: (LocalDate, DormEvent) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val now by nowState
    // Bez krzykliwej zieleni — odliczanie w stonowanym kolorze tekstu.
    // Lista przeliczana co minutę, ale tekst z sekundami i tak tyka co 1 s.
    val items = remember(now.truncatedTo(ChronoUnit.MINUTES)) {
        val mile = now.truncatedTo(ChronoUnit.MINUTES)
        // Pierwszy wiersz nie dubluje karty TERAZ: świeżo startujące wydarzenie
        // trzyma się listy przez minutę po starcie — bierzemy zapas i odsiewamy.
        val running = DormSchedule.currentCaredEvent(
            mile, DormSchedule.eventsForDate(now.toLocalDate())
        )
        DormSchedule.upcomingCared(mile, 5)
            .filterNot { running != null && it.first == now.toLocalDate() && it.second.id == running.id }
            .take(4)
    }
    val today = remember(now) { now.toLocalDate() }
    BsSurface(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Nadchodzące",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                "Zobacz wszystko",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.heightIn(min = 48.dp)
                    .clickable(onClick = onSeeAll, role = Role.Button)
                    .padding(vertical = 12.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        if (items.isEmpty()) {
            Text(
                "Brak nadchodzących punktów z opieką.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items.forEach { (date, e) ->
            val target = LocalDateTime.of(date, e.start)
            // Dzień przy pozadzisiejszych (weekendowa przeskocznia): "niedz. • 17:30–18:30".
            val rangeText = if (date == today) timeRange(e) else {
                val dow = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, plLocale)
                "$dow • ${timeRange(e)}"
            }
            val rowPress = remember(e.id, date) { MutableInteractionSource() }
            Row(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        onClick = { onFocus(date, e); haptics.performTick() },
                        role = Role.Button,
                        interactionSource = rowPress,
                        indication = LocalIndication.current
                    )
                    .fluentScale(rememberFluentPress(rowPress))
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        eventIcon(e), null,
                        Modifier.size(23.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        e.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        rangeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        e.start.format(timeFmt),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip
                    )
                    Text(
                        formatCountdown(now, target),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip
                    )
                }
            }
        }
    }
}


/** Nagłówek zakładki w kolorze primary — te same napisy, nowa oprawa. */
@Composable
fun TabHero(title: String, subtitle: String = "") {
    // Płaski kolor primary — bez gradientów w całej apce.
    val onHero = MaterialTheme.colorScheme.onPrimary
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(Modifier.padding(22.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = onHero,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = onHero.copy(alpha = 0.72f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Ustawienia: wybór motywu z natychmiastowym podglądem. */
@Composable
fun SettingsScreen(
    appTheme: String,
    onThemeChange: (String) -> Unit
) {
    val versionName = com.bscompanion.app.BuildConfig.VERSION_NAME
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            TabHero("Ustawienia")
        }
        item {
            BsSurface(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconDot(Icons.Filled.MenuBook, boxSize = 52.dp, iconSize = 28.dp)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "O aplikacji",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "Internauta • wersja $versionName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Towarzysz mieszkańca Internatu ZST Szczecinek. Plan dnia, stołówka i przypomnienia o wydarzeniach — zawsze pod ręką.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text("Autor: skraba", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        item {
            BsSurface(Modifier.fillMaxWidth()) {
                Text(
                    "Motyw",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Dotknij — cała apka przebudowuje się natychmiast.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(12.dp))
                AppTheme.entries.forEach { t ->
                    val sel = appTheme == t.name
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (sel) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .selectable(
                                selected = sel,
                                onClick = { onThemeChange(t.name) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Żywa próbka: kropka w docelowych barwach motywu.
                        Box(
                            Modifier.size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when (t) {
                                        AppTheme.MONO -> Color.Black
                                        AppTheme.GRAFIT -> Color(0xFF232329)
                                        AppTheme.MLEKO -> Color(0xFFF1F1EC)
                                        AppTheme.CANDY -> Color(0xFFFFB1C8)
                                        AppTheme.BURSZTYN -> Color(0xFFFFC15C)
                                        AppTheme.LAS -> Color(0xFF9BE2A8)
                                        AppTheme.LAWENDA -> Color(0xFFD3B8FF)
                                    }
                                )
                                .border(
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            t.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                            color = if (sel) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (sel) {
                            Icon(
                                Icons.Filled.CheckCircle, null,
                                Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

/** Płaska karta w kolorach schematu — wspólny język całego UI. */
@Composable
fun BsSurface(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 22.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    // Mroźne szkło zamienione na płaską taflę — bez gradientów w całej apce.
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = scheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        // Bez spacedBy: odstępy niosą jawne Spacery (4/8/12dp) — inaczej sumowały się.
        content = {
            Column(
                Modifier.padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                content = content
            )
        }
    )
}

/** Ostatni piątek czerwca danego roku — ustawowy koniec zajęć. */
private fun lastFridayOfJune(year: Int): LocalDate =
    LocalDate.of(year, 6, 30)
        .with(java.time.temporal.TemporalAdjusters.lastInMonth(java.time.DayOfWeek.FRIDAY))

/** Koniec bieżącego roku szkolnego: tegoroczny, a po nim przyszłoroczny. */
private fun semesterEnd(today: LocalDate): LocalDate {
    val thisYear = lastFridayOfJune(today.year)
    return if (today.isAfter(thisYear)) lastFridayOfJune(today.year + 1) else thisYear
}

@Composable
fun SemesterCountdown(nowState: State<LocalDateTime>) {
    // Liczone z sekundnika Dziś: karta nie zamarza po północy ani po powrocie z tła
    // (bez odczytu stanu Compose pomijał ją w przebudowach).
    val now by nowState
    val today = now.toLocalDate()
    val text = remember(today) {
        val june = lastFridayOfJune(today.year)
        val september = LocalDate.of(today.year, 9, 1)
        if (today.isAfter(june) && today.isBefore(september)) {
            // Lipiec–sierpień: zajęcia skończone, nowy rok jeszcze nie ruszył.
            "Wakacje!"
        } else {
            val days = ChronoUnit.DAYS.between(today, semesterEnd(today))
            when {
                days > 1 -> "Do końca zajęć: $days dni"
                days == 1L -> "Do końca zajęć: 1 dzień"
                else -> "Dziś koniec zajęć!"
            }
        }
    }
    BsSurface(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.semantics(mergeDescendants = true) {
                contentDescription = text
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.School, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Koniec roku",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
fun EventRow(e: DormEvent, isCurrent: Boolean = false, flash: Boolean = false) {
    val haptics = LocalHapticFeedback.current
    val scheme = MaterialTheme.colorScheme
    // Zwykły wiersz to płaska tafla, bieżący w kolorze primary jak hero.
    // Mignięcie z Nadchodzących: jasny pojemnik gaśnie sam po ~1 s (timer w Planie).
    val rowBg by animateColorAsState(
        when {
            flash -> scheme.primaryContainer
            isCurrent -> scheme.primary
            else -> scheme.surfaceContainerLowest
        },
        tween(FluentMotion.EnterMs), label = "flash"
    )
    val content = if (isCurrent) scheme.onPrimary else scheme.onSurface
    val subContent = if (isCurrent) scheme.onPrimary.copy(alpha = 0.72f)
    else scheme.onSurfaceVariant
    val borderColor = if (isCurrent) scheme.onPrimary.copy(alpha = 0.35f)
    else scheme.outlineVariant
    var expanded by remember(e.id) { mutableStateOf(false) }
    val rowPress = remember { MutableInteractionSource() }
    val rowScale = rememberFluentPress(rowPress)
    Surface(
        modifier = Modifier.fillMaxWidth()
            .semantics {
                this[SemanticsProperties.Role] = Role.Button
                stateDescription = if (expanded) "Rozwinięte" else "Zwinięte"
            },
        shape = RoundedCornerShape(20.dp),
        color = rowBg,
        border = BorderStroke(1.dp, borderColor),
        onClick = { expanded = !expanded; haptics.performTick() },
        interactionSource = rowPress
    ) {
        Row(Modifier.fluentScale(rowScale).padding(20.dp), verticalAlignment = Alignment.Top) {
            // Ikona zawsze w tym samym kole — żaden wiersz nie "pływa".
            if (isCurrent) IconDotOnContainer(eventIcon(e))
            else IconDot(eventIcon(e))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    e.title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = content,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    e.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = subContent,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                timeRange(e),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = content,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                softWrap = true,
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.End,
                modifier = Modifier.widthIn(min = 64.dp, max = 96.dp)
            )
        }
    }
}

@Composable
fun ScheduleScreen(
    focusKey: String? = null,
    onFocusConsumed: () -> Unit = {}
) {
    // Takt 30 s wyrównany do ściany zegara (nie dryfuje za granicę eventu).
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000 - System.currentTimeMillis() % 30_000)
            tick++
        }
    }
    // Dzień i „teraz" liczone z jednego odczytu na tick (bez straddlingu minuty).
    val nowTick = remember(tick) { LocalDateTime.now() }
    val today = remember(nowTick) { nowTick.toLocalDate() }
    // Fokus z Nadchodzących ("data|id"): pokazujemy TEN dzień, niekoniecznie dziś.
    // Dzień przypinamy lokalnie: zgaszenie fokusu po mignięciu nie może cofać
    // ekranu do dziś w połowie oglądania (błąd: plan wracał po 400 ms).
    val focus = remember(focusKey) { focusKey?.let { parseFocusKey(it) } }
    var pinnedDate by remember { mutableStateOf<LocalDate?>(focus?.first) }
    LaunchedEffect(focus) { focus?.let { pinnedDate = it.first } }
    val shownDate = pinnedDate ?: today
    val shownEvents = remember(shownDate) { DormSchedule.eventsForDate(shownDate) }
    val events = shownEvents
    val isShownToday = shownDate == today
    val closedForHighlight = remember(nowTick) { DormSchedule.isNoCare(nowTick) }
    val current = remember(nowTick, events, isShownToday) {
        if (!isShownToday || closedForHighlight) null
        else DormSchedule.currentEvent(nowTick.toLocalTime(), events)
    }
    val focusId = focus?.second?.id
    val listState = rememberLazyListState()
    // Mignięcie zgasza fokus — wiersz wraca do zwykłego wyglądu.
    var flashId by remember(focusKey) { mutableStateOf(focusId) }
    LaunchedEffect(focusId, events) {
        if (focusId == null) return@LaunchedEffect
        val idx = events.indexOfFirst { it.id == focusId }
        if (idx >= 0) {
            try { listState.scrollToItem(maxOf(0, idx - 1)) } catch (_: Exception) { }
        }
        delay(400)
        flashId = null
        onFocusConsumed()
    }
    // Baner, gdy TERAZ brak opieki (sobota / nd<16:00 / pt≥18:00) — nie tylko sobota.
    val isClosedNow = closedForHighlight && isShownToday
    val isFullyClosed = remember(shownDate) { DormSchedule.isFullyClosedDay(shownDate) }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            TabHero(
                "Porządek dnia",
                if (isShownToday) "Zał. nr 6 Regulaminu • internat czynny ndz 16:00 – pt 18:00"
                else {
                    val dow = shownDate.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, plLocale)
                    "Plan na $dow ${shownDate.dayOfMonth}.${shownDate.monthValue}"
                }
            )
            AnimatedVisibility(visible = isClosedNow) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    StatusChip(
                        if (isFullyClosed) "Brak opieki wychowawczej — plan poglądowy"
                        else "Brak opieki teraz — plan poglądowy (czynne ndz 16:00 – pt 18:00)"
                    )
                }
            }
        }
        // Bez Appear w leniwych wierszach — LazyColumn kasuje kompozycję poza
        // viewportem i wejście odtwarzałoby się przy każdym scroll-backu.
        itemsIndexed(events, key = { _, e -> e.id }) { _, e ->
            EventRow(
                e,
                isCurrent = !closedForHighlight && e.id == current?.id,
                flash = e.id == flashId
            )
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "W czasie nauki własnej i ciszy nocnej drzwi wejściowe są zamknięte. Powrót do 21:00.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MealsScreen() {
    // Godziny bierzemy z DormSchedule (jedno źródło prawdy), nie z literałów.
    // Takt 30 s jak w Planie: rolowanie po północy bez wracania na zakładkę.
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000 - System.currentTimeMillis() % 30_000)
            tick++
        }
    }
    val todayMeals = remember { mutableStateOf(LocalDate.now()) }
    // Odśwież po północy przy powrocie na zakładkę (SaveableStateProvider trzyma stan).
    LifecycleResumeEffect(Unit) {
        todayMeals.value = LocalDate.now()
        onPauseOrDispose { }
    }
    LaunchedEffect(tick) {
        val d = LocalDate.now()
        if (d != todayMeals.value) todayMeals.value = d
    }
    val nowTick = remember(tick, todayMeals.value) { LocalDateTime.now() }
    val meals = remember(todayMeals.value) { DormSchedule.eventsForDate(todayMeals.value) }
    fun hours(id: String) = meals.firstOrNull { it.id == id }?.let { timeRange(it) } ?: "—"
    // Każdy posiłek liczony WŁASNYM czasem: piątek 19:00 pokazuje śniadanie i obiad
    // zjedzone za dnia, a kreskę tylko tam, gdzie stołówka była nieczynna.
    fun closedFor(id: String): Boolean {
        val e = meals.firstOrNull { it.id == id } ?: return true
        return DormSchedule.isNoCare(LocalDateTime.of(todayMeals.value, e.start))
    }
    // Baner, gdy TERAZ brak opieki (jak w Planie i Dziś), nie tylko w soboty.
    val closedToday = remember(nowTick) { DormSchedule.isNoCare(nowTick) }
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            TabHero(
                "Stołówka",
                "Zał. nr 3 • 3 posiłki dziennie, wykupienie pakietu obowiązkowe"
            )
            AnimatedVisibility(visible = closedToday) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    StatusChip("Brak opieki wychowawczej — stołówka nieczynna")
                }
            }
        }
        item {
            MealCard(
                "Śniadanie",
                if (closedFor("sniadanie")) "—" else hours("sniadanie"),
                if (closedFor("sniadanie")) "Stołówka nieczynna w dniu bez opieki." else "Szwedzki stół. Schludny ubiór, samoobsługa, odnieś naczynia.",
                Icons.Filled.FreeBreakfast
            )
        }
        item {
            MealCard(
                "Obiad",
                if (closedFor("obiad")) "—" else hours("obiad"),
                if (closedFor("obiad")) "Stołówka nieczynna w dniu bez opieki." else "Dwudaniowy + kompot, codziennie.",
                Icons.Filled.LunchDining
            )
        }
        item {
            MealCard(
                "Kolacja",
                if (closedFor("kolacja")) "—" else hours("kolacja"),
                if (closedFor("kolacja")) "Stołówka nieczynna w dniu bez opieki." else "Szwedzki stół (bez dni wyjazdów). Po posiłku porządek na stoliku.",
                Icons.Filled.Restaurant
            )
        }
        item {
            BsSurface(Modifier.fillMaxWidth()) {
                Text("Zasady", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                val rules = listOf(
                    "Zakaz wynoszenia naczyń i posiłków do pokoi.",
                    "W pokojach tylko herbata/kawa/gorący kubek, we własnych naczyniach.",
                    "Nieobecność ≥ 2 dni zgłoś intendentowi dzień wcześniej (osobiście / tel. / SMS) — odliczenie w kolejnym miesiącu.",
                    "Opłaty za wyżywienie do 15. każdego miesiąca."
                )
                rules.forEach {
                    Row(verticalAlignment = Alignment.Top) {
                        Text("•", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun MealCard(title: String, hours: String, desc: String, icon: ImageVector = Icons.Filled.LunchDining) {
    var expanded by remember(title, hours, desc) { mutableStateOf(false) }
    BsSurface(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            IconDot(icon, boxSize = 52.dp, iconSize = 28.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    hours,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .semantics {
                            this[SemanticsProperties.Role] = Role.Button
                            stateDescription = if (expanded) "Rozwinięte" else "Zwinięte"
                        }
                        .clickable { expanded = !expanded }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChangelogScreen() {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            TabHero(
                "Zmiany",
                "Co nowego w Internaucie"
            )
        }
        // Karta aktualizacji: stan sieci + version.json z GitHub. Zawsze nad About,
        // bo to status operacyjny, nie historia.
        item {
            UpdateCard()
        }
        item {
            InfoCard("1.71.0 — mądrzejsze powiadomienia", listOf(
                "Pobudka odzywa się 5 minut przed startem, nie 20.",
                "Każde przypomnienie mówi też, co dalej — np. „Dalej: Śniadanie • 07:00”.",
                "Trwałe „Internauta czuwa” pokazuje Teraz i Dalej razem."
            ))
            InfoCard("1.70.0 — porządek w zmianach", listOf(
                "Historia zmian na nowo: starsze wpisy usunięte, ostatnie wydania napisane po ludzku.",
                "Bez zmian w działaniu aplikacji."
            ))
            InfoCard("1.69.0 — poprawki błędów", listOf(
                "Plan dnia zostaje na dniu wybranym z Nadchodzących — nie wraca sam do dzisiaj.",
                "NASTĘPNE nie pokazuje już tego samego, co TERAZ.",
                "Licznik końca roku odświeża się sam i mówi Wakacje! w wakacje.",
                "Pierścień TERAZ startuje od zera — bez pełnego koła na starcie wydarzenia.",
                "Spóźnione przypomnienie gaśnie dokładnie o czasie startu.",
                "Wyłączone powiadomienia wyłączają też budziki."
            ))
            InfoCard("1.68.0 — wydanie z Linuksa", listOf(
                "Wersja zbudowana i podpisana na Linuksie.",
                "Bez zmian w działaniu aplikacji."
            ))
            InfoCard("1.67.0 — fala jak w Material 3", listOf(
                "Tor i fala mają tę samą grubość — jak w Material 3 Expressive.",
                "Fala ma stałą długość: nawet krótki łuk to pełne, szerokie fale.",
                "Reszta bez zmian."
            ))
            InfoCard("1.66.0 — miękka fala", listOf(
                "Fala szeroka i spokojna, bez wibrowania — jak w stylu Oddychanie.",
                "Reszta bez zmian."
            ))
            InfoCard("1.65.0 — fala wraca", listOf(
                "Postęp znowu faluje, a krótki łuk to jedna albo dwie szerokie fale.",
                "Reszta bez zmian."
            ))
            InfoCard("1.64.0 — fala tylko na kole", listOf(
                "Postęp to gładki łuk, a fala pojawia się tylko na pełnym kole TERAZ.",
                "Koniec poszarpanej fali przy krótkim postępie.",
                "Reszta bez zmian."
            ))
            InfoCard("1.63.0 — krótszy błysk", listOf(
                "Mignięcie w Planie trwa 0,4 s zamiast 1,2 s.",
                "Reszta bez zmian."
            ))
            InfoCard("1.62.0 — jeden dzwonek i wibracje", listOf(
                "Wydarzenia o tej samej godzinie dzwonią razem — jednym powiadomieniem.",
                "Lekkie odbicie dotykowe przy zakładce, zmianie motywu i wierszu planu.",
                "Przegląd kodu: 5 naprawionych błędów i spokojniejsze planowanie powiadomień."
            ))
            InfoCard("1.61.0 — żywy TERAZ", listOf(
                "Fala na TERAZ nigdy nie gaśnie — także przy zamkniętym internacie.",
                "Reszta bez zmian."
            ))
            InfoCard("1.60.0 — cofnięcie fali", listOf(
                "Fala wraca tylko przy trwającym wydarzeniu.",
                "Reszta bez zmian."
            ))
        }
    }
}

@Composable
fun InfoCard(title: String, lines: List<String>) {
    BsSurface(Modifier.fillMaxWidth()) {
        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(8.dp))
        lines.forEach {
            Row(verticalAlignment = Alignment.Top) {
                Text("•", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

/** Karta aktualizacji z GitHub. Działa offline: pokazuje brak sieci, nie blokuje. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UpdateCard() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<UpdateState>(UpdateState.Idle) }
    val online = rememberIsOnline()

    fun runCheck() {
        if (state == UpdateState.Checking) return
        scope.launch {
            state = UpdateState.Checking
            state = checkForUpdate(context)
        }
    }

    // Pierwsze wejście na Zmiany = jedno sprawdzenie. Bez WorkManagera.
    LaunchedEffect(Unit) { runCheck() }
    // Powrót z offline → online: jedno dosprawdzenie także po błędzie, nie pętla.
    LaunchedEffect(online) {
        if (online && (state == UpdateState.Offline || state == UpdateState.Idle || state is UpdateState.Error)) runCheck()
    }

    // Stan lokalnego pobrania dla ZNALEZIONEJ wersji: odświeżany co sekundę
    // w trakcie pobierania + po powrocie z tła (apka mogła nie żyć).
    val avail = (state as? UpdateState.Available)?.info
    var dlTick by remember { mutableStateOf(0) }
    var resumeTick by remember { mutableStateOf(0) }
    LifecycleResumeEffect(Unit) {
        resumeTick++
        onPauseOrDispose { }
    }
    val dl = remember(avail?.versionName, dlTick, resumeTick) {
        avail?.let { queryDownloadStatus(context, it.versionName) }
    }
    val downloading = dl?.state == DlState.RUNNING
    val paused = dl?.state == DlState.PAUSED
    // Rodzaj łącza do szczerego komunikatu: brak Wi-Fi/danych vs dziwne Wi-Fi.
    val kind = remember(online, resumeTick) { connectionKind(context) }
    LaunchedEffect(avail?.versionName, downloading, paused) {
        // Sondowanie postępu w trakcie i we wstrzymaniu (wznowienie też widać);
        // reszta stanów stoi.
        if (downloading || paused) {
            while (true) {
                delay(1000 - System.currentTimeMillis() % 1000)
                dlTick++
            }
        }
    }

    BsSurface(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconDot(
                when (state) {
                    is UpdateState.Available -> Icons.Filled.Notifications
                    is UpdateState.Offline -> Icons.Filled.CloudOff
                    else -> Icons.Filled.History
                }
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Aktualizacja", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    when (val s = state) {
                        UpdateState.Idle, UpdateState.Checking -> "Sprawdzanie…"
                        is UpdateState.Available -> "Dostępna ${s.info.versionName}" + if (s.info.force) " — wymagana" else ""
                        UpdateState.UpToDate -> "Masz najnowszą wersję"
                        UpdateState.Offline -> if (kind == ConnectionKind.NONE) {
                            "Brak Wi-Fi i danych — podłącz się, apka sprawdzi sama"
                        } else {
                            "Połączenie bez internetu — nie można sprawdzić"
                        }
                        is UpdateState.Error -> "Nie sprawdzono: ${s.message}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (state == UpdateState.Checking) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        // Pobieranie w toku: pasek + procent + Anuluj. Samo pobieranie żyje
        // w DownloadManagerze (powiadomienie systemowe), więc trwa mimo zamknięcia apki.
        // Wstrzymane (roaming/brak sieci): pokazujemy wprost, nie wieczne kółko.
        if ((downloading || paused) && dl != null) {
            Spacer(Modifier.height(8.dp))
            if (paused) {
                Text(
                    "Wstrzymano — czeka na sieć",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                val frac = dl.fraction
                if (frac != null) {
                    LinearProgressIndicator(
                        progress = { frac },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Pobieranie… ${(frac * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Pobieranie…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (avail != null && avail.changelog.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                avail.changelog,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Pobrane = tylko Instaluj. W trakcie / wstrzymane = tylko Anuluj.
        // Brak/porażka = Pobierz. Nowsza wersja sama wraca do Pobierz.
        val downloaded = dl?.state == DlState.SUCCESS
        val busy = downloading || paused
        if (downloaded) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Pobrano — gotowe do instalacji",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(20.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(
                onClick = { runCheck() },
                modifier = Modifier.heightIn(min = 48.dp)
            ) { Text(if (state == UpdateState.Checking) "Sprawdzanie…" else "Sprawdź") }
            if (avail != null && !downloaded && !busy) {
                FilledTonalButton(
                    onClick = {
                        downloadAndInstall(context, avail)
                        dlTick++
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { Text("Pobierz ${avail.versionName}") }
            }
            if (busy) {
                TextButton(
                    onClick = {
                        cancelDownload(context)
                        dlTick++
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { Text("Anuluj") }
            }
            if (avail != null && downloaded) {
                FilledTonalButton(
                    onClick = { installApkFile(context, avail.versionName) },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { Text("Zainstaluj") }
            }
        }
        if (state == UpdateState.Offline) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Plan, posiłki i alarmy działają lokalnie.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
