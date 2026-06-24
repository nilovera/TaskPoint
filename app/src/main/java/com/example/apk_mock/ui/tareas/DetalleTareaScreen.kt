package com.example.apk_mock.ui.tareas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.StoreOffer
import com.example.apk_mock.domain.model.Tarea
import com.example.apk_mock.ui.components.AppDeleteConfirmDialog
import com.example.apk_mock.ui.components.DetailActionTopBar
import com.example.apk_mock.ui.theme.TaskPointTheme
import com.example.apk_mock.ui.theme.categoryChipColors
import com.example.apk_mock.ui.utils.displayName
import kotlinx.coroutines.delay

@Composable
fun DetalleTareaScreen(
    taskId: String,
    viewModel: TareasViewModel,
    onBack: () -> Unit,
    onEditTask: (String) -> Unit = {},
    onTaskDeleted: () -> Unit = {},
    showTaskEditedMessage: Boolean = false,
    onTaskEditedMessageShown: () -> Unit = {},
    innerPadding: PaddingValues = PaddingValues()
) {
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    LaunchedEffect(taskId) { viewModel.loadTaskDetail(taskId) }

    val tarea = detailState.tarea
    val rutina = detailState.rutina
    val offers = detailState.offers
    val offersLocationPending = detailState.offersLocationPending
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSavedOverlay by remember { mutableStateOf(false) }
    val colors = TaskPointTheme.colors

    LaunchedEffect(detailState.isDeleted) {
        if (detailState.isDeleted) {
            viewModel.consumeTaskDeleted()
            onTaskDeleted()
        }
    }

    LaunchedEffect(showTaskEditedMessage) {
        if (showTaskEditedMessage) {
            showSavedOverlay = true
            delay(3000)
            showSavedOverlay = false
            onTaskEditedMessageShown()
        }
    }

    if (detailState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentAlignment = Alignment.Center
        ) {
            Text("Cargando tarea...", color = colors.textSecondary, fontSize = 16.sp)
        }
        return
    }

    if (tarea == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No se encontró la tarea.", color = colors.textSecondary, fontSize = 16.sp)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = innerPadding.calculateTopPadding() + 20.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            )
    ) {
        DetailHeader(
            taskId = tarea.id,
            onBack = onBack,
            onEditTask = onEditTask,
            onDeleteClick = { showDeleteDialog = true }
        )
        Spacer(Modifier.height(22.dp))
        if (tarea.requiereRevisionHorario) {
            ScheduleReviewWarning()
            Spacer(Modifier.height(18.dp))
        }
        DetailTitle(tarea = tarea)
        Spacer(Modifier.height(18.dp))
        TaskInfoCard(tarea = tarea, rutina = rutina)
        Spacer(Modifier.height(16.dp))
        DetailSectionTitle("Foto")
        PhotoBlock(tarea.photoPath)
        Spacer(Modifier.height(20.dp))
        DetailSectionTitle("Notas")
        NotesBlock(text = tarea.notas.ifBlank { "Sin notas." })
        Spacer(Modifier.height(20.dp))
        if (offers.isNotEmpty() || offersLocationPending) {
            DetailSectionTitle("OFERTAS CERCA DE ESTA RUTINA")
            Spacer(Modifier.height(8.dp))
            if (offersLocationPending) {
                Text(
                    "Calcularemos las ofertas cercanas cuando se ubique la dirección de la rutina.",
                    color = colors.textSecondary,
                    fontSize = 14.sp
                )
            } else {
                offers.forEach { offer ->
                    OfferCard(offer = offer)
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }

    if (showDeleteDialog) {
        AppDeleteConfirmDialog(
            title = "Eliminar tarea",
            message = "Estas seguro que queres eliminar esta tarea?",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                viewModel.onEliminarTarea(tarea.id)
            }
        )
    }

    if (showSavedOverlay) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(bottom = innerPadding.calculateBottomPadding() + 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                color = colors.success,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        "Cambios guardados correctamente.",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleReviewWarning() {
    val colors = TaskPointTheme.colors

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.warningBackground,
        border = BorderStroke(1.dp, colors.warningText),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.WarningAmber,
                contentDescription = null,
                tint = colors.warningText,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.size(10.dp))
            Column {
                Text(
                    "Tarea deshabilitada",
                    color = colors.warningText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Edita su dia y horario para volver a habilitarla.",
                    color = colors.warningText,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun DetailHeader(
    taskId: String,
    onBack: () -> Unit,
    onEditTask: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    DetailActionTopBar(
        title = "Detalle de tarea",
        onBack = onBack,
        editLabel = "Editar tarea",
        deleteLabel = "Eliminar tarea",
        onEdit = { onEditTask(taskId) },
        onDelete = onDeleteClick
    )
}

@Composable
private fun DetailTitle(tarea: Tarea) {
    val categoryColors = tarea.categoria.categoryChipColors()
    val colors = TaskPointTheme.colors

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            tarea.titulo,
            color = colors.textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(18.dp))
    Surface(shape = RoundedCornerShape(5.dp), color = categoryColors.container) {
        Text(
            tarea.categoria.label,
            color = categoryColors.content,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun TaskInfoCard(tarea: Tarea, rutina: Rutina?) {
    val colors = TaskPointTheme.colors

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            InfoRow(Icons.Default.Work, "Rutina", tarea.rutinaNombre ?: rutina?.nombre ?: "Sin rutina")
            DividerLine()
            InfoRow(Icons.Default.LocationOn, "Dirección", rutina?.direccion ?: "Sin dirección")
            DividerLine()
            InfoRow(Icons.Default.DateRange, "Día", tarea.dia?.displayName() ?: "Sin día")
            DividerLine()
            InfoRow(Icons.Default.AccessTime, "Horario", tarea.horario ?: "Sin horario")
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    val colors = TaskPointTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.size(12.dp))
        Column {
            Text(label, color = colors.textSecondary, fontSize = 14.sp)
            Text(value, color = colors.textPrimary, fontSize = 17.sp)
        }
    }
}

@Composable
private fun DividerLine() {
    val colors = TaskPointTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.border.copy(alpha = 0.75f))
    )
}

@Composable
private fun DetailSectionTitle(text: String) {
    val colors = TaskPointTheme.colors

    Text(text, color = colors.label, fontSize = 14.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun PhotoBlock(photoPath: String?) {
    var isPhotoExpanded by remember(photoPath) { mutableStateOf(false) }

    TaskPhotoImage(
        photoPath = photoPath,
        contentDescription = "Foto de la tarea",
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        onClick = photoPath?.takeIf { it.isNotBlank() }?.let {
            { isPhotoExpanded = true }
        }
    )

    if (isPhotoExpanded && !photoPath.isNullOrBlank()) {
        ExpandedTaskPhotoDialog(
            photoPath = photoPath,
            onDismiss = { isPhotoExpanded = false }
        )
    }
}

@Composable
private fun NotesBlock(text: String) {
    val colors = TaskPointTheme.colors

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text, color = colors.label, fontSize = 16.sp, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun OfferCard(offer: StoreOffer) {
    val colors = TaskPointTheme.colors

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(offer.store.logo.toLogoText(), color = colors.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.size(12.dp))
            Column {
                Text(offer.store.name.displayStoreName(), color = colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${offer.store.address.shortAddress()} | ${offer.distanceMeters} m",
                    color = colors.placeholder,
                    fontSize = 13.sp
                )
                Text(offer.offer.title, color = colors.success, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun String.toLogoText(): String {
    return split("_")
        .filter { it.isNotBlank() && it != "logo" }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifBlank { take(3).uppercase() }
}

private fun String.displayStoreName(): String {
    return substringBefore(" - ")
}

private fun String.shortAddress(): String {
    return substringBefore(",")
}
