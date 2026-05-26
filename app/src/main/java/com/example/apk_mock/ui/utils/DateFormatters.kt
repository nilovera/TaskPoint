package com.example.apk_mock.ui.utils

import com.example.apk_mock.domain.model.DiaSemana
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val SpanishArgentina: Locale = Locale.forLanguageTag("es-AR")

fun LocalDate.homeDateLabel(): String {
    val monthName = month.getDisplayName(TextStyle.FULL, SpanishArgentina)
    return "$dayOfMonth de $monthName · $year"
}

fun LocalDate.toDiaSemana(): DiaSemana = when (dayOfWeek) {
    DayOfWeek.MONDAY -> DiaSemana.LUN
    DayOfWeek.TUESDAY -> DiaSemana.MAR
    DayOfWeek.WEDNESDAY -> DiaSemana.MIE
    DayOfWeek.THURSDAY -> DiaSemana.JUE
    DayOfWeek.FRIDAY -> DiaSemana.VIE
    DayOfWeek.SATURDAY -> DiaSemana.SAB
    DayOfWeek.SUNDAY -> DiaSemana.DOM
}

fun DiaSemana.displayName(): String = when (this) {
    DiaSemana.LUN -> "Lunes"
    DiaSemana.MAR -> "Martes"
    DiaSemana.MIE -> "Miércoles"
    DiaSemana.JUE -> "Jueves"
    DiaSemana.VIE -> "Viernes"
    DiaSemana.SAB -> "Sábado"
    DiaSemana.DOM -> "Domingo"
}

fun DiaSemana.daysFrom(today: DiaSemana): Int {
    val days = DiaSemana.values()
    val currentIndex = days.indexOf(today)
    val targetIndex = days.indexOf(this)
    return (targetIndex - currentIndex + days.size) % days.size
}

fun DiaSemana?.taskSectionLabel(today: LocalDate): String {
    val dia = this ?: return "Sin día asignado"
    val offset = dia.daysFrom(today.toDiaSemana())
    val date = today.plusDays(offset.toLong())
    val monthName = date.month.getDisplayName(TextStyle.FULL, SpanishArgentina)
    val prefix = when (offset) {
        0 -> "Hoy · "
        1 -> "Mañana · "
        else -> ""
    }

    return "$prefix${dia.displayName()} ${date.dayOfMonth} de $monthName"
}
