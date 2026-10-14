package com.bscompanion.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DormScheduleTest {

    private fun monday(): LocalDate = LocalDate.of(2026, 10, 12) // poniedziałek

    @Test
    fun nextCared_includesEventStartingExactlyNow() {
        val now = LocalDateTime.of(monday(), LocalTime.of(21, 0))
        val hit = DormSchedule.nextCaredEvent(now)
        assertNotNull(hit)
        // 21:00: powrot i toaleta — pierwszy w sortowaniu to powrot
        assertEquals(LocalTime.of(21, 0), hit!!.second.start)
    }

    @Test
    fun upcomingCared_keepsBoth21oclockEvents() {
        val from = LocalDateTime.of(monday(), LocalTime.of(20, 59))
        val ids = DormSchedule.upcomingCared(from, 4).map { it.second.id }
        assertTrue(ids.contains("powrot"))
        assertTrue(ids.contains("toaleta"))
    }

    @Test
    fun isNoCare_boundaries() {
        // Sobota cała
        assertTrue(DormSchedule.isNoCare(LocalDateTime.of(LocalDate.of(2026, 10, 17), LocalTime.of(12, 0))))
        // Ndz 15:59 brak, 16:00 opieka
        assertTrue(DormSchedule.isNoCare(LocalDateTime.of(LocalDate.of(2026, 10, 18), LocalTime.of(15, 59))))
        assertTrue(!DormSchedule.isNoCare(LocalDateTime.of(LocalDate.of(2026, 10, 18), LocalTime.of(16, 0))))
        // Pt 17:59 opieka, 18:00 brak
        assertTrue(!DormSchedule.isNoCare(LocalDateTime.of(LocalDate.of(2026, 10, 16), LocalTime.of(17, 59))))
        assertTrue(DormSchedule.isNoCare(LocalDateTime.of(LocalDate.of(2026, 10, 16), LocalTime.of(18, 0))))
    }

    @Test
    fun currentEvent_overnightCisza() {
        val events = DormSchedule.eventsForDate(monday())
        assertEquals("cisza", DormSchedule.currentEvent(LocalTime.of(23, 0), events)?.id)
        assertEquals("cisza", DormSchedule.currentEvent(LocalTime.of(2, 0), events)?.id)
        assertEquals("pobudka", DormSchedule.currentEvent(LocalTime.of(6, 30), events)?.id)
    }

    @Test
    fun gapsAreExplicitCzasWolny() {
        val events = DormSchedule.eventsForDate(monday())
        assertEquals("czas_wolny_2", DormSchedule.currentEvent(LocalTime.of(6, 50), events)?.id)
        assertEquals("czas_wolny_4", DormSchedule.currentEvent(LocalTime.of(12, 0), events)?.id)
        assertEquals("czas_wolny_5", DormSchedule.currentEvent(LocalTime.of(21, 50), events)?.id)
        // Bez alarmów: luki nie budzą.
        assertTrue(events.filter { it.id.startsWith("czas_wolny_") }.all { !it.notifyDefault })
    }

    @Test
    fun nextCared_skipsWeekendGap() {
        // Pt 18:30 (po zamknięciu) → następne to ndz 16:00+, czyli ndz nauka 17:30.
        val fridayEvening = LocalDateTime.of(LocalDate.of(2026, 10, 16), LocalTime.of(18, 30))
        val hit = DormSchedule.nextCaredEvent(fridayEvening)
        assertNotNull(hit)
        // Nie sobota — konkretnie niedzielna nauka (plan jest deterministyczny).
        assertEquals(LocalDate.of(2026, 10, 18), hit!!.first)
        assertEquals("nauka", hit.second.id)
    }

    @Test
    fun lookahead_respectsCount() {
        val from = LocalDateTime.of(monday(), LocalTime.of(6, 0))
        val list = DormSchedule.upcomingCared(from, 2)
        assertEquals(2, list.size)
        // Posortowane po (data, godzina): pełny porządek leksykograficzny.
        val firstDt = LocalDateTime.of(list[0].first, list[0].second.start)
        val secondDt = LocalDateTime.of(list[1].first, list[1].second.start)
        assertTrue(firstDt <= secondDt)
    }

    @Test
    fun sunday2200_ciszaIsCared() {
        // Ndz 22:00 ma opiekę (ndz od 16:00 czynne) — regresja na end-date bug.
        val sunday2200 = LocalDateTime.of(LocalDate.of(2026, 10, 18), LocalTime.of(22, 0))
        assertTrue(!DormSchedule.isNoCare(sunday2200))
        val hit = DormSchedule.nextCaredEvent(LocalDateTime.of(LocalDate.of(2026, 10, 18), LocalTime.of(21, 50)))
        assertNotNull(hit)
        assertNull(hit!!.takeIf { it.second.id == "cisza" && DormSchedule.isNoCare(LocalDateTime.of(it.first, it.second.start)) })
    }

    @Test
    fun isCurfew_boundaries() {
        // Godzina policyjna 21:00–07:00 (włącznie 21:00, wyłącznie 07:00).
        assertTrue(DormSchedule.isCurfew(LocalTime.of(21, 0)))
        assertTrue(DormSchedule.isCurfew(LocalTime.of(23, 59)))
        assertTrue(DormSchedule.isCurfew(LocalTime.of(0, 0)))
        assertTrue(DormSchedule.isCurfew(LocalTime.of(6, 59)))
        assertTrue(!DormSchedule.isCurfew(LocalTime.of(7, 0)))
        assertTrue(!DormSchedule.isCurfew(LocalTime.of(12, 0)))
        assertTrue(!DormSchedule.isCurfew(LocalTime.of(20, 59)))
    }
}
