package com.example.taskpoint.ui.tareas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskpoint.domain.model.CategoriaTarea
import com.example.taskpoint.domain.model.DiaSemana
import com.example.taskpoint.ui.register.AppTextField
import com.example.taskpoint.ui.theme.*

@Composable
fun CrearTareaScreen(
    viewModel: TareasViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.formState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadFormData() }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.consumeSuccess()
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 100.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = SubtitleGray)
                }
                Text("Nueva tarea", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(48.dp))
            }

            Spacer(Modifier.height(24.dp))

            // ── Título ────────────────────────────────────────────────────────
            SectionLabel("Título de la tarea *")
            AppTextField(
                label = "",
                value = state.titulo,
                onValueChange = viewModel::onTituloChange,
                placeholder = "Comprar comida",
                isError = state.tituloError != null,
                errorMessage = state.tituloError
            )

            Spacer(Modifier.height(20.dp))

            // ── Categoría ─────────────────────────────────────────────────────
            SectionLabel("Categoría *")
            CategoriaSelector(
                seleccionada = state.categoriaSeleccionada,
                onSelect = viewModel::onCategoriaSelect
            )
            if (state.categoriaError != null) {
                Spacer(Modifier.height(4.dp))
                Text(state.categoriaError!!, color = ErrorRed, fontSize = 12.sp)
            }

            Spacer(Modifier.height(20.dp))

            // ── Rutina asociada ───────────────────────────────────────────────
            SectionLabel("Rutina asociada *")
            DropdownField(
                placeholder = "Seleccioná una rutina",
                selectedText = state.rutinaSeleccionadaNombre,
                isError = state.rutinaError != null,
                errorMessage = state.rutinaError
            ) {
                state.rutinasDisponibles.forEach { rutina ->
                    DropdownMenuItem(
                        text = {
                            Text(rutina.nombre, color = Color.White, fontSize = 14.sp)
                        },
                        onClick = { viewModel.onRutinaSelect(rutina) },
                        modifier = Modifier.background(SurfaceField)
                    )
                }
                if (state.rutinasDisponibles.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No hay rutinas creadas", color = SubtitleGray, fontSize = 14.sp) },
                        onClick = {},
                        modifier = Modifier.background(SurfaceField)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Día ───────────────────────────────────────────────────────────
            SectionLabel("Día *")
            DropdownField(
                placeholder = "Seleccioná un día",
                selectedText = state.diaSeleccionado?.label,
                isError = state.diaError != null,
                errorMessage = state.diaError
            ) {
                DiaSemana.values().forEach { dia ->
                    DropdownMenuItem(
                        text = { Text(dia.label, color = Color.White, fontSize = 14.sp) },
                        onClick = { viewModel.onDiaSelect(dia) },
                        modifier = Modifier.background(SurfaceField)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Horario ───────────────────────────────────────────────────────
            SectionLabel("Horario *")
            DropdownField(
                placeholder = "Seleccioná un horario",
                selectedText = state.horario,
                isError = state.horarioError != null,
                errorMessage = state.horarioError
            ) {
                listOf("06:00","07:00","08:00","09:00","10:00","11:00","12:00",
                    "13:00","14:00","15:00","16:00","17:00","18:00","19:00","20:00","21:00","22:00").forEach { h ->
                    DropdownMenuItem(
                        text = { Text(h, color = Color.White, fontSize = 14.sp) },
                        onClick = { viewModel.onHorarioChange(h) },
                        modifier = Modifier.background(SurfaceField)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Foto (opcional) ───────────────────────────────────────────────
            SectionLabel("Foto (opcional)")
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceField,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = SubtitleGray, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Agregar foto", color = Color.White, fontSize = 14.sp)
                        Text("Desde cámara o galería", color = SubtitleGray, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Notas (opcional) ──────────────────────────────────────────────
            SectionLabel("Notas (opcional)")
            val maxNotas = 100
            OutlinedTextField(
                value = state.notas,
                onValueChange = { if (it.length <= maxNotas) viewModel.onNotasChange(it) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                placeholder = { Text("Agregá una nota...", color = PlaceholderGray, fontSize = 14.sp) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceField,
                    unfocusedContainerColor = SurfaceField,
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = FieldBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = AccentBlue
                )
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text("${state.notas.length}/$maxNotas", color = SubtitleGray, fontSize = 11.sp)
            }

            Spacer(Modifier.height(28.dp))
        }

        // ── Botón fijo abajo ──────────────────────────────────────────────────
        Button(
            onClick = viewModel::onCrearTarea,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StrengthGreen)
        ) {
            Text(
                text = if (state.rutinasDisponibles.isEmpty()) "Crear tarea" else "Guardar cambios",
                fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White
            )
        }
    }
}

// ── Componentes reutilizables ─────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = LabelGray, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun CategoriaSelector(
    seleccionada: CategoriaTarea?,
    onSelect: (CategoriaTarea) -> Unit
) {
    val rows = listOf(
        listOf(CategoriaTarea.PERSONAL, CategoriaTarea.SUPERMERCADO, CategoriaTarea.INDUMENTARIA),
        listOf(CategoriaTarea.FACULTAD, CategoriaTarea.ESTUDIO)
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fila.forEach { cat ->
                    val selected = cat == seleccionada
                    Surface(
                        onClick = { onSelect(cat) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) AccentBlue else SurfaceField,
                        border = if (selected) null else ButtonDefaults.outlinedButtonBorder
                    ) {
                        Box(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                            Text(
                                cat.label,
                                fontSize = 12.sp,
                                color = if (selected) Color.White else SubtitleGray,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    placeholder: String,
    selectedText: String?,
    isError: Boolean = false,
    errorMessage: String? = null,
    menuContent: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedText ?: "",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            placeholder = { Text(placeholder, color = PlaceholderGray, fontSize = 14.sp) },
            trailingIcon = {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = SubtitleGray
                )
            },
            isError = isError,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceField,
                unfocusedContainerColor = SurfaceField,
                errorContainerColor = Color(0xFF2A1515),
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = FieldBorder,
                errorBorderColor = ErrorRed,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                errorTextColor = Color.White,
                cursorColor = AccentBlue
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SurfaceField)
        ) {
            menuContent()
        }
    }
    if (isError && errorMessage != null) {
        Spacer(Modifier.height(4.dp))
        Text(errorMessage, color = ErrorRed, fontSize = 12.sp)
    }
}