package com.example.apk_mock.ui.rutinas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.ui.components.AppEmptyStateCard
import com.example.apk_mock.ui.components.BottomStatusMessage
import com.example.apk_mock.ui.components.CreateActionPill
import com.example.apk_mock.ui.components.MainScreenHeader
import com.example.apk_mock.ui.theme.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun RutinasScreen(
    viewModel: RutinasViewModel,
    userName: String,
    isInitialDataSyncInProgress: Boolean = false,
    onNavigateToCrear: () -> Unit,
    onRutinaClick: (Rutina) -> Unit = {},
    onProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    innerPadding: PaddingValues = PaddingValues()
) {
    val listState by viewModel.listState.collectAsState()
    val colors = TaskPointTheme.colors
    var overlayMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(listState.snackbarMessage) {
        listState.snackbarMessage?.let { message ->
            overlayMessage = message
            delay(3000)
            overlayMessage = null
            viewModel.consumeSnackbar()
        }
    }

    val rutinas = viewModel.rutinasFiltradas()

    // Scaffold propio: maneja snackbar y FAB
    // Recibe innerPadding del Scaffold externo (bottom bar) sin duplicar
    Scaffold(
        containerColor = colors.background,
        floatingActionButton = {
            if (overlayMessage == null) {
                CreateActionPill(
                    text = "Nueva rutina +",
                    onClick = onNavigateToCrear,
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { selfPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                    // top: padding del Scaffold externo + margen visual
                        top = innerPadding.calculateTopPadding() + 8.dp,
                    // bottom: el Scaffold propio calcula el espacio del FAB
                        bottom = innerPadding.calculateBottomPadding() + selfPadding.calculateBottomPadding(),
                        start = 20.dp,
                        end = 20.dp
                    )
            ) {
            // ── Header ────────────────────────────────────────────────────────
            MainScreenHeader(
                title = "Mis rutinas",
                userName = userName,
                onProfile = onProfile,
                onLogout = onLogout
            )

            if (rutinas.isEmpty() && isInitialDataSyncInProgress) {
                Spacer(Modifier.height(12.dp))
                AppEmptyStateCard(message = "Cargando tus rutinas...")
            } else if (rutinas.isEmpty()) {
                // ── Empty state ────────────────────────────────────────────────
                Spacer(Modifier.height(12.dp))
                AppEmptyStateCard(
                    message = "No tenes rutinas\ncreadas.",
                    actionText = "Crear rutina \u2197",
                    onAction = onNavigateToCrear
                )
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
                    items(rutinas) { rutina ->
                        RutinaCard(rutina = rutina, onClick = { onRutinaClick(rutina) })
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

            overlayMessage?.let { message ->
                BottomStatusMessage(
                    message = message,
                    bottomPadding = innerPadding.calculateBottomPadding() + 16.dp
                )
            }
        }
    }
}

// ── Componentes reutilizables ─────────────────────────────────────────────────

@Composable
fun FiltrosDias(seleccionado: DiaSemana?, onSelect: (DiaSemana?) -> Unit) {
    val opciones: List<DiaSemana?> = listOf(null) + DiaSemana.values().toList()
    val colors = TaskPointTheme.colors

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        opciones.forEach { dia ->
            val isSelected = dia == seleccionado
            Surface(
                onClick = { onSelect(dia) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) colors.primary else colors.surface,
                border = if (isSelected) null else BorderStroke(1.dp, colors.border),
                modifier = Modifier.height(32.dp)
            ) {
                Box(Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = dia?.label ?: "Todas",
                        fontSize = 16.sp,
                        color = if (isSelected) Color.White else colors.textSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RutinaCard(rutina: Rutina, onClick: () -> Unit = {}) {
    val colors = TaskPointTheme.colors
    val isLight = colors.background.luminance() > 0.5f
    val dayChipContainer = colors.primary.copy(alpha = if (isLight) 0.16f else 0.18f)
    val countChipContainer = if (isLight) colors.primary else colors.primary.copy(alpha = 0.28f)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.routineCard,
        border = BorderStroke(1.dp, colors.border),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(rutina.icono.colorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(rutina.icono.emoji, fontSize = 24.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(rutina.nombre, color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Text("${rutina.horarioInicio} – ${rutina.horarioFin}", color = colors.textSecondary, fontSize = 14.sp)
                if (rutina.direccion.isNotBlank()) {
                    Text(rutina.direccion, color = colors.textSecondary, fontSize = 16.sp)
                }
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rutina.diasSemana.distinct().sortedBy { it.ordinal }.forEach { dia ->
                        Surface(shape = RoundedCornerShape(6.dp), color = dayChipContainer) {
                            Text(
                                dia.label,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 14.sp,
                                color = colors.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            if (rutina.cantidadTareas > 0) {
                Spacer(Modifier.width(5.dp))
                Surface(shape = RoundedCornerShape(50), color = countChipContainer) {
                    Text(
                        "${rutina.cantidadTareas} tareas",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
