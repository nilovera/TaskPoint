package com.example.apk_mock.ui.tareas


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Tarea
import com.example.apk_mock.ui.rutinas.FiltrosDias
import com.example.apk_mock.ui.theme.*
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
    onNavigateToDetalle: (String) -> Unit = {},
    showTaskCreatedMessage: Boolean = false,
    onTaskCreatedMessageShown: () -> Unit = {},
    innerPadding: PaddingValues = PaddingValues()
) {
    val listState by viewModel.listState.collectAsState()
    val tareas = viewModel.tareasFiltradas()
    val canCreateTask = listState.rutinasDisponibles > 0
    val today = LocalDate.now()
    var showCreatedOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.refreshTareas() }
    LaunchedEffect(showTaskCreatedMessage) {
        if (showTaskCreatedMessage) {
            showCreatedOverlay = true
            delay(3000)
            showCreatedOverlay = false
            onTaskCreatedMessageShown()
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        floatingActionButton = {
            if (!showCreatedOverlay) {
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
                        Text(
                            "Nueva tarea +",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(SurfaceField),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Perfil", tint = SubtitleGray, modifier = Modifier.size(26.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            FiltrosDias(seleccionado = listState.filtroDia, onSelect = { viewModel.onFiltroDia(it) })
            Spacer(Modifier.height(20.dp))

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
                                color = DateBlue, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                            )
                        }
                        items(tareasDelDia) { tarea ->
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

            if (showCreatedOverlay) {
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
                            "Tarea creada correctamente.",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

private fun diaLabel(dia: DiaSemana?, today: LocalDate): String {
    val monthName = today.month.getDisplayName(TextStyle.FULL, Locale("es", "AR"))
    return when (dia) {
        DiaSemana.LUN -> "Hoy · Lunes ${today.dayOfMonth} de $monthName"
        DiaSemana.MAR -> "Mañana · Martes ${today.dayOfMonth + 1} de $monthName"
        DiaSemana.MIE -> "Miércoles ${today.dayOfMonth + 2} de $monthName"
        DiaSemana.JUE -> "Jueves ${today.dayOfMonth + 3} de $monthName"
        DiaSemana.VIE -> "Viernes ${today.dayOfMonth + 4} de $monthName"
        DiaSemana.SAB -> "Sábado ${today.dayOfMonth + 5} de $monthName"
        DiaSemana.DOM -> "Domingo ${today.dayOfMonth + 6} de $monthName"
        null -> "Sin día asignado"
    }
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

@Composable
fun TareaCard(
    tarea: Tarea,
    onClick: () -> Unit = {}
) {
    val catColor = categoriaColor(tarea.categoria)
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceField,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tarea.titulo, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                if (tarea.horario != null || tarea.rutinaNombre != null) {
                    Text(
                        listOfNotNull(tarea.horario, tarea.rutinaNombre).joinToString(" | "),
                        color = SubtitleGray, fontSize = 12.sp
                    )
                }
            }
            Surface(shape = RoundedCornerShape(6.dp), color = catColor.copy(alpha = 0.18f)) {
                Text(
                    tarea.categoria.label,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp, color = catColor, fontWeight = FontWeight.SemiBold
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
