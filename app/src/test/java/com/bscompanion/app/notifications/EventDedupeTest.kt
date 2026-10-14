package com.bscompanion.app.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EventDedupeTest {

    @Test
    fun duplicateWindow_suppressesRapidRedelivery() {
        val now = 1_700_000_000_000L
        // Nigdy nie odpalony — pokazuj.
        assertFalse(EventReceiver.isDuplicateDelivery(0L, now))
        // Stary znacznik poza oknem — pokazuj.
        assertFalse(EventReceiver.isDuplicateDelivery(now - 11 * 60_000L, now))
        // Świeży znacznik w oknie — duplikat, pomiń.
        assertTrue(EventReceiver.isDuplicateDelivery(now - 60_000L, now))
        assertTrue(EventReceiver.isDuplicateDelivery(now, now))
        // Cofnięty zegar — nie tłumimy (użytkownik ma zobaczyć alarm).
        assertFalse(EventReceiver.isDuplicateDelivery(now + 60_000L, now))
    }

    @Test
    fun stableSlot_isStableAndInSafeRange() {
        val a = NotificationHelper.stableSlotId("2026-10-12/nauka")
        val b = NotificationHelper.stableSlotId("2026-10-12/nauka")
        assertEquals(a, b)
        assertTrue(a in 100_000..100_100_000)
        assertTrue(NotificationHelper.stableSlotId("2026-10-13/nauka") in 100_000..100_100_000)
        assertTrue(NotificationHelper.stableSlotId("2026-10-12/kolacja") in 100_000..100_100_000)
        // Poza zarezerwowanymi: trwałe 1001, test 9999, fallback 50001.
        assertTrue(a != 1001 && a != 9999 && a != 50_001)
    }

    @Test
    fun eventKey_parsedFromDataString() {
        assertEquals(
            "2026-10-12/nauka",
            EventReceiver.eventKeyFromDataString("bs://event/2026-10-12/nauka/30/true")
        )
        assertEquals(
            "2026-10-12/nauka",
            EventReceiver.eventKeyFromDataString("bs://event/2026-10-12/nauka/0/false")
        )
        assertNull(EventReceiver.eventKeyFromDataString(null))
        assertNull(EventReceiver.eventKeyFromDataString("bs://event/garbage"))
        assertNull(EventReceiver.eventKeyFromDataString("not-a-uri"))
        assertNull(EventReceiver.eventKeyFromDataString("bs://event/2026-10-12//30/true"))
    }

    @Test
    fun coalesce_mergesSameMinuteDifferentEvents() {
        val now = 1_700_000_000_000L
        val start = now + 1_800_000L
        // Dwa różne wydarzenia o tej samej minucie, chwilę po pierwszym — doklej.
        assertTrue(EventReceiver.shouldCoalesce(
            now - 60_000L, start, "2026-10-14/powrot",
            now, start, "2026-10-14/toaleta"
        ))
        // To samo wystąpienie — nigdy (slot sam nadpisuje).
        assertFalse(EventReceiver.shouldCoalesce(
            now - 60_000L, start, "2026-10-14/powrot",
            now, start, "2026-10-14/powrot"
        ))
        // Za stary poprzednik — osobny wiersz.
        assertFalse(EventReceiver.shouldCoalesce(
            now - 300_000L, start, "2026-10-14/powrot",
            now, start, "2026-10-14/toaleta"
        ))
        // Starty daleko od siebie — osobny wiersz.
        assertFalse(EventReceiver.shouldCoalesce(
            now - 60_000L, start, "2026-10-14/powrot",
            now, start + 3_600_000L, "2026-10-14/nauka"
        ))
    }
}
