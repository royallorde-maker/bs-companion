package com.bscompanion.app.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class EventCategory(val label: String) {
    WAKEUP("Pobudka"),
    MEAL("Posiłek"),
    STUDY("Nauka"),
    QUIET("Cisza nocna"),
    DUTY("Dyżur"),
    FREE("Czas wolny"),
    ORG("Organizacyjne")
}

data class DormEvent(
    val id: String,
    val title: String,
    val description: String,
    val start: LocalTime,
    val end: LocalTime?,
    val category: EventCategory,
    val notifyDefault: Boolean = true
)

/**
 * Porządek dnia na podstawie Załącznika nr 6 Regulaminu Internatu ZST Szczecinek
 * + godziny posiłków z Załącznika nr 3.
 */
object DormSchedule {

    // LISTA DNIA (kolejność chronologiczna nie jest wymagana — sortujemy w eventsForDate).
    // end = null znaczy: wydarzenie punktowe (UI pokazuje 15 min "obecności").
    private val weekdayEvents = listOf(
        DormEvent(
            "pobudka", "Pobudka",
            "Pobudka 6:30. Toaleta poranna i porządki w pokojach do 7:15.",
            LocalTime.of(6, 30), LocalTime.of(6, 45),
            EventCategory.WAKEUP
        ),
        DormEvent(
            "sniadanie", "Śniadanie",
            "Stołówka 7:00–7:50. Po posiłku odnieś naczynia, zostaw porządek.",
            LocalTime.of(7, 0), LocalTime.of(7, 50),
            EventCategory.MEAL
        ),
        DormEvent(
            "wyjscie", "Wyjście do szkoły",
            "Do 7:55 wyjdź na zajęcia i zostaw klucze u wychowawcy.",
            LocalTime.of(7, 55), null,
            EventCategory.ORG
        ),
        DormEvent(
            "obiad", "Obiad",
            "Obiad 13:30–15:30.",
            LocalTime.of(13, 30), LocalTime.of(15, 30),
            EventCategory.MEAL
        ),
        DormEvent(
            "czas_wolny_1", "Czas wolny",
            "Odpoczynek, prace na rzecz internatu. Możliwość wyjścia z internatu.",
            LocalTime.of(15, 30), LocalTime.of(17, 30),
            EventCategory.FREE, notifyDefault = false
        ),
        DormEvent(
            "nauka", "Obowiązkowa nauka własna",
            "17:30–18:30 we własnym pokoju lub cichej nauki. Cisza, bez telefonów i gości.",
            LocalTime.of(17, 30), LocalTime.of(18, 30),
            EventCategory.STUDY
        ),
        DormEvent(
            "kolacja", "Kolacja",
            "Stołówka 18:30–19:30 (bez dni wyjazdów).",
            LocalTime.of(18, 30), LocalTime.of(19, 30),
            EventCategory.MEAL
        ),
        DormEvent(
            "zajecia_wych", "Czas wolny / zajęcia z wychowawcą",
            "19:30–21:00.",
            LocalTime.of(19, 30), LocalTime.of(21, 0),
            EventCategory.FREE, notifyDefault = false
        ),
        DormEvent(
            "powrot", "Powrót do internatu",
            "Wróć do 21:00. Po weekendzie powrót do 21:00 (21:00–22:00 tylko po uprzedzeniu).",
            LocalTime.of(21, 0), null,
            EventCategory.ORG
        ),
        DormEvent(
            "toaleta", "Toaleta wieczorna / kąpiel",
            "Kąpiel do 21:45.",
            LocalTime.of(21, 0), LocalTime.of(21, 45),
            EventCategory.ORG
        ),
        // Luki między punktami jako jawny Czas wolny (FREE, bez alarmów):
        // TERAZ podświetla je w Planie zamiast pustego "Przerwa".
        DormEvent(
            "czas_wolny_6", "Czas wolny",
            "Spokój przed pobudką.",
            LocalTime.of(6, 0), LocalTime.of(6, 30),
            EventCategory.FREE, notifyDefault = false
        ),
        DormEvent(
            "czas_wolny_2", "Czas wolny",
            "Między pobudką a śniadaniem.",
            LocalTime.of(6, 45), LocalTime.of(7, 0),
            EventCategory.FREE, notifyDefault = false
        ),
        DormEvent(
            "czas_wolny_3", "Czas wolny",
            "Chwila między śniadaniem a wyjściem.",
            LocalTime.of(7, 50), LocalTime.of(7, 55),
            EventCategory.FREE, notifyDefault = false
        ),
        DormEvent(
            "czas_wolny_4", "Czas wolny",
            "Lekcje w szkole.",
            LocalTime.of(8, 10), LocalTime.of(13, 30),
            EventCategory.FREE, notifyDefault = false
        ),
        DormEvent(
            "czas_wolny_5", "Czas wolny",
            "Chwila przed ciszą nocną.",
            LocalTime.of(21, 45), LocalTime.of(22, 0),
            EventCategory.FREE, notifyDefault = false
        ),
        DormEvent(
            "cisza", "Cisza nocna 22:00–6:00",
            "Od 22:00 przebywaj we własnym pokoju. Zakaz rozmów telefonicznych, drzwi wejściowe zamknięte.",
            LocalTime.of(22, 0), LocalTime.of(6, 0),
            EventCategory.QUIET
        )
    )

    fun eventsForDate(date: LocalDate): List<DormEvent> {
        // Obiad codziennie 13:30–15:30, reszta planu jak w Zał. nr 6.
        // UWAGA: zwraca pełny plan także w dni bez opieki — filtrowanie
        // po isNoCare robią: EventScheduler (alarmy) i UI (plansza/licznik).
        return weekdayEvents.sortedBy { it.start }
    }

    /**
     * Precyzyjne godziny bez opieki: sobota cała, niedziela do 16:00,
     * piątek od 18:00 (internat czynny ndz 16:00 – pt 18:00).
     */
    fun isNoCare(dateTime: LocalDateTime): Boolean {
        return when (dateTime.dayOfWeek) {
            DayOfWeek.SATURDAY -> true
            DayOfWeek.SUNDAY -> dateTime.toLocalTime() < LocalTime.of(16, 0)
            DayOfWeek.FRIDAY -> dateTime.toLocalTime() >= LocalTime.of(18, 0)
            else -> false
        }
    }

    /** Cały dzień kalendarzowy bez ani jednej godziny opieki (tylko sobota). */
    fun isFullyClosedDay(date: LocalDate): Boolean {
        return isNoCare(LocalDateTime.of(date, LocalTime.of(12, 0))) &&
            isNoCare(LocalDateTime.of(date, LocalTime.of(20, 0)))
    }

    /** Baner „Brak opieki": prawda, gdy TERAZ nie ma opieki (nd rano, pt wieczór, sobota). */
    fun isClosedNow(now: LocalDateTime = LocalDateTime.now()): Boolean = isNoCare(now)

    /**
     * Godzina policyjna 21:00–07:00 (powrót do internatu → pobudka).
     * Czysta logika czasu — żadnego UI tu nie ma (winieta usunięta w 1.6.0).
     */
    fun isCurfew(time: LocalTime): Boolean =
        !time.isBefore(LocalTime.of(21, 0)) || time.isBefore(LocalTime.of(7, 0))

    fun isCurfew(now: LocalDateTime): Boolean = isCurfew(now.toLocalTime())

    /**
     * Najbliższe wydarzenie Z OPIEKĄ od danego momentu (szuka do przodu,
     * pomijając dni/godziny bez opieki). Zwraca parę (data, event).
     * Cisza nocna jest pełnoprawnym punktem z opieką (alarm!) — wliczamy ją
     * także dla dni przyszłych (22:00 sortuje się ostatnia, więc nie zasłania dni).
     */
    fun nextCaredEvent(now: LocalDateTime, lookaheadDays: Int = 8): Pair<LocalDate, DormEvent>? {
        val today = now.toLocalDate()
        // Minimum 8 dni: krótszy horyzont (np. 1) z pt wieczora nie sięgałby
        // za weekendową dziurę i zwracał null mimo wydarzeń w ndz/pn.
        val days = maxOf(8, lookaheadDays)
        val fromTruncated = now.truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
        for (d in 0 until days) {
            val date = today.plusDays(d.toLong())
            val from = if (d == 0) fromTruncated.toLocalTime() else LocalTime.MIN
            val evs = eventsForDate(date)
            // Wszystkie nadchodzące danego dnia (łącznie z ciszą) — pierwszy z opieką.
            // >= (nie >): wydarzenie startujące dokładnie teraz nie może być pominięte.
            // Obcięcie do minut: wołanie z 12:00:00.5 nie gubi eventu 12:00:00.
            val upcoming = evs.filter { !it.start.isBefore(from) }
                .sortedBy { it.start }
            for (n in upcoming) {
                if (isNoCare(LocalDateTime.of(date, n.start))) continue
                // Start poza opieką już odfiltrowany wyżej — tu jeszcze koniec:
                // wydarzenie zahaczające o zamknięcie (np. pt 17:30–18:30) odpada,
                // tak jak w upcomingCared() i EventScheduler.rescheduleAll().
                val end = n.end
                if (end != null) {
                    val endDate = if (end < n.start) date.plusDays(1) else date
                    if (isNoCare(LocalDateTime.of(endDate, end).minusMinutes(1))) continue
                }
                return date to n
            }
        }
        return null
    }

    /**
     * Kolejne N wydarzeń z opieką (oś czasu "Nadchodzące").
     * Zbiera wszystkie punkty z horyzontu i sortuje — dzięki temu dwa wydarzenia
     * o tej samej godzinie (powrot 21:00 + toaleta 21:00-21:45) nie gubią się,
     * w przeciwieństwie do kursora start+1min. Cisza nocna wliczana także dla
     * dni przyszłych (ma alarm notifyDefault) — sortuje się na koniec dnia.
     */
    fun upcomingCared(from: LocalDateTime, count: Int): List<Pair<LocalDate, DormEvent>> {
        val out = mutableListOf<Pair<LocalDate, DormEvent>>()
        val today = from.toLocalDate()
        val fromTruncated = from.truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
        for (d in 0 until 10) {
            if (out.size >= count) break
            val date = today.plusDays(d.toLong())
            val fromTime = if (d == 0) fromTruncated.toLocalTime() else LocalTime.MIN
            for (e in eventsForDate(date)) {
                if (d == 0 && e.start.isBefore(fromTime)) continue
                if (isNoCare(LocalDateTime.of(date, e.start))) continue
                val end = e.end
                if (end != null) {
                    val endDate = if (end < e.start) date.plusDays(1) else date
                    if (isNoCare(LocalDateTime.of(endDate, end).minusMinutes(1))) continue
                }
                out += date to e
            }
        }
        return out.sortedWith(compareBy({ it.first }, { it.second.start }, { it.second.id })).take(count)
    }

    private fun spanSeconds(e: DormEvent): Int {
        val end = e.end ?: return 15 * 60
        val s = e.start.toSecondOfDay()
        val en = end.toSecondOfDay()
        val d = (en - s + 24 * 3600) % (24 * 3600)
        return if (d == 0) 15 * 60 else d
    }

    /** Event trwający teraz (obsługuje ciszę nocną przez północ). */
    fun currentEvent(now: LocalTime, events: List<DormEvent>): DormEvent? {
        // Cisza nocna: 22:00–24:00 lub 00:00–06:00
        if (!now.isBefore(LocalTime.of(22, 0)) || now.isBefore(LocalTime.of(6, 0))) {
            return events.firstOrNull { it.id == "cisza" }
        }
        // Przy nakładających się pozycjach wygrywa najbardziej konkretna (najkrótsza).
        return events.filter { e ->
            val end = e.end ?: e.start.plusMinutes(15)
            !now.isBefore(e.start) && now.isBefore(end)
        }.minByOrNull { spanSeconds(it) }
    }

    /**
     * Bezpieczna wersja dla UI: null w godzinach bez opieki (sobota, nd<16:00,
     * pt≥18:00) zamiast ciszy z planu. Zapobiega pokazaniu „TERAZ: cisza"
     * w zamkniętym internacie, gdy wołający zapomni o isNoCare.
     */
    fun currentCaredEvent(now: LocalDateTime, events: List<DormEvent>): DormEvent? {
        if (isNoCare(now)) return null
        return currentEvent(now.toLocalTime(), events)
    }
}
