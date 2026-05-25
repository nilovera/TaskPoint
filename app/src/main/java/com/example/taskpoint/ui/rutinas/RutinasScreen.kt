package com.example.taskpoint.ui.rutinas

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
import com.example.taskpoint.domain.model.DiaSemana
import com.example.taskpoint.domain.model.Rutina
import com.example.taskpoint.ui.theme.*

@Composable
fun RutinasScreen(
    viewModel: RutinasViewModel,
    userName: String,
    onNavigateToCrear: () -> Unit,
    innerPadding: PaddingValues = PaddingValues()
) {
    val listState by viewModel.listState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.refreshRutinas() }

    LaunchedEffect(listState.snackbarMessage) {
        listState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeSnackbar()
        }
    }

    val rutinas = viewModel.rutinasFiltradas()

    // Scaffold propio: maneja snackbar y FAB
    // Recibe innerPadding del Scaffold externo (bottom bar) sin duplicar
    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    containerColor = StrengthGreen,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(data.visuals.message, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    ) { selfPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    // top: padding del Scaffold externo + margen visual
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    // bottom: el Scaffold propio calcula el espacio del snackbar/FAB
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
                Text(
                    text = "Mis rutinas",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SurfaceField),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint = SubtitleGray,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            if (rutinas.isEmpty()) {
                // ── Empty state ────────────────────────────────────────────────
                Spacer(Modifier.height(12.dp))
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
                            "No tenés rutinas\ncreadas.",
                            color = SubtitleGray,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = onNavigateToCrear,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentBlue.copy(alpha = 0.25f)
                            )
                        ) {
                            Text(
                                "Crear rutina ↗",
                                color = AccentBlue,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                // ── Filtros días ───────────────────────────────────────────────
                Spacer(Modifier.height(12.dp))
                FiltrosDias(
                    seleccionado = listState.filtrodia,
                    onSelect = { viewModel.onFiltroDia(it) }
                )
                Spacer(Modifier.height(16.dp))

                // ── Lista rutinas ──────────────────────────────────────────────
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(rutinas) { rutina -> RutinaCard(rutina = rutina) }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Componentes reutilizables ─────────────────────────────────────────────────

@Composable
fun FiltrosDias(seleccionado: DiaSemana?, onSelect: (DiaSemana?) -> Unit) {
    val opciones: List<DiaSemana?> = listOf(null) + DiaSemana.values().take(4)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        opciones.forEach { dia ->
            val isSelected = dia == seleccionado
            Surface(
                onClick = { onSelect(dia) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) AccentBlue else SurfaceField,
                modifier = Modifier.height(32.dp)
            ) {
                Box(Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = dia?.label ?: "Todas",
                        fontSize = 13.sp,
                        color = if (isSelected) Color.White else SubtitleGray,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun RutinaCard(rutina: Rutina) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceField,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(rutina.icono.colorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(rutina.icono.emoji, fontSize = 22.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(rutina.nombre, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("${rutina.horarioInicio} – ${rutina.horarioFin}", color = SubtitleGray, fontSize = 13.sp)
                if (rutina.direccion.isNotBlank()) {
                    Text(rutina.direccion, color = SubtitleGray, fontSize = 12.sp)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    rutina.diasSemana.forEach { dia ->
                        Surface(shape = RoundedCornerShape(6.dp), color = AccentBlue.copy(alpha = 0.18f)) {
                            Text(
                                dia.label,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp,
                                color = AccentBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            if (rutina.cantidadTareas > 0) {
                Surface(shape = RoundedCornerShape(8.dp), color = SurfaceField) {
                    Text(
                        "${rutina.cantidadTareas} tareas",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        color = SubtitleGray
                    )
                }
            }
        }
    }
}