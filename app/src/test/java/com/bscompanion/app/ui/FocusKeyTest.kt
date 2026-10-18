package com.bscompanion.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class FocusKeyTest {

    @Test
    fun focusKey_parsesRealEvent() {
        val monday = LocalDate.of(2026, 10, 12)
        val hit = parseFocusKey("2026-10-12|cisza")
        assertEquals(monday, hit?.first)
        assertEquals("cisza", hit?.second?.id)
    }

    @Test
    fun focusKey_returnsNullForUnknownOrMalformed() {
        assertNull(parseFocusKey(""))
        assertNull(parseFocusKey("garbage"))
        assertNull(parseFocusKey("2026-10-12|nie_istnieje"))
        assertNull(parseFocusKey("2026-13-45|cisza"))
    }

    @Test
    fun focusKey_rejectsTrailingJunk() {
        // Klucz to TYLKO "data|id" — bez kośla
        assertNull(parseFocusKey("2026-10-12|cisza|extra"))
    }
}
