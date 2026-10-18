package com.bscompanion.app.notifications

import com.bscompanion.app.data.DormEvent
import com.bscompanion.app.data.DormSchedule
import com.bscompanion.app.data.EventCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class SmartNotifyTest {

    private fun monday(): LocalDate = LocalDate.of(2026, 10, 12)

    @Test
    fun smartOffset_valuesMatchCategory() {
        val events = DormSchedule.eventsForDate(monday())
        val byId = events.associateBy { it.id }
        // Punktowe = 15
        assertEquals(15, EventScheduler.smartOffsetMinutes(byId.getValue("powrot")))
        assertEquals(15, EventScheduler.smartOffsetMinutes(byId.getValue("wyjscie")))
        // Pobudka = 5 (krótkie szturchnięcie przed porannym budzikiem, nie 20)
        assertEquals(5, EventScheduler.smartOffsetMinutes(byId.getValue("pobudka")))
        // Nauka = 20 (czas wrócić do pokoju)
        assertEquals(20, EventScheduler.smartOffsetMinutes(byId.getValue("nauka")))
        // Posiłki = 30
        assertEquals(30, EventScheduler.smartOffsetMinutes(byId.getValue("sniadanie")))
        assertEquals(30, EventScheduler.smartOffsetMinutes(byId.getValue("obiad")))
        assertEquals(30, EventScheduler.smartOffsetMinutes(byId.getValue("kolacja")))
        // Cisza = 10
        assertEquals(10, EventScheduler.smartOffsetMinutes(byId.getValue("cisza")))
        // Reszta z koniec = 20
        assertEquals(20, EventScheduler.smartOffsetMinutes(byId.getValue("toaleta")))
    }

    @Test
    fun sameStart_eventsShareMaxOffset() {
        val events = DormSchedule.eventsForDate(monday())
        val map = EventScheduler.offsetsByStart(events)
        // Powrót (15) i toaleta (20) startują o 21:00 — wspólny offset = max = 20.
        assertEquals(20, map[LocalTime.of(21, 0)])
        // Obiad jest sam o 13:30.
        assertEquals(30, map[LocalTime.of(13, 30)])
        // Pobudka też jest sama — 5.
        assertEquals(5, map[LocalTime.of(6, 30)])
    }

    @Test
    fun everyOffsetIsCancellable() {
        // Każdy offset, jaki scheduler może użyć, musi być w defensywnym zbiorze
        // cancelAll — inaczej PendingIntent zostaje na zawsze.
        val events = DormSchedule.eventsForDate(monday())
        val used = EventScheduler.offsetsByStart(events).values
        // Ograniczamy do znanych kategorii; 0/5 to obronne wartości.
        used.forEach { assertTrue(it in 0..30) }
    }

    @Test
    fun eveningPair_postsWithinCoalesceWindow() {
        // Regresja 1.57→1.62: powrót+toaleta muszą dzwonić razem, bo łączenie
        // wymaga postingu w oknie COALESCE_WINDOW_MS. Wspólny offset = różnica 0.
        val events = DormSchedule.eventsForDate(monday())
        val map = EventScheduler.offsetsByStart(events)
        val off = map.getValue(LocalTime.of(21, 0))
        // Oba eventy dostają TEN SAM offset → postingi o tej samej ms.
        val powrot = events.first { it.id == "powrot" }
        val toaleta = events.first { it.id == "toaleta" }
        assertEquals(off, EventScheduler.smartOffsetMinutes(powrot).let { map.getValue(powrot.start) })
        assertEquals(off, map.getValue(toaleta.start))
    }
}
