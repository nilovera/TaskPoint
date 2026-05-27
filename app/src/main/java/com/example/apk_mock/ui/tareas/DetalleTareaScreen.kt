package com.example.apk_mock.ui.tareas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.apk_mock.ui.components.DetailActionTopBar
import com.example.apk_mock.ui.theme.AccentBlue
import com.example.apk_mock.ui.theme.BackgroundDark
import com.example.apk_mock.ui.theme.CancelRed
import com.example.apk_mock.ui.theme.DateBlue
import com.example.apk_mock.ui.theme.FieldBorder
import com.example.apk_mock.ui.theme.LabelGray
import com.example.apk_mock.ui.theme.PlaceholderGray
import com.example.apk_mock.ui.theme.StrengthGreen
import com.example.apk_mock.ui.theme.SubtitleGray
import com.example.apk_mock.ui.theme.SurfaceField
import com.example.apk_mock.ui.theme.categoryColor
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
    LaunchedEffect(Unit) { viewModel.refreshTareas() }

    val tarea = viewModel.getTareaById(taskId)
    val rutina = tarea?.let { viewModel.getRutinaForTarea(it) }
    val offers = tarea?.let { viewModel.getOffersForTarea(it) }.orEmpty()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSavedOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(showTaskEditedMessage) {
        if (showTaskEditedMessage) {
            showSavedOverlay = true
            delay(3000)
            showSavedOverlay = false
            onTaskEditedMessageShown()
        }
    }

    if (tarea == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No se encontró la tarea.", color = SubtitleGray, fontSize = 16.sp)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
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
        DetailTitle(tarea = tarea)
        Spacer(Modifier.height(18.dp))
        TaskInfoCard(tarea = tarea, rutina = rutina)
        Spacer(Modifier.height(16.dp))
        DetailSectionTitle("Foto")
        PhotoBlock()
        Spacer(Modifier.height(20.dp))
        DetailSectionTitle("Notas")
        NotesBlock(text = tarea.notas.ifBlank { "Sin notas." })
        Spacer(Modifier.height(20.dp))
        if (offers.isNotEmpty()) {
            DetailSectionTitle("OFERTAS CERCA DE ESTA RUTINA")
            Spacer(Modifier.height(8.dp))
            offers.forEach { offer ->
                OfferCard(offer = offer)
                Spacer(Modifier.height(10.dp))
            }
        }
    }

    if (showDeleteDialog) {
        BottomDeleteTaskDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                if (viewModel.onEliminarTarea(tarea.id)) {
                    showDeleteDialog = false
                    onTaskDeleted()
                }
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
                color = StrengthGreen,
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
        deleteColor = CancelRed,
        onEdit = { onEditTask(taskId) },
        onDelete = onDeleteClick
    )
}

@Composable
private fun BottomDeleteTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.62f))
            .padding(horizontal = 28.dp)
            .padding(top = 92.dp, bottom = 128.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = SurfaceField,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, FieldBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text("Eliminar tarea", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(18.dp))
                Text(
                    "¿Estás seguro que querés eliminar esta tarea?",
                    color = LabelGray,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Esta acción no se puede deshacer.",
                    color = SubtitleGray,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LabelGray),
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f)
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(52.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CancelRed, contentColor = Color.White),
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f)
                    ) {
                        Text("Eliminar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTitle(tarea: Tarea) {
    val categoryColor = tarea.categoria.categoryColor()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            tarea.titulo,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(18.dp))
    Surface(shape = RoundedCornerShape(5.dp), color = categoryColor.copy(alpha = 0.22f)) {
        Text(
            tarea.categoria.label,
            color = categoryColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun TaskInfoCard(tarea: Tarea, rutina: Rutina?) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = SurfaceField,
        border = androidx.compose.foundation.BorderStroke(1.dp, FieldBorder),
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
                .background(Color(0xFF090B12)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = DateBlue, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.size(12.dp))
        Column {
            Text(label, color = SubtitleGray, fontSize = 14.sp)
            Text(value, color = Color.White, fontSize = 17.sp)
        }
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(FieldBorder.copy(alpha = 0.75f))
    )
}

@Composable
private fun DetailSectionTitle(text: String) {
    Text(text, color = LabelGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun PhotoBlock() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF173016)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Image, contentDescription = "Foto de la tarea", tint = StrengthGreen, modifier = Modifier.size(36.dp))
    }
}

@Composable
private fun NotesBlock(text: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceField,
        border = androidx.compose.foundation.BorderStroke(1.dp, FieldBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text, color = LabelGray, fontSize = 16.sp, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun OfferCard(offer: StoreOffer) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceField,
        border = androidx.compose.foundation.BorderStroke(1.dp, FieldBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEAF2FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(offer.store.logo.toLogoText(), color = AccentBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.size(12.dp))
            Column {
                Text(offer.store.name.displayStoreName(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${offer.store.address.shortAddress()} | ${offer.distanceMeters} m",
                    color = PlaceholderGray,
                    fontSize = 13.sp
                )
                Text(offer.offer.title, color = StrengthGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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

