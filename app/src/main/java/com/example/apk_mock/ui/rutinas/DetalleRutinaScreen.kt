package com.example.apk_mock.ui.rutinas

import android.widget.Space
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.Tarea
import com.example.apk_mock.ui.components.AppConfirmDialog
import com.example.apk_mock.ui.components.DetailActionTopBar
import com.example.apk_mock.ui.tareas.TareasViewModel
import com.example.apk_mock.ui.theme.AccentBlue
import com.example.apk_mock.ui.theme.BackgroundDark
import com.example.apk_mock.ui.theme.ErrorRed
import com.example.apk_mock.ui.theme.StrengthGreen
import com.example.apk_mock.ui.theme.SubtitleGray
import com.example.apk_mock.ui.theme.SurfaceField
import com.example.apk_mock.ui.theme.categoryColor

private val DetailCard = Color(0xFF171B2D)
private val DetailContent = Color(0xFF111629)
private val DetailBorder = Color(0xFF252B44)
private val DetailMenu = Color(0xFF0B1540)

@Composable
fun DetalleRutinaScreen(
    rutinaId: String,
    rutinasViewModel: RutinasViewModel,
    tareasViewModel: TareasViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    onEdit: () -> Unit,
    innerPadding: PaddingValues = PaddingValues()
) {
    val detalleState by rutinasViewModel.detalleState.collectAsState()
    val tareasState by tareasViewModel.listState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

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
            .filter { it.rutinaId == rutina.id || it.rutinaNombre == rutina.nombre }
            .sortedBy { it.horario ?: "" }
    }

    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            ) { data ->
                Snackbar(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    containerColor = StrengthGreen,
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
                .background(BackgroundDark)
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
                item { TasksSection(tareas = tareasAsociadas) }
            }
        }
    }

    if (showDeleteDialog && rutina != null) {
        AppConfirmDialog(
            title = "Eliminar rutina",
            message = "Estas seguro que queres eliminar \"${rutina.nombre}\"? Esta accion tambien elimina sus tareas asociadas y no se puede deshacer.",
            confirmText = "Eliminar",
            confirmColor = ErrorRed,
            containerColor = DetailCard,
            messageColor = SubtitleGray,
            supportColor = SubtitleGray,
            dismissContainerColor = Color.Transparent,
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
    DetailActionTopBar(
        title = "Detalle de rutina",
        onBack = onBack,
        backIconTint = SubtitleGray,
        actionIconTint = Color.White,
        menuContainerColor = DetailMenu,
        menuBorderColor = null,
        editLabel = "Editar rutina",
        deleteLabel = "Eliminar rutina",
        deleteColor = ErrorRed,
        itemFontSize = 12.sp,
        itemIconSize = 16.dp,
        onEdit = onEdit,
        onDelete = onDelete
    )
}

@Composable
private fun RoutineHero(rutina: Rutina) {
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
            color = Color.White,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (rutina.direccion.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                rutina.direccion,
                color = SubtitleGray,
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = DetailContent,
        border = BorderStroke(1.dp, DetailBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DetailCard)
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Text(
                    title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            HorizontalDivider(color = DetailBorder)
            Box(Modifier.padding(contentPadding)) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DaysSection(rutina: Rutina) {
    DetailSection(title = "Dias de la semana") {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            rutina.diasSemana.distinct().sortedBy { it.ordinal }.forEach { dia ->
                Surface(shape = RoundedCornerShape(5.dp), color = AccentBlue.copy(alpha = 0.35f)) {
                    Text(
                        dia.label,
                        color = Color(0xFF9EB0FF),
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
                    .background(DetailBorder)
            )
            TimeValue(label = "Fin", value = rutina.horarioFin, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TimeValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = SubtitleGray, fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
        Text(value, color = SubtitleGray, fontSize = 16.sp)
    }
}

@Composable
private fun DescriptionSection(rutina: Rutina) {
    DetailSection(title = "Descripcion") {
        Text(
            rutina.descripcion.ifBlank { "Sin descripcion" },
            color = SubtitleGray,
            fontSize = 18.sp,
            lineHeight = 18.sp,
            modifier = Modifier.height(48.dp)
        )
    }
}

@Composable
private fun TasksSection(tareas: List<Tarea>) {
    DetailSection(title = "Tareas asociadas", contentPadding = PaddingValues(0.dp)) {
        if (tareas.isEmpty()) {
            Text(
                "No hay tareas asociadas.",
                color = SubtitleGray,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
            )
        } else {
            Column {
                tareas.forEachIndexed { index, tarea ->
                    AssociatedTaskRow(tarea = tarea)
                    if (index < tareas.lastIndex) {
                        HorizontalDivider(color = DetailBorder)
                    }
                }
            }
        }
    }
}

@Composable
private fun AssociatedTaskRow(tarea: Tarea) {
    val catColor = tarea.categoria.categoryColor()

    Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.DateRange,
            contentDescription = null,
            tint = SubtitleGray,
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
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Surface(shape = RoundedCornerShape(5.dp), color = catColor.copy(alpha = 0.22f)) {
            Text(
                tarea.categoria.label,
                color = catColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun MissingRoutineState(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = DetailCard,
        border = BorderStroke(1.dp, DetailBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "No se encontro esta rutina.",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Puede haber sido eliminada o no pertenecer a tu cuenta.",
                color = SubtitleGray,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Volver", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

