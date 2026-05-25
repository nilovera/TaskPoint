package com.example.apk_mock.ui.tareas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.apk_mock.ui.theme.AccentBlue
import com.example.apk_mock.ui.theme.BackgroundDark
import com.example.apk_mock.ui.theme.DateBlue
import com.example.apk_mock.ui.theme.StrengthGreen
import com.example.apk_mock.ui.theme.SubtitleGray
import com.example.apk_mock.ui.theme.SurfaceField
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun TareasScreen(
    viewModel: TareasViewModel,
    userName: String,
    onNavigateToCrear: () -> Unit,
    onProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToDetalle: (String) -> Unit = {},
    showTaskCreatedMessage: Boolean = false,
    onTaskCreatedMessageShown: () -> Unit = {},
    showTaskDeletedMessage: Boolean = false,
    onTaskDeletedMessageShown: () -> Unit = {},
    innerPadding: PaddingValues = PaddingValues()
) {
    val listState by viewModel.listState.collectAsState()
    val tareas = viewModel.tareasFiltradas()
    val canCreateTask = listState.rutinasDisponibles > 0
    val today = LocalDate.now()
    var overlayMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.refreshTareas() }
    LaunchedEffect(showTaskCreatedMessage) {
        if (showTaskCreatedMessage) {
            overlayMessage = "Tarea creada correctamente."
            delay(3000)
            overlayMessage = null
            onTaskCreatedMessageShown()
        }
    }
    LaunchedEffect(showTaskDeletedMessage) {
        if (showTaskDeletedMessage) {
            overlayMessage = "Tarea eliminada correctamente."
            delay(3000)
            overlayMessage = null
            onTaskDeletedMessageShown()
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        floatingActionButton = {
            if (overlayMessage == null) {
                Box(
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding() + 8.dp)
                ) {
                    Button(
                        onClick = onNavigateToCrear,
                        enabled = canCreateTask,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF4E5562),
                            disabledContentColor = Color.White
                        ),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Nueva tarea +", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { selfPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding() + 8.dp,
                        bottom = innerPadding.calculateBottomPadding() + selfPadding.calculateBottomPadding(),
                        start = 20.dp,
                        end = 20.dp
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mis tareas",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                    EmptyTasksBlock(
                        canCreateTask = canCreateTask,
                        onNavigateToCrear = onNavigateToCrear
                    )
                } else {
                    val agrupadas = tareas
                        .groupBy { it.dia }
                        .toList()
                        .sortedWith(
                            compareBy<Pair<DiaSemana?, List<Tarea>>> { (dia, _) ->
                                dia?.daysFrom(today.toDiaSemana()) ?: Int.MAX_VALUE
                            }.thenBy { (dia, _) -> dia?.ordinal ?: Int.MAX_VALUE }
                        )

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        agrupadas.forEach { (dia, tareasDelDia) ->
                            item {
                                Text(
                                    diaLabel(dia, today),
                                    color = DateBlue,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                                )
                            }
                            items(tareasDelDia.sortedBy { it.horario ?: "" }) { tarea ->
                                TareaCard(
                                    tarea = tarea,
                                    onClick = { onNavigateToDetalle(tarea.id) }
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }

            if (overlayMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StrengthGreen,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(
                            start = 20.dp,
                            end = 20.dp,
                            bottom = innerPadding.calculateBottomPadding() + 16.dp
                        )
                        .height(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            overlayMessage.orEmpty(),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTasksBlock(
    canCreateTask: Boolean,
    onNavigateToCrear: () -> Unit
) {
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
                color = SubtitleGray,
                fontSize = 15.sp,
                lineHeight = 22.sp,
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
                    "Crear tarea ↗",
                    color = if (canCreateTask) AccentBlue else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun diaLabel(dia: DiaSemana?, today: LocalDate): String {
    if (dia == null) return "Sin día asignado"

    val offset = dia.daysFrom(today.toDiaSemana())
    val date = today.plusDays(offset.toLong())
    val monthName = date.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-AR"))
    val prefix = when (offset) {
        0 -> "Hoy · "
        1 -> "Mañana · "
        else -> ""
    }

    return "$prefix${dia.displayName()} ${date.dayOfMonth} de $monthName"
}

private fun DiaSemana.daysFrom(today: DiaSemana): Int {
    val days = DiaSemana.values()
    val currentIndex = days.indexOf(today)
    val targetIndex = days.indexOf(this)
    return (targetIndex - currentIndex + days.size) % days.size
}

private fun LocalDate.toDiaSemana(): DiaSemana = when (dayOfWeek) {
    DayOfWeek.MONDAY -> DiaSemana.LUN
    DayOfWeek.TUESDAY -> DiaSemana.MAR
    DayOfWeek.WEDNESDAY -> DiaSemana.MIE
    DayOfWeek.THURSDAY -> DiaSemana.JUE
    DayOfWeek.FRIDAY -> DiaSemana.VIE
    DayOfWeek.SATURDAY -> DiaSemana.SAB
    DayOfWeek.SUNDAY -> DiaSemana.DOM
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
fun TareaCard(
    tarea: Tarea,
    onClick: () -> Unit = {}
) {
    val catColor = categoriaColor(tarea.categoria)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceField,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 62.dp)
            .clickable(onClick = onClick)
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
                    fontSize = 10.sp,
                    color = catColor,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

private fun categoriaColor(cat: CategoriaTarea): Color = when (cat.code) {
    "PERSONAL" -> Color(0xFF4D6BFE)
    "SUPERMERCADO" -> Color(0xFF34C759)
    "INDUMENTARIA" -> Color(0xFFFF9F0A)
    "FACULTAD" -> Color(0xFF8B5CF6)
    "ESTUDIO" -> Color(0xFF06B6D4)
    "FARMACIA", "MEDICO" -> Color(0xFFE85D75)
    "GIMNASIO" -> Color(0xFF34C759)
    "BANCO", "TRANSPORTE" -> Color(0xFF4D6BFE)
    "ESCUELA", "LIBRERIA" -> Color(0xFF8B5CF6)
    "VETERINARIA", "FERRETERIA", "PANADERIA", "PELUQUERIA" -> Color(0xFFFF9F0A)
    "CASA" -> Color(0xFF06B6D4)
    else -> SubtitleGray
}
