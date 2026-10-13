package com.bscompanion.app.ui

// EKRANY APLIKACJI (Compose, Material3).
// Zakładki: 0 = Dziś, 1 = Plan dnia, 2 = Posiłki, 3 = Zmiany (O aplikacji + changelog).
// Wspólny język wizualny: BsSurface (węgiel + włosek), StatusChip, Ring.
// Animacje: Appear (wejścia), splash (MainActivity), morph cyfr (AnimatedContent), płynny Ring (Animatable).

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
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
import com.bscompanion.app.notifications.BatteryHelper
import com.bscompanion.app.notifications.EventScheduler
import com.bscompanion.app.notifications.ExactAlarmHelper
import com.bscompanion.app.notifications.NotificationHelper
import com.bscompanion.app.notifications.Prefs
import com.bscompanion.app.notifications.findActivity
import com.bscompanion.app.update.UpdateState
import com.bscompanion.app.update.checkForUpdate
import com.bscompanion.app.update.downloadAndInstall
import com.bscompanion.app.update.installApkFile
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
    val end = e.end ?: return null
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

/** Ikona w jednolitym kole — ten sam środek optyczny w każdym wierszu/karcie. */
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
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, Modifier.size(iconSize), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun IconDotOnContainer(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    boxSize: Dp = 44.dp,
    iconSize: Dp = 24.dp
) {
    Box(
        modifier
            .size(boxSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, Modifier.size(iconSize), tint = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

/** Pierścień postępu: łuk dryfu płynnie, a przy zmianie wydarzenia SKACZE. */
@Composable
fun Ring(
    progress: Float?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val track = MaterialTheme.colorScheme.outlineVariant
    val arc = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current
    val strokeRound = remember(density) {
        Stroke(with(density) { 4.dp.toPx() }, cap = StrokeCap.Round)
    }
    val strokeButt = remember(density) {
        Stroke(with(density) { 4.dp.toPx() }, cap = StrokeCap.Butt)
    }
    val anim = remember { Animatable(0f) }
    val want = progress?.coerceIn(0f, 1f) ?: 0f
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
        // Inset o połowę stroku (2.dp), żeby zewnętrzne 2dp nie było ucinane.
        Canvas(Modifier.fillMaxSize().padding(2.dp)) {
            drawArc(track, -90f, 360f, false, style = strokeButt)
            if (anim.value > 0.01f) {
                // Przy pełnym kole Round cap robi garb na łączeniu — wtedy Butt.
                val style = if (anim.value >= 0.999f) strokeButt else strokeRound
                drawArc(arc, -90f, 360f * anim.value, false, style = style)
            }
        }
        content()
    }
}

@Composable
fun Appear(index: Int = 0, content: @Composable () -> Unit) {
    // Saveable — powrót na zakładkę nie odtwarza wejścia. Nie używać w leniwych
    // wierszach (LazyColumn kasuje kompozycję poza viewportem i tak odtworzy).
    var visible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(250, delayMillis = index * 40)) +
            slideInVertically(tween(250, delayMillis = index * 40)) { it / 4 }
    ) { content() }
}

@Composable
fun BsApp(themeMode: Int, onThemeChange: (Int) -> Unit) {
    var tab by rememberSaveable { mutableStateOf(0) }
    val titles = listOf("Dziś", "Plan dnia", "Posiłki", "Zmiany")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.Schedule, Icons.Filled.LunchDining, Icons.Filled.History)
    // Back z zakładek 1-3 wraca do Dziś, wyjście tylko z 0.
    BackHandler(enabled = tab != 0) { tab = 0 }
    val saveableHolder = rememberSaveableStateHolder()
    // Subtelny stan sieci dla całej apki — kropka offline, reszta działa lokalnie.
    val online = rememberIsOnline()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                    titles.forEachIndexed { i, t ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            // Jeden węzeł semantyczny — label już niesie nazwę, ikona dekoracyjna.
                            icon = { Icon(icons[i], contentDescription = null) },
                            label = { Text(t, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(Modifier.fillMaxSize().padding(padding)) {
            // Miękkie przejście między zakładkami; treść od razu WIDOCZNA (bez przezroczystego otwarcia).
            // SaveableStateProvider trzyma scroll + saveable stany zakładek.
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
                            "Offline — plan działa lokalnie",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                androidx.compose.foundation.layout.Box(Modifier.weight(1f).fillMaxWidth()) {
            AnimatedContent(
                targetState = tab,
                transitionSpec = {
                    fadeIn(tween(150)) togetherWith fadeOut(tween(150))
                },
                label = "tabs"
            ) { t ->
                saveableHolder.SaveableStateProvider(t) {
                    when (t) {
                        0 -> HomeScreen()
                        1 -> ScheduleScreen()
                        2 -> MealsScreen()
                        3 -> ChangelogScreen(themeMode, onThemeChange)
                    }
                }
            }
                }
            }
            // Godzina policyjna 21:00–07:00: subtelna czerwona winieta nad treścią.
            CurfewVignette()
        }
    }
    /* Poza barem celowo nie wychodzimy: winieta w treści wystarczy,
       a dolny pasek nawigacji zostaje czytelny. */
}

/** Czerwień szlabanu — stonowana, czytelna na obu motywach. */
private val CurfewRed = Color(0xFFE0322A)

/**
 * Subtelna czerwona winieta godziny policyjnej 21:00–07:00 (jak szlaban na noc).
 * Środek ekranu czysty, czerwień tylko na krawędziach. Nieklikalna (Box bez
 * handlerów) — dotyk przechodzi do treści pod spodem. Własny takt minutowy,
 * więc reszta ekranu stoi.
 */
@Composable
fun BoxScope.CurfewVignette() {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000 - System.currentTimeMillis() % 60_000)
            now = LocalDateTime.now()
        }
    }
    // Powrót z tła po długiej przerwie — odśwież natychmiast, nie za minutę.
    var resumeTick by remember { mutableStateOf(0) }
    LifecycleResumeEffect(Unit) {
        resumeTick++
        onPauseOrDispose { }
    }
    val active = remember(now.truncatedTo(ChronoUnit.MINUTES), resumeTick) {
        DormSchedule.isCurfew(now.toLocalTime())
    }
    if (!active) return
    BoxWithConstraints(Modifier.matchParentSize().clearAndSetSemantics { }) {
        val density = LocalDensity.current
        val wPx = with(density) { maxWidth.toPx() }
        val hPx = with(density) { maxHeight.toPx() }
        val brush = remember(wPx, hPx) {
            Brush.radialGradient(
                0f to CurfewRed.copy(alpha = 0f),
                0.55f to CurfewRed.copy(alpha = 0f),
                0.82f to CurfewRed.copy(alpha = 0.06f),
                1f to CurfewRed.copy(alpha = 0.18f),
                center = Offset(wPx / 2f, hPx / 2f),
                radius = kotlin.math.hypot(wPx, hPx) / 2f
            )
        }
        Box(Modifier.fillMaxSize().background(brush))
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    // SEKUNDNIK: tyka co 1 s, ale czytają go tylko małe poddrzewa (ClockHeader/EventsGrid)
    // przez referencję State — sama HomeScreen NIE przerysowuje się co takt.
    // Minutnik pochodny dla osi Nadchodzące (rekompozycja 1/min, nie 1/s).
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
    val minuteState = remember {
        derivedStateOf { nowState.value.truncatedTo(ChronoUnit.MINUTES) }
    }
    // Minutowy takt mieszka w NotifCard — HomeScreen go nie subskrybuje.
    var notifEnabled by rememberSaveable { mutableStateOf(Prefs.isEnabled(context)) }
    var minutesBefore by rememberSaveable { mutableStateOf(Prefs.minutesBefore(context)) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Appear(0) { ClockHeader(nowState) }
        }
        item {
            Appear(1) { EventsGrid(nowState) }
        }
        item {
            Appear(2) { UpcomingCard(minuteState) }
        }
        item {
            Appear(3) {
                NotifCard(
                    notifEnabled = notifEnabled,
                    minutesBefore = minutesBefore,
                    onEnabledChange = {
                        notifEnabled = it
                        Prefs.setEnabled(context, it)
                        EventScheduler.rescheduleAsync(context, force = true)
                    },
                    onMinutesChange = {
                        minutesBefore = it
                        Prefs.setMinutesBefore(context, it)
                        EventScheduler.rescheduleAsync(context, force = true)
                    }
                )
            }
        }
    }
}

/** Nagłówek zegara — jedyny czytelnik taktu oprócz siatki i licznika. */
@Composable
fun ClockHeader(nowState: State<LocalDateTime>) {
    val now by nowState
    val dateStr = remember(now.toLocalDate()) {
        now.toLocalDate().format(dateFmt)
    }
    // Kwantyzacja do minuty — bez 60x zbędnych rekompozycji na sekundę.
    // AnimatedContent i tak odpala się tylko przy zmianie "HH:mm".
    val minuteKey = remember(now) { now.truncatedTo(ChronoUnit.MINUTES) }
    val timeStr = remember(minuteKey) {
        minuteKey.format(timeFmt)
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            "BS Companion",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        AnimatedContent(
            targetState = timeStr,
            transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
            label = "clock"
        ) { animatedTime ->
            Text(
                animatedTime,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            dateStr.replaceFirstChar { it.uppercase(plLocale) },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (DormSchedule.isNoCare(now)) {
            Spacer(Modifier.height(8.dp))
            StatusChip("Brak opieki wychowawczej")
        }
    }
}

/** Siatka TERAZ / NASTĘPNE — czyta takt, reszta ekranu stoi. */
@Composable
private fun TerazContent(now: LocalDateTime, closed: Boolean, current: DormEvent?) {
    if (closed) {
        // Ta sama kotwica 56dp co Ring — ikony w obu kartach w jednej linii.
        Ring(progress = null) {
            Icon(
                Icons.Filled.Bedtime, null,
                Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "TERAZ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
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
                categoryIcon(current.category), null,
                Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "TERAZ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Text(
            current.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Text(
            timeRange(current),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip
        )
    } else {
        Ring(progress = null) {
            Icon(
                Icons.Filled.WbSunny, null,
                Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(12.dp))
        Text("TERAZ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text("Przerwa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun NastepneContent(
    now: LocalDateTime,
    today: LocalDate,
    next: DormEvent?,
    nextDate: LocalDate?,
    compact: Boolean = false
) {
    if (next != null && nextDate != null) {
        val target = LocalDateTime.of(nextDate, next.start)
        // Dzień tylko spoza dziś ("Obiad • 13:30–15:30" albo "Obiad • sob • ...").
        val day = if (nextDate == today) "" else nextDate.dayOfWeek.getDisplayName(
            java.time.format.TextStyle.SHORT, plLocale
        )
        val head = if (day.isEmpty()) next.title else "${next.title} • $day"
        // Kotwica 56dp jak Ring obok + ikona z kategorii (nie stałe śniadanie).
        Ring(progress = null) {
            Icon(
                categoryIcon(next.category), null,
                Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "NASTĘPNE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Text(
            formatCountdown(now, target),
            style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip
        )
        Spacer(Modifier.height(4.dp))
        Text(
            head,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            timeRange(next),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip
        )
    } else {
        Ring(progress = null) {
            Icon(
                Icons.Filled.FreeBreakfast, null,
                Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(12.dp))
        Text("NASTĘPNE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text("Koniec planu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Spacer(Modifier.height(16.dp))
    }
}

/** Siatka TERAZ / NASTĘPNE — czyta takt, reszta ekranu stoi. */
@Composable
fun EventsGrid(nowState: State<LocalDateTime>) {
    val now by nowState
    // Poza godzinami opieki pokazujemy stan zamknięcia zamiast ciszy z planu.
    val closed = DormSchedule.isNoCare(now)
    val today = now.toLocalDate()
    val events = remember(today) { DormSchedule.eventsForDate(today) }
    // Lookup obcięty do minut (12:00:00.5 nie gubi eventu 12:00); licznik pełny.
    val lookupNow = remember(now) { now.truncatedTo(ChronoUnit.MINUTES) }
    val current = remember(lookupNow, events) { DormSchedule.currentCaredEvent(lookupNow, events) }
    val cared = remember(lookupNow) { DormSchedule.nextCaredEvent(lookupNow) }
    val next = cared?.second
    val nextDate = cared?.first
    // Obok siebie jak wcześniej: TERAZ | NASTĘPNE w jednym wierszu, zawsze równa wysokość.
    // Wąsko: mniejszy padding + countdown titleMedium, żeby licznik mieścił się w ~134dp.
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val compact = maxWidth < 380.dp
        val cardPadding = if (compact) 12.dp else 16.dp
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BsSurface(Modifier.weight(1f).fillMaxHeight().heightIn(min = 150.dp), contentPadding = cardPadding) {
                TerazContent(now, closed, current)
            }
            BsSurface(Modifier.weight(1f).fillMaxHeight().heightIn(min = 150.dp), contentPadding = cardPadding) {
                NastepneContent(now, today, next, nextDate, compact = compact)
            }
        }
    }
}

/** Oś czasu: 4 najbliższe punkty z opieką (z dniem, gdy nie dziś). */
@Composable
fun UpcomingCard(nowState: State<LocalDateTime>) {
    val now by nowState
    // Stan minutowy (pochodny) — rekompozycja 1/min, nie 1/s. Obcięcie chroni
    // przed zgubieniem eventu startującego dokładnie w sekundzie tyknięcia.
    val items = remember(now) {
        DormSchedule.upcomingCared(now.truncatedTo(ChronoUnit.MINUTES), 4)
    }
    val today = now.toLocalDate()
    val dateSuffixFmt = remember { java.time.format.DateTimeFormatter.ofPattern("d.M") }
    BsSurface(Modifier.fillMaxWidth()) {
        Text("Nadchodzące", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        if (items.isEmpty()) {
            Text(
                "Brak nadchodzących punktów z opieką.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items.forEachIndexed { i, (date, e) ->
            val day = when (date) {
                today -> "dziś"
                today.plusDays(1) -> "jutro"
                else -> "${date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, plLocale)} ${date.format(dateSuffixFmt)}"
            }
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    e.start.format(timeFmt),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.widthIn(min = 52.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        e.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "$day • ${timeRange(e)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip
                    )
                }
            }
            if (i < items.lastIndex) Spacer(Modifier.height(12.dp))
        }
    }
}

/** Węglowa karta z włoskowatą ramką — wspólny język całego UI. */
@Composable
fun BsSurface(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 22.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        // Bez spacedBy: odstępy niosą jawne Spacery (4/8/12dp) — inaczej sumowały się.
        content = { Column(Modifier.padding(contentPadding), verticalArrangement = Arrangement.spacedBy(0.dp), content = content) }
    )
}

/** Karta ustawień powiadomień + pracy w tle (własny takt minutowy). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotifCard(
    notifEnabled: Boolean,
    minutesBefore: Int,
    onEnabledChange: (Boolean) -> Unit,
    onMinutesChange: (Int) -> Unit
) {
    val context = LocalContext.current
    var minuteTick by remember { mutableStateOf(LocalTime.now().truncatedTo(ChronoUnit.MINUTES)) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000 - System.currentTimeMillis() % 60_000)
            minuteTick = LocalTime.now().truncatedTo(ChronoUnit.MINUTES)
        }
    }
    val today = remember(minuteTick) { LocalDate.now() }
    val cared = remember(today, minuteTick) {
        DormSchedule.nextCaredEvent(LocalDateTime.of(today, minuteTick))
    }
    val nextTitle = cared?.second?.title ?: "—"
    var resumeTick by remember { mutableStateOf(0) }
    LifecycleResumeEffect(Unit) {
        // Powrót z ustawień systemowych — odśwież Binder-stan natychmiast, nie za 60s.
        resumeTick++
        onPauseOrDispose { }
    }
    val blocked = remember(minuteTick, notifEnabled, resumeTick) { NotificationHelper.notificationsBlocked(context) }
    // Start synchronicznie z realną wartością (bez flesza po pierwszej klatce).
    var canExact by remember { mutableStateOf(ExactAlarmHelper.canSchedule(context)) }
    LifecycleResumeEffect(resumeTick) {
        canExact = ExactAlarmHelper.canSchedule(context)
        onPauseOrDispose { }
    }
    BsSurface(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.heightIn(min = 48.dp).clickable(
                onClick = { onEnabledChange(!notifEnabled) }
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Notifications, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Powiadomienia",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Switch(checked = notifEnabled, onCheckedChange = onEnabledChange)
        }
        if (blocked) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Powiadomienia zablokowane", style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        "System je wycisza — test też nic nie pokaże",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = {
                    NotificationHelper.openAppNotificationSettings(context)
                }) { Text("Ustawienia") }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Przypomnienia o posiłkach, nauce własnej, ciszy nocnej i powrocie.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text("Przypomnij wcześniej:", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        // FlowRow: na wąskim ekranie chipy zawijają się zamiast ucinać.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(5, 10, 15, 30).forEach { m ->
                FilterChip(
                    selected = minutesBefore == m,
                    onClick = { onMinutesChange(m) },
                    label = { Text("$m min") }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    EventScheduler.rescheduleAsync(context, force = true)
                },
                modifier = Modifier.heightIn(min = 48.dp)
            ) { Text("Przeplanuj") }
            TextButton(
                onClick = {
                    if (NotificationHelper.notificationsBlocked(context)) {
                        android.widget.Toast.makeText(context, "Powiadomienia zablokowane w systemie", android.widget.Toast.LENGTH_SHORT).show()
                        NotificationHelper.openAppNotificationSettings(context)
                    } else {
                        NotificationHelper.showEventNotification(
                            context, 9999, "Test BS Companion",
                            "Powiadomienia działają. Kolejne: $nextTitle", false
                        )
                        android.widget.Toast.makeText(context, "Test wysłany", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.heightIn(min = 48.dp)
            ) { Text("Wyślij testowe") }
        }
        Spacer(Modifier.height(8.dp))
        AnimatedVisibility(visible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canExact) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Dokładne alarmy", style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        "Wyłączone — przypomnienia mogą się spóźniać",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = {
                    context.findActivity()?.let { ExactAlarmHelper.openSettings(it) }
                }) { Text("Zezwól na alarmy") }
            }
        }
        BatteryRow()
    }
}

/** Wiersz statusu baterii z własnym wolnym taktem (nie budzi reszty karty). */
@Composable
fun BatteryRow() {
    val context = LocalContext.current
    fun safeExempt(): Boolean = try {
        BatteryHelper.isExempt(context)
    } catch (e: Exception) {
        android.util.Log.w("BatteryRow", "isExempt failed", e)
        false
    }
    var exempt by remember { mutableStateOf(safeExempt()) }
    LifecycleResumeEffect(Unit) {
        exempt = safeExempt()
        onPauseOrDispose { }
    }
    // Flaga ask-once: po pierwszym zapytaniu nie pokazujemy systemowego dialogu
    // w kółko — tylko ekran ustawień (niektóre OEM-y blokują REQUEST_...).
    val asked = remember(exempt) {
        try { Prefs.isBatteryAsked(context) } catch (_: Exception) { false }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Praca w tle", style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                if (exempt) "Bez ograniczeń — alarmy niezawodne"
                else "System może ucinać alarmy",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        AnimatedVisibility(visible = !exempt) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = {
                val act = context.findActivity()
                if (act == null) {
                    android.widget.Toast.makeText(context, "Nie można otworzyć ustawień", android.widget.Toast.LENGTH_SHORT).show()
                    return@TextButton
                }
                try {
                    if (!asked) {
                        BatteryHelper.requestExempt(act)
                        try { Prefs.setBatteryAsked(act) } catch (e: Exception) {
                            android.util.Log.w("BatteryRow", "setBatteryAsked failed", e)
                        }
                    } else {
                        // Już pytano: prowadź wprost do ustawień (dialog OEM mógł być zablokowany).
                        try {
                            act.startActivity(android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                        } catch (e: Exception) {
                            android.util.Log.w("BatteryRow", "open battery settings failed", e)
                            android.widget.Toast.makeText(context, "Nie można otworzyć ustawień baterii", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.w("BatteryRow", "battery request failed", e)
                    android.widget.Toast.makeText(context, "Nie można otworzyć ustawień baterii", android.widget.Toast.LENGTH_SHORT).show()
                }
                exempt = safeExempt()
                }) { Text(if (asked) "Ustawienia baterii" else "Zezwól na tło") }
            }
        }
    }
}

@Composable
fun EventRow(e: DormEvent, isCurrent: Boolean = false) {
    val container = if (isCurrent) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceContainerHigh
    val content = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface
    val subContent = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    else MaterialTheme.colorScheme.outlineVariant
    var expanded by remember(e.id) { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = container,
        border = BorderStroke(1.dp, borderColor),
        onClick = { expanded = !expanded }
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
            // Ikona zawsze w tym samym kole — żaden wiersz nie "pływa".
            if (isCurrent) IconDotOnContainer(categoryIcon(e.category))
            else IconDot(categoryIcon(e.category))
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
fun ScheduleScreen() {
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
    val events = remember(today) { DormSchedule.eventsForDate(today) }
    val closedForHighlight = remember(nowTick) { DormSchedule.isNoCare(nowTick) }
    val current = remember(nowTick, events) {
        if (closedForHighlight) null
        else DormSchedule.currentEvent(nowTick.toLocalTime(), events)
    }
    // Baner, gdy TERAZ brak opieki (sobota / nd<16:00 / pt≥18:00) — nie tylko sobota.
    val isClosedNow = closedForHighlight
    val isFullyClosed = remember(today) { DormSchedule.isFullyClosedDay(today) }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text("Porządek dnia", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(
                "Zał. nr 6 Regulaminu • internat czynny ndz 16:00 – pt 18:00",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
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
            EventRow(e, isCurrent = !closedForHighlight && e.id == current?.id)
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
    // Baner, gdy TERAZ brak opieki (jak w Planie i Dziś), nie tylko w soboty.
    val closedToday = remember(nowTick) { DormSchedule.isNoCare(nowTick) }
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text("Stołówka", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "Zał. nr 3 • 3 posiłki dziennie, wykupienie pakietu obowiązkowe",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
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
                if (closedToday) "—" else hours("sniadanie"),
                if (closedToday) "Stołówka nieczynna w dniu bez opieki." else "Szwedzki stół. Schludny ubiór, samoobsługa, odnieś naczynia.",
                Icons.Filled.FreeBreakfast
            )
        }
        item {
            MealCard(
                "Obiad",
                if (closedToday) "—" else hours("obiad"),
                if (closedToday) "Stołówka nieczynna w dniu bez opieki." else "Dwudaniowy + kompot, codziennie.",
                Icons.Filled.LunchDining
            )
        }
        item {
            MealCard(
                "Kolacja",
                if (closedToday) "—" else hours("kolacja"),
                if (closedToday) "Stołówka nieczynna w dniu bez opieki." else "Szwedzki stół (bez dni wyjazdów). Po posiłku porządek na stoliku.",
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
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChangelogScreen(themeMode: Int, onThemeChange: (Int) -> Unit) {
    val versionName = com.bscompanion.app.BuildConfig.VERSION_NAME
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text("Zmiany", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "Co nowego w BS Companion",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        // Karta aktualizacji: stan sieci + version.json z GitHub. Zawsze nad About,
        // bo to status operacyjny, nie historia.
        item {
            UpdateCard()
        }
        // O APLIKACJI POD aktualizacją: karta About + autor, pod nią motyw i historia.
        // Przy dopisywaniu kolejnych wersji dodawaj nowe itemy PONIŻEJ karty motywu.
        item {
            BsSurface(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconDot(Icons.Filled.MenuBook, boxSize = 52.dp, iconSize = 28.dp)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("O aplikacji", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(
                            "BS Companion • wersja $versionName",
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
                // Motyw: 0 = system, 1 = ciemny, 2 = jasny.
                // Stan żyje w MainActivity — zmiana przebudowuje motyw natychmiast.
                Text("Motyw", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "Jasny dopełnia ciemny „ink”.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "System", 1 to "Ciemny", 2 to "Jasny").forEach { (v, label) ->
                        FilterChip(
                            selected = themeMode == v,
                            onClick = { onThemeChange(v) },
                            label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                    }
                }
            }
        }
        item {
            InfoCard("1.2.2 — naprawa instalacji", listOf(
                "FileProvider pokrywa files/Download/updates (koniec z Nie można otworzyć pliku)",
                "DownloadManager przez setDestinationInExternalFilesDir",
                "Zainstaluj czeka na niepusty plik zamiast otwierać pobieranie w locie"
            ))
        }
        item {
            InfoCard("1.2.1 — test kanału aktualizacji", listOf(
                "Wersja testowa: telefon na 1.2.0 ma wykryć tę aktualizację",
                "Ten sam kanał version.json + latest/download co wydania"
            ))
        }
        item {
            InfoCard("1.2.0 — aktualizacje z GitHub i offline", listOf(
                "Karta Aktualizacja w Zmiany: sprawdza version.json z GitHub",
                "Subtelny pasek Offline — plan i alarmy działają lokalnie bez sieci",
                "Brak sieci pokazuje Nie można sprawdzić zamiast crashu",
                "Pobieranie apk przez DownloadManager + instalacja przez FileProvider",
                "Wersja z BuildConfig, URL tylko z backend/api/version.json"
            ))
        }
        item {
            InfoCard("1.1.3 — czytelność UI i kontrast", listOf(
                "TERAZ / NASTĘPNE znowu obok siebie, ciaśniejszy padding na wąskich ekranach",
                "Ciemny secondaryContainer jaśniejszy — zaznaczone chipy widać na karcie",
                "Ramki i Ring na outlineVariant / primary, koniec z niewidzialnymi włoskami",
                "Zegar i licznik już bez … — Clip zamiast Ellipsis, data do 2 linii",
                "Godziny w wierszach i kartach zawijają się, nie gubią końcówki",
                "MealCard: tytuł + godziny osobno, opis rozwijany po tapnięciu",
                "Zamknięta stołówka pokazuje — i Stołówka nieczynna zamiast menu",
                "Pojedynczy Switch dla TalkBack, przyciski min. 48dp, banery z animacją",
                "Splash z insets i wyśrodkowaniem, start 800ms + fade 200ms",
                "Bary i cutout shortEdges, wczesny motyw bez flesza"
            ))
        }
        item {
            InfoCard("1.1.2 — poprawki UI i spójność planu", listOf(
                "NASTĘPNE liczy też koniec wydarzenia — brak licznika bez alarmu (pt 17:30)",
                "Plan i Stołówka pokazują baner Brak opieki w dni zamknięte",
                "Poprawiony kontrast podświetlonego wiersza (onPrimaryContainer)",
                "Banery blokad odświeżają się po powrocie z ustawień",
                "Zakładki trzymają scroll i ustawienia, Back wraca do Dziś",
                "Brak ponownych animacji przy scrollu, lżejszy Ring i zegar co minutę",
                "Odliczanie i godziny nie ucinają się, chipy motywu zawijają się",
                "Puste Nadchodzące ma komunikat, karty czytelniejsze w obu motywach",
                "Splash nie odtwarza się po rotacji, wersja z BuildConfig bez fallbacku"
            ))
        }
        item {
            InfoCard("1.1.1 — stabilność alarmów", listOf(
                "Rotacja / tap w powiadomienie nie zatrzymuje już planisty",
                "Ndz 22:00 cisza planuje się poprawnie (koniec przez północ)",
                "Ustawienia backupowane, cache harmonogramu nie (migracja bs_prefs)",
                "Dokładne alarmy z uczciwym fallbackiem + wiersz Zezwól",
                "Lżejsze anulowanie alarmów, debounce, brak podwójnych dialogów",
                "Poprawione podświetlenie Planu dnia i karty TERAZ / NASTĘPNE",
                "Ciemny start bez białego flesza, ikony barów wg motywu apki"
            ))
        }
        item {
            InfoCard("1.1.0 — Nadchodzące i motyw", listOf(
                "Oś czasu Nadchodzące: 4 kolejne punkty planu z dniem",
                "Wybór motywu System / Ciemny / Jasny",
                "Obiad codziennie 13:30–15:30, koniec z dyżurami w planie",
                "Równe karty TERAZ / NASTĘPNE, naprawiona płynność 120 Hz",
                "Samokasujące się przypomnienia i trwalsze alarmy"
            ))
        }
        item {
            InfoCard("1.0.0 — pierwsze wydanie", listOf(
                "Plan dnia, stołówka i ekran główny z odliczaniem",
                "Przypomnienia 5/10/15/30 min przed z samokasowaniem o czasie wydarzenia",
                "Ciche trwałe powiadomienie z następnym punktem planu",
                "Pomijanie przerw w opiece (weekendy, zamknięcia)",
                "Tryb 120 Hz, ciemny motyw premium, ekran startowy"
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
    // Powrót z offline → online: jedno dosprawdzenie, nie pętla.
    LaunchedEffect(online) {
        if (online && (state == UpdateState.Offline || state == UpdateState.Idle)) runCheck()
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
                        UpdateState.Offline -> "Brak połączenia — nie można sprawdzić"
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
        val avail = (state as? UpdateState.Available)?.info
        if (avail != null && avail.changelog.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                avail.changelog,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(
                onClick = { runCheck() },
                modifier = Modifier.heightIn(min = 48.dp)
            ) { Text(if (state == UpdateState.Checking) "Sprawdzanie…" else "Sprawdź") }
            if (avail != null) {
                FilledTonalButton(
                    onClick = { downloadAndInstall(context, avail) },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { Text("Pobierz ${avail.versionName}") }
                TextButton(
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
