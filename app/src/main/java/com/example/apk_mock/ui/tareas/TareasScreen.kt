package com.example.apk_mock.ui.tareas

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Tarea
import com.example.apk_mock.ui.components.AppEmptyStateCard
import com.example.apk_mock.ui.components.BottomStatusMessage
import com.example.apk_mock.ui.components.CreateActionPill
import com.example.apk_mock.ui.components.MainScreenHeader
import com.example.apk_mock.ui.rutinas.FiltrosDias
import com.example.apk_mock.ui.theme.BackgroundDark
import com.example.apk_mock.ui.theme.DateBlue
import com.example.apk_mock.ui.theme.SubtitleGray
import com.example.apk_mock.ui.theme.SurfaceField
import com.example.apk_mock.ui.theme.categoryColor
import com.example.apk_mock.ui.utils.daysFrom
import com.example.apk_mock.ui.utils.taskSectionLabel
import com.example.apk_mock.ui.utils.toDiaSemana
import java.time.LocalDate
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
                CreateActionPill(
                    text = "Nueva tarea +",
                    onClick = onNavigateToCrear,
                    enabled = canCreateTask,
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
                        top = innerPadding.calculateTopPadding() + 8.dp,
                        bottom = innerPadding.calculateBottomPadding() + selfPadding.calculateBottomPadding(),
                        start = 20.dp,
                        end = 20.dp
                    )
            ) {
                MainScreenHeader(
                    title = "Mis tareas",
                    userName = userName,
                    onProfile = onProfile,
                    onLogout = onLogout
                )

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
                                    dia.taskSectionLabel(today),
                                    color = DateBlue,
                                    fontSize = 14.sp,
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

            overlayMessage?.let { message ->
                BottomStatusMessage(
                    message = message,
                    bottomPadding = innerPadding.calculateBottomPadding() + 16.dp
                )
            }
        }
    }
}

@Composable
private fun EmptyTasksBlock(
    canCreateTask: Boolean,
    onNavigateToCrear: () -> Unit
) {
    AppEmptyStateCard(
        message = "No tenes tareas\ncargadas.",
        actionText = "Crear tarea \u2197",
        onAction = onNavigateToCrear,
        actionEnabled = canCreateTask
    )
}

@Composable
fun TareaCard(
    tarea: Tarea,
    onClick: () -> Unit = {}
) {
    val catColor = tarea.categoria.categoryColor()
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (tarea.horario != null || tarea.rutinaNombre != null) {
                    Text(
                        listOfNotNull(tarea.horario, tarea.rutinaNombre).joinToString(" | "),
                        color = SubtitleGray,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Surface(shape = RoundedCornerShape(5.dp), color = catColor.copy(alpha = 0.18f)) {
                Text(
                    tarea.categoria.label,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize = 12.sp,
                    color = catColor,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

