package com.example.apk_mock.ui.rutinas

import android.widget.Space
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.Tarea
import com.example.apk_mock.domain.model.perteneceARutina
import com.example.apk_mock.ui.components.AppDeleteConfirmDialog
import com.example.apk_mock.ui.components.DetailActionTopBar
import com.example.apk_mock.ui.tareas.TareasViewModel
import com.example.apk_mock.ui.theme.TaskPointTheme
import com.example.apk_mock.ui.theme.categoryChipColors

@Composable
fun DetalleRutinaScreen(
    rutinaId: String,
    rutinasViewModel: RutinasViewModel,
    tareasViewModel: TareasViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    onEdit: () -> Unit,
    onTaskClick: (String) -> Unit,
    innerPadding: PaddingValues = PaddingValues()
) {
    val detalleState by rutinasViewModel.detalleState.collectAsStateWithLifecycle()
    val tareasState by tareasViewModel.listState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val colors = TaskPointTheme.colors

    LaunchedEffect(rutinaId) {
        rutinasViewModel.loadDetalleRutina(rutinaId)
        tareasViewModel.refreshTareas()
    }

    LaunchedEffect(detalleState.errorMessage) {
        detalleState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            rutinasViewModel.consumeDetalleError()
        }
    }

    LaunchedEffect(detalleState.snackbarMessage) {
        detalleState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            rutinasViewModel.consumeDetalleSnackbar()
        }
    }

    LaunchedEffect(detalleState.isDeleted) {
        if (detalleState.isDeleted) {
            rutinasViewModel.consumeDetalleDeletion()
            onDeleted()
        }
    }

    val rutina = detalleState.rutina
    val tareasAsociadas = remember(rutina, tareasState.tareas) {
        if (rutina == null) emptyList()
        else tareasState.tareas
            .filter { it.perteneceARutina(rutina) }
            .sortedBy { it.horario ?: "" }
    }

    Scaffold(
        containerColor = colors.background,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            ) { data ->
                Snackbar(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    containerColor = colors.success,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(data.visuals.message, fontSize = 13.sp)
                }
            }
        }
    ) { selfPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 28.dp,
                bottom = innerPadding.calculateBottomPadding() + selfPadding.calculateBottomPadding() + 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DetailTopBar(
                    onBack = onBack,
                    onEdit = onEdit,
                    onDelete = { showDeleteDialog = true }
                )
            }

            if (rutina == null) {
                item { MissingRoutineState(onBack = onBack) }
            } else {
                item { RoutineHero(rutina = rutina) }
                item { DaysSection(rutina = rutina) }
                item { TimeSection(rutina = rutina) }
                item { DescriptionSection(rutina = rutina) }
                item {
                    TasksSection(
                        tareas = tareasAsociadas,
                        onTaskClick = { tarea -> onTaskClick(tarea.id) }
                    )
                }
            }
        }
    }

    if (showDeleteDialog && rutina != null) {
        AppDeleteConfirmDialog(
            title = "Eliminar rutina",
            message = "Estas seguro que queres eliminar \"${rutina.nombre}\"?",
            support = "Esta accion tambien elimina sus tareas asociadas y no se puede deshacer.",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                rutinasViewModel.onEliminarRutina(rutina.id)
            }
        )
    }

}

@Composable
private fun DetailTopBar(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = TaskPointTheme.colors

    DetailActionTopBar(
        title = "Detalle de rutina",
        onBack = onBack,
        backIconTint = colors.textSecondary,
        actionIconTint = colors.textPrimary,
        editLabel = "Editar rutina",
        deleteLabel = "Eliminar rutina",
        onEdit = onEdit,
        onDelete = onDelete
    )
}

@Composable
private fun RoutineHero(rutina: Rutina) {
    val colors = TaskPointTheme.colors

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .background(Color(rutina.icono.colorHex), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(rutina.icono.emoji, fontSize = 42.sp)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            rutina.nombre,
            color = colors.textPrimary,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (rutina.direccion.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                rutina.direccion,
                color = colors.textSecondary,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    content: @Composable () -> Unit
) {
    val colors = TaskPointTheme.colors

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = colors.subTaskCard,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceAlt)
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Text(
                    title,
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            HorizontalDivider(color = colors.border)
            Box(Modifier.padding(contentPadding)) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DaysSection(rutina: Rutina) {
    val colors = TaskPointTheme.colors

    DetailSection(title = "Dias de la semana") {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            rutina.diasSemana.distinct().sortedBy { it.ordinal }.forEach { dia ->
                Surface(shape = RoundedCornerShape(5.dp), color = colors.primary.copy(alpha = 0.18f)) {
                    Text(
                        dia.label,
                        color = colors.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeSection(rutina: Rutina) {
    val colors = TaskPointTheme.colors

    DetailSection(title = "Horario", contentPadding = PaddingValues(0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            TimeValue(label = "Inicio", value = rutina.horarioInicio, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(colors.border)
            )
            TimeValue(label = "Fin", value = rutina.horarioFin, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TimeValue(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = TaskPointTheme.colors

    Column(
        modifier = modifier.padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = colors.textSecondary, fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
        Text(value, color = colors.textSecondary, fontSize = 16.sp)
    }
}

@Composable
private fun DescriptionSection(rutina: Rutina) {
    val colors = TaskPointTheme.colors

    DetailSection(title = "Descripcion") {
        Text(
            rutina.descripcion.ifBlank { "Sin descripcion" },
            color = colors.textSecondary,
            fontSize = 18.sp,
            lineHeight = 18.sp,
            modifier = Modifier.height(48.dp)
        )
    }
}

@Composable
private fun TasksSection(
    tareas: List<Tarea>,
    onTaskClick: (Tarea) -> Unit
) {
    val colors = TaskPointTheme.colors

    DetailSection(title = "Tareas asociadas", contentPadding = PaddingValues(0.dp)) {
        if (tareas.isEmpty()) {
            Text(
                "No hay tareas asociadas.",
                color = colors.textSecondary,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
            )
        } else {
            Column {
                tareas.forEachIndexed { index, tarea ->
                    AssociatedTaskRow(
                        tarea = tarea,
                        onClick = { onTaskClick(tarea) }
                    )
                    if (index < tareas.lastIndex) {
                        HorizontalDivider(color = colors.border)
                    }
                }
            }
        }
    }
}

@Composable
private fun AssociatedTaskRow(
    tarea: Tarea,
    onClick: () -> Unit
) {
    val categoryColors = tarea.categoria.categoryChipColors()
    val colors = TaskPointTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = if (tarea.requiereRevisionHorario) {
                    "Abrir tarea ${tarea.titulo}. Deshabilitada hasta revisar su dia y horario."
                } else {
                    "Abrir tarea ${tarea.titulo}"
                }
            }
            .clickable(role = Role.Button, onClick = onClick)
            .background(
                if (tarea.requiereRevisionHorario) {
                    colors.warningBackground
                } else {
                    Color.Transparent
                }
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (tarea.requiereRevisionHorario) Icons.Default.WarningAmber else Icons.Default.DateRange,
            contentDescription = null,
            tint = if (tarea.requiereRevisionHorario) colors.warningText else colors.textSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Text(
                tarea.titulo,
                color = if (tarea.requiereRevisionHorario) colors.warningText else colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (tarea.requiereRevisionHorario) {
                Text(
                    "Revisar dia y horario",
                    color = colors.warningText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Surface(shape = RoundedCornerShape(5.dp), color = categoryColors.container) {
            Text(
                tarea.categoria.label,
                color = categoryColors.content,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun MissingRoutineState(onBack: () -> Unit) {
    val colors = TaskPointTheme.colors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "No se encontro esta rutina.",
                color = colors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Puede haber sido eliminada o no pertenecer a tu cuenta.",
                color = colors.textSecondary,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text("Volver", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
