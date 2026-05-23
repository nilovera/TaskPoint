package com.example.apk_mock.ui.tareas


import androidx.compose.foundation.background
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
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TareasScreen(
    viewModel: TareasViewModel,
    userName: String,
    onNavigateToCrear: () -> Unit,
    innerPadding: PaddingValues = PaddingValues()
) {
    val listState by viewModel.listState.collectAsState()
    val tareas = viewModel.tareasFiltradas()
    val today = LocalDate.now()
    val monthName = today.month.getDisplayName(TextStyle.FULL, Locale("es", "AR"))
    val dateLabel = "${today.dayOfMonth} de $monthName · ${today.year}"

    LaunchedEffect(Unit) { viewModel.refreshTareas() }

    // Scaffold propio: maneja el FAB
    // Recibe innerPadding del Scaffold externo (bottom bar) sin duplicar
    Scaffold(
        containerColor = BackgroundDark,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCrear,
                containerColor = AccentBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(50)
            ) {
                Text("Nueva tarea +", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(SurfaceField),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Perfil", tint = SubtitleGray, modifier = Modifier.size(26.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

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
                            "No tenés tareas\nel día de hoy.",
                            color = SubtitleGray, fontSize = 15.sp, lineHeight = 22.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = onNavigateToCrear,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue.copy(alpha = 0.25f))
                        ) {
                            Text("Crear tarea ↗", color = AccentBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                FiltrosDias(seleccionado = listState.filtroDia, onSelect = { viewModel.onFiltroDia(it) })
                Spacer(Modifier.height(16.dp))
                val agrupadas = tareas.groupBy { it.dia }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    agrupadas.forEach { (dia, tareasDelDia) ->
                        item {
                            Text(
                                diaLabel(dia, today),
                                color = SubtitleGray, fontSize = 13.sp, fontWeight = FontWeight.Medium,
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

@Composable
fun TareaCard(tarea: Tarea) {
    val catColor = categoriaColor(tarea.categoria)
    Surface(shape = RoundedCornerShape(14.dp), color = SurfaceField, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = Color.Transparent, border = ButtonDefaults.outlinedButtonBorder) {
                Spacer(Modifier.size(22.dp))
            }
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

private fun categoriaColor(cat: CategoriaTarea): Color = when (cat) {
    CategoriaTarea.PERSONAL     -> Color(0xFF4D6BFE)
    CategoriaTarea.SUPERMERCADO -> Color(0xFF34C759)
    CategoriaTarea.INDUMENTARIA -> Color(0xFFFF9F0A)
    CategoriaTarea.FACULTAD     -> Color(0xFF8B5CF6)
    CategoriaTarea.ESTUDIO      -> Color(0xFF06B6D4)
}