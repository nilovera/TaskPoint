package com.example.taskpoint

import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.ui.utils.daysFrom
import com.example.apk_mock.ui.utils.toDiaSemana
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DateFormattersTest {

    @Test
    fun localDateMapsToDiaSemana() {
        assertEquals(DiaSemana.LUN, LocalDate.of(2026, 6, 29).toDiaSemana())
        assertEquals(DiaSemana.DOM, LocalDate.of(2026, 6, 28).toDiaSemana())
    }

    @Test
    fun daysFromWrapsToNextWeek() {
        assertEquals(0, DiaSemana.LUN.daysFrom(DiaSemana.LUN))
        assertEquals(1, DiaSemana.MAR.daysFrom(DiaSemana.LUN))
        assertEquals(1, DiaSemana.LUN.daysFrom(DiaSemana.DOM))
    }
}
