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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
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
    // Czarny krążek + jasna ikona — czytelne na podświetlonym (jasnym) wierszu.
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
fun BsApp() {
    var tab by rememberSaveable { mutableStateOf(0) }
    val titles = listOf("Dziś", "Plan dnia", "Posiłki", "Zmiany")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.Schedule, Icons.Filled.LunchDining, Icons.Filled.History)
    var detailKey by rememberSaveable { mutableStateOf<String?>(null) }
    // Back: najpierw zamyka szczegóły, potem z zakładek 1-3 wraca do Dziś, wyjście tylko z 0.
    BackHandler(enabled = tab != 0 || detailKey != null) {
        if (detailKey != null) detailKey = null else tab = 0
    }
    val saveableHolder = rememberSaveableStateHolder()
    // Subtelny stan sieci dla całej apki — kropka offline, reszta działa lokalnie.
    val online = rememberIsOnline()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            PillBar(titles, icons, tab) { detailKey = null; tab = it }
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
                            "Offline — plan działa lokalnie",
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
            AnimatedContent(
                targetState = tab,
                transitionSpec = {
                    fadeIn(tween(150)) togetherWith fadeOut(tween(150))
                },
                label = "tabs"
            ) { t ->
                saveableHolder.SaveableStateProvider(t) {
                    val detail = remember(detailKey) { detailKey?.let { parseDetailKey(it) } }
                    if (detail != null) {
                        DetailScreen(detail.first, detail.second) { detailKey = null }
                    } else when (t) {
                        0 -> HomeScreen(
                            onTab = { tab = it },
                            onOpenEvent = { d, e -> detailKey = "$d|${e.id}" }
                        )
                        1 -> ScheduleScreen()
                        2 -> MealsScreen()
                        3 -> ChangelogScreen()
                    }
                }
            }
                }
            }
        }
    }
}

/** Pływająca szklana pigułka w języku Nexora: wybrana zakładka to granatowa pastylka. */
@Composable
fun PillBar(
    titles: List<String>,
    icons: List<ImageVector>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Box(
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            // Pigułka nad paskiem gestów — bez tego tonie pod systemową nawigację.
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                titles.forEachIndexed { i, t ->
                    val sel = selected == i
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (sel) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .selectable(selected = sel, onClick = { onSelect(i) }, role = Role.Tab)
                            .semantics { contentDescription = t }
                            .padding(horizontal = if (sel) 16.dp else 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            icons[i], contentDescription = null,
                            Modifier.size(20.dp),
                            tint = if (sel) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
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

/** Klucz szczegółów wydarzenia: "2026-10-13|cisza". */
fun parseDetailKey(key: String): Pair<LocalDate, DormEvent>? {
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
    onOpenEvent: (LocalDate, DormEvent) -> Unit
) {
    val context = LocalContext.current
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
    // Minutnik pochodny dla osi Nadchodzące (rekompozycja 1/min, nie 1/s).
    var notifEnabled by rememberSaveable { mutableStateOf(Prefs.isEnabled(context)) }
    var minutesBefore by rememberSaveable { mutableStateOf(Prefs.minutesBefore(context)) }

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
                    onOpen = onOpenEvent
                )
            }
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

/** Granatowa karta-bohater w języku Nexora: żywy zegar + TERAZ/NASTĘPNE + skróty. */
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
    // Płaski granat — bez gradientów w całej apce.
    val onHero = MaterialTheme.colorScheme.onPrimary
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(Modifier.padding(22.dp)) {
            Text(
                "BS Companion",
                style = MaterialTheme.typography.labelLarge,
                color = onHero.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            AnimatedContent(
                targetState = timeStr,
                transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                label = "clock"
            ) { animatedTime ->
                Text(
                    animatedTime,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = onHero,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
    val cared = remember(lookupNow) { DormSchedule.nextCaredEvent(lookupNow) }
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
                    Ring(progress = null) {
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
                            categoryIcon(current.category), null,
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
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Przerwa",
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
                            categoryIcon(next.category), null,
                            Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        formatCountdown(now, target),
                        style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (day.isEmpty()) next.title else "${next.title} • $day",
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
    onOpen: (LocalDate, DormEvent) -> Unit
) {
    val now by nowState
    // Bez krzykliwej zieleni — odliczanie w stonowanym kolorze tekstu.
    // Stan minutowy (pochodny) — rekompozycja 1/min, nie 1/s.
    val items = remember(now.truncatedTo(ChronoUnit.MINUTES)) {
        DormSchedule.upcomingCared(now.truncatedTo(ChronoUnit.MINUTES), 4)
    }
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
            Row(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpen(date, e) }
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
                        categoryIcon(e.category), null,
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
                        timeRange(e),
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

/** Szczegóły wydarzenia w języku drugiego ekranu Nexora: donut postępu + karty faktów. */
@Composable
fun DetailScreen(date: LocalDate, event: DormEvent, onBack: () -> Unit) {
    val context = LocalContext.current
    // Sekundnik jak na Dziś — donut pełznie płynnie, tekst i tak co minutę.
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        // Wyrównanie do granicy sekund, żeby nie dryfował.
        while (true) {
            delay(1000 - System.currentTimeMillis() % 1000)
            now = LocalDateTime.now()
        }
    }
    val startDT = LocalDateTime.of(date, event.start)
    val endDT = event.end?.let {
        if (it <= event.start) LocalDateTime.of(date.plusDays(1), it)
        else LocalDateTime.of(date, it)
    }
    val totalMin = endDT?.let { Duration.between(startDT, it).toMinutes().coerceAtLeast(1) } ?: 15L
    // Licznik w SEKUNDACH — minutowy skok pierścienia przy sekundowym takcie to był bug.
    val totalSec = endDT?.let { Duration.between(startDT, it).seconds.coerceAtLeast(1) } ?: 15L * 60
    val progress: Float
    val center: String
    val caption: String
    when {
        now.isBefore(startDT) -> {
            progress = 0f
            center = "za " + formatCountdown(now, startDT)
            caption = "do startu"
        }
        endDT != null && !now.isBefore(endDT) -> {
            progress = 1f
            center = "Zakończone"
            caption = "wydarzenie minęło"
        }
        else -> {
            val doneSec = Duration.between(startDT, now).seconds.coerceIn(0, totalSec)
            progress = doneSec / totalSec.toFloat()
            center = "jeszcze " + formatCountdown(now, endDT!!)
            caption = "do końca"
        }
    }
    val today = LocalDate.now()
    val dateSuffixFmt = remember { java.time.format.DateTimeFormatter.ofPattern("d.M") }
    val day = when (date) {
        today -> "dziś"
        today.plusDays(1) -> "jutro"
        else -> "${date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, plLocale)} ${date.format(dateSuffixFmt)}"
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, null,
                        Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        event.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "$day • ${timeRange(event)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        item {
            BsSurface(Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth().height(250.dp)) {
                    val scheme = MaterialTheme.colorScheme
                    Canvas(Modifier.size(210.dp).align(Alignment.Center)) {
                        val w = 22.dp.toPx()
                        drawArc(scheme.outlineVariant, -90f, 360f, false, style = Stroke(w))
                        if (progress > 0.005f) {
                            drawArc(
                                scheme.primary, -90f, 360f * progress, false,
                                style = Stroke(w, cap = StrokeCap.Round)
                            )
                        }
                    }
                    Column(
                        Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            center,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            caption,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    MiniPill(
                        Icons.Filled.Schedule, "Start ${event.start.format(timeFmt)}",
                        Modifier.align(Alignment.TopStart)
                    )
                    MiniPill(
                        Icons.Filled.History, "Koniec ${event.end?.format(timeFmt) ?: "—"}",
                        Modifier.align(Alignment.TopEnd)
                    )
                    MiniPill(
                        Icons.Filled.Timer, "$totalMin min",
                        Modifier.align(Alignment.BottomStart)
                    )
                    MiniPill(
                        Icons.Filled.CheckCircle, "${(progress * 100).toInt()}%",
                        Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        }
        item {
            BsSurface(Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        event.category.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$day • ${timeRange(event)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Start",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            event.start.format(timeFmt),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Koniec",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            event.end?.format(timeFmt) ?: "—",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        item {
            BsSurface(Modifier.fillMaxWidth()) {
                Text(
                    event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                MiniPill(
                    Icons.Filled.Notifications,
                    if (event.notifyDefault) {
                        "Przypomnienie: ${Prefs.minutesBefore(context)} min przed"
                    } else {
                        "Bez przypomnienia"
                    }
                )
            }
        }
    }
}

@Composable
private fun MiniPill(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(5.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

/** Granatowy nagłówek zakładki w języku Nexora — te same napisy, nowa oprawa. */
@Composable
fun TabHero(title: String, subtitle: String) {
    // Płaski granat — bez gradientów w całej apce.
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

/** Szklana karta: gradient tonu + włosek — wspólny język całego UI. */
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
                // Bez natychmiastowego odświeżania: dialog systemowy jeszcze wisi,
                // stan podniesie LifecycleResumeEffect po powrocie.
                }) { Text(if (asked) "Ustawienia baterii" else "Zezwól na tło") }
            }
        }
    }
}

@Composable
fun EventRow(e: DormEvent, isCurrent: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    // Zwykły wiersz to płaska tafla, bieżący to płaski granat jak hero.
    val content = if (isCurrent) scheme.onPrimary else scheme.onSurface
    val subContent = if (isCurrent) scheme.onPrimary.copy(alpha = 0.72f)
    else scheme.onSurfaceVariant
    val borderColor = if (isCurrent) scheme.onPrimary.copy(alpha = 0.35f)
    else scheme.outlineVariant
    var expanded by remember(e.id) { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (isCurrent) scheme.primary else scheme.surfaceContainerLowest,
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
            TabHero(
                "Porządek dnia",
                "Zał. nr 6 Regulaminu • internat czynny ndz 16:00 – pt 18:00"
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
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChangelogScreen() {
    val versionName = com.bscompanion.app.BuildConfig.VERSION_NAME
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            TabHero(
                "Zmiany",
                "Co nowego w BS Companion"
            )
        }
        // Karta aktualizacji: stan sieci + version.json z GitHub. Zawsze nad About,
        // bo to status operacyjny, nie historia.
        item {
            UpdateCard()
        }
        // O APLIKACJI POD aktualizacją: karta About + autor, pod nią historia.
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
            InfoCard("1.18.0 — uczciwsze detale", listOf(
                "Stołówka: każdy posiłek liczony własnym czasem, nie chwilą obecną",
                "Pobieranie pilnuje stanu DownloadManagera zamiast zgadywać po rozmiarze",
                "Donut pełznie co sekundę, ponowienie po błędzie, dostępniejsze przyciski",
                "Koniec martwego theme_mode i przedwczesnego odświeżania baterii"
            ))
        }
        item {
            InfoCard("1.17.0 — twardszy plan i pobieranie", listOf(
                "Debug i testy działają bez klucza — klucz tylko do release",
                "Aktualizacje tylko z https z domen GitHuba",
                "Spóźnione przypomnienie strzela za sekundę zamiast przepadać"
            ))
        }
        item {
            InfoCard("1.16.0 — czytelny znaczek", listOf(
                "Ikona bieżącego wydarzenia: czarny krążek zamiast niewidzialnej bieli",
                "Treść i alarmy bez zmian"
            ))
        }
        item {
            InfoCard("1.15.0 — czysty mono", listOf(
                "Koniec błękitu — tylko czerń, szarości i biel",
                "Biała pigułka i karta-bohater na czerni",
                "Treść i alarmy bez zmian"
            ))
        }
        item {
            InfoCard("1.14.0 — tylko AMOLED", listOf(
                "Koniec wyboru motywu — zawsze głęboka czerń",
                "Usunięta karta Motyw ze Zmian",
                "Treść i alarmy bez zmian"
            ))
        }
        item {
            InfoCard("1.13.0 — spokojniejszy układ", listOf(
                "Bohater bez skrótów — nawigacja tylko w pigułce na dole",
                "Wiersze bez linii i krzykliwej zieleni, lżejsza typografia",
                "Treść i alarmy bez zmian"
            ))
        }
        item {
            InfoCard("1.12.0 — zwarta pigułka nawigacji", listOf(
                "Etykieta tylko na wybranej zakładce — nazwy już się nie ucinają",
                "Treść i alarmy bez zmian"
            ))
        }
        item {
            InfoCard("1.11.0 — pasek nad gestami", listOf(
                "Pigułka nawigacji uniesiona nad systemowy pasek gestów",
                "Treść i alarmy bez zmian"
            ))
        }
        item {
            InfoCard("1.10.0 — pierścień i płaskie tło", listOf(
                "Pierścień postępu rusza też dla wydarzeń punktowych (15 min)",
                "Donut szczegółów pełznie co sekundę zamiast co minutę",
                "Koniec gradientów — płaskie tafle w całej apce",
                "Treść i alarmy bez zmian"
            ))
        }
        item {
            InfoCard("1.9.0 — osobne karty TERAZ/NASTĘPNE", listOf(
                "TERAZ i NASTĘPNE wróciły jako dwie osobne karty pod bohaterem",
                "Pierścień postępu bieżącego wydarzenia, licznik odliczający",
                "Treść i alarmy bez zmian"
            ))
        }
        item {
            InfoCard("1.8.0 — zakładki w języku Nexora", listOf(
                "Granatowe nagłówki Planu, Stołówki i Zmian jak karta-bohater",
                "Wiersze planu: mroźne szkło, bieżące wydarzenie na granatowo",
                "Usunięty wykres zajętości dnia",
                "Treść, teksty i alarmy bez zmian"
            ))
        }
        item {
            InfoCard("1.7.0 — pełna przebudowa Dziś", listOf(
                "Granatowa karta-bohater: żywy zegar, TERAZ/NASTĘPNE, skróty do zakładek",
                "Nadchodzące jako wiersze holdingów + szczegóły wydarzenia z donutem postępu",
                "Wykres zajętości planu dziś (6:00–22:00) liczony z planu",
                "Treść, teksty i alarmy bez zmian"
            ))
        }
        item {
            InfoCard("1.6.0 — wygląd Nexora", listOf(
                "Mroźne szkło: jasny błękit, granatowe akcenty, pływająca pigułka nawigacji",
                "Ciemny motyw w tym samym języku: głęboki granat",
                "Usunięta czerwona winieta godziny policyjnej",
                "Zero zmian treści — wszystkie teksty i alarmy jak w 1.4.1"
            ))
        }
        item {
            InfoCard("1.4.1 — tylko przypomnienia", listOf(
                "Przypomnienie N min przed, znika o czasie wydarzenia",
                "Koniec z powrotami: brak alarmów punktualnych wcale"
            ))
        }
        item {
            InfoCard("1.4.0 — szkło ink", listOf(
                "Karty w szkle: gradient tonu + pływająca pigułka nawigacji",
                "Poświata primary w tle, ten sam ink w obu motywach"
            ))
        }
        item {
            InfoCard("1.3.0 — finalny test kanału", listOf(
                "Instalacja wprost z apki: Pobierz → Zainstaluj, bez USB",
                "Dowód, że kanał 1.2.2 → dalej działa samodzielnie"
            ))
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
    // Powrót z offline → online: jedno dosprawdzenie także po błędzie, nie pętla.
    LaunchedEffect(online) {
        if (online && (state == UpdateState.Offline || state == UpdateState.Idle || state is UpdateState.Error)) runCheck()
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
