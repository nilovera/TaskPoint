package com.example.apk_mock.ui.tareas


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Tarea
import com.example.apk_mock.ui.components.ProfileMenuButton
import com.example.apk_mock.ui.rutinas.FiltrosDias
import com.example.apk_mock.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TareasScreen(
    viewModel: TareasViewModel,
    userName: String,
    onNavigateToCrear: () -> Unit,
    onProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    innerPadding: PaddingValues = PaddingValues()
) {
    val listState by viewModel.listState.collectAsState()
    val tareas = viewModel.tareasFiltradas()
    val canCreateTask = listState.rutinasDisponibles > 0
    val today = LocalDate.now()

    LaunchedEffect(Unit) { viewModel.refreshTareas() }

    // Scaffold propio: maneja el FAB
    // Recibe innerPadding del Scaffold externo (bottom bar) sin duplicar
    Scaffold(
        containerColor = BackgroundDark,
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (canCreateTask) AccentBlue else Color(0xFF4E5562))
                    .clickable(enabled = canCreateTask, onClick = onNavigateToCrear)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Nueva tarea +",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { selfPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = selfPadding.calculateBottomPadding(),
                    start = 20.dp,
                    end = 20.dp
                )
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                   Text(text = "Mis tareas",
                       color = Color.White,
                       fontSize = 28.sp,
                       fontWeight = FontWeight.Bold)
                }
                ProfileMenuButton(
                    userName = userName,
                    onProfile = onProfile,
                    onLogout = onLogout
                )
            }

            Spacer(Modifier.height(16.dp))
            FiltrosDias(seleccionado = listState.filtroDia, onSelect = { viewModel.onFiltroDia(it) })
            Spacer(Modifier.height(12.dp))

            if (tareas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceField)
                        .padding(vertical = 48.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No tenes tareas\ncargadas.",
                            color = SubtitleGray, fontSize = 15.sp, lineHeight = 22.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = onNavigateToCrear,
                            enabled = canCreateTask,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentBlue.copy(alpha = 0.25f),
                                disabledContainerColor = Color(0xFF59606E),
                                disabledContentColor = Color.White
                            )
                        ) {
                            Text(
                                "Crear tarea \u2197",
                                color = if (canCreateTask) AccentBlue else Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                val agrupadas = tareas.groupBy { it.dia }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    agrupadas.forEach { (dia, tareasDelDia) ->
                        item {
                            Text(
                                diaLabel(dia, today),
                                color = AccentBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                            )
                        }
                        items(tareasDelDia) { tarea ->
                            TareaCard(tarea = tarea)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

private fun diaLabel(dia: DiaSemana?, today: LocalDate): String {
    if (dia == null) return "Sin día asignado"

    val targetDate = today.nextOccurrenceOf(dia.toDayOfWeek())
    val monthName = targetDate.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-AR"))
    val prefix = when (targetDate) {
        today -> "Hoy · "
        today.plusDays(1) -> "Mañana · "
        else -> ""
    }

    return "$prefix${dia.displayName()} ${targetDate.dayOfMonth} de $monthName"
}

private fun LocalDate.nextOccurrenceOf(dayOfWeek: DayOfWeek): LocalDate {
    val daysUntil = (dayOfWeek.value - this.dayOfWeek.value + 7) % 7
    return plusDays(daysUntil.toLong())
}

private fun DiaSemana.toDayOfWeek(): DayOfWeek = when (this) {
    DiaSemana.LUN -> DayOfWeek.MONDAY
    DiaSemana.MAR -> DayOfWeek.TUESDAY
    DiaSemana.MIE -> DayOfWeek.WEDNESDAY
    DiaSemana.JUE -> DayOfWeek.THURSDAY
    DiaSemana.VIE -> DayOfWeek.FRIDAY
    DiaSemana.SAB -> DayOfWeek.SATURDAY
    DiaSemana.DOM -> DayOfWeek.SUNDAY
}

private fun DiaSemana.displayName(): String = when (this) {
    DiaSemana.LUN -> "Lunes"
    DiaSemana.MAR -> "Martes"
    DiaSemana.MIE -> "Miércoles"
    DiaSemana.JUE -> "Jueves"
    DiaSemana.VIE -> "Viernes"
    DiaSemana.SAB -> "Sábado"
    DiaSemana.DOM -> "Domingo"
}

@Composable
fun TareaCard(tarea: Tarea) {
    val catColor = categoriaColor(tarea.categoria)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceField,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 62.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    tarea.titulo,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (tarea.horario != null || tarea.rutinaNombre != null) {
                    Text(
                        listOfNotNull(tarea.horario, tarea.rutinaNombre).joinToString(" | "),
                        color = SubtitleGray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Surface(shape = RoundedCornerShape(5.dp), color = catColor.copy(alpha = 0.18f)) {
                Text(
                    tarea.categoria.label,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize = 10.sp, color = catColor, fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

private fun categoriaColor(cat: CategoriaTarea): Color = when (cat) {
    CategoriaTarea.PERSONAL     -> Color(0xFF4D6BFE)
    CategoriaTarea.SUPERMERCADO -> Color(0xFF34C759)
    CategoriaTarea.INDUMENTARIA -> Color(0xFFFF9F0A)
    CategoriaTarea.FACULTAD     -> Color(0xFF8B5CF6)
    CategoriaTarea.ESTUDIO      -> Color(0xFF06B6D4)
}
