package com.example.apk_mock.ui.tareas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.ui.components.FormFieldLabel
import com.example.apk_mock.ui.register.AppTextField
import com.example.apk_mock.ui.theme.*
import com.example.apk_mock.ui.theme.categoryColor

@Composable
fun CrearTareaScreen(
    viewModel: TareasViewModel,
    onBack: () -> Unit,
    onTaskCreated: () -> Unit = onBack
) {
    val state by viewModel.formState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadFormData() }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.consumeSuccess()
            onTaskCreated()
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
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 100.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .background(SurfaceField, RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = SubtitleGray)
                }
                Text("Nueva tarea", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(36.dp))
            }

            Spacer(Modifier.height(24.dp))

            // ── Título ────────────────────────────────────────────────────────
            FormFieldLabel("Título de la tarea", required = true)
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
            FormFieldLabel("Categoría", required = true)
            CategoriaSelector(
                categorias = state.categoriasDisponibles,
                seleccionada = state.categoriaSeleccionada,
                onSelect = viewModel::onCategoriaSelect
            )
            if (state.categoriaError != null) {
                Spacer(Modifier.height(4.dp))
                Text(state.categoriaError!!, color = ErrorRed, fontSize = 12.sp)
            }

            Spacer(Modifier.height(20.dp))

            // ── Rutina asociada ───────────────────────────────────────────────
            FormFieldLabel("Rutina asociada", required = true)
            DropdownField(
                placeholder = "Seleccioná una rutina",
                selectedText = state.rutinaSeleccionadaNombre,
                isError = state.rutinaError != null,
                errorMessage = state.rutinaError
            ) { dismissMenu ->
                state.rutinasDisponibles.forEach { rutina ->
                    DropdownMenuItem(
                        text = {
                            Text(rutina.nombre, color = Color.White, fontSize = 14.sp)
                        },
                        onClick = {
                            viewModel.onRutinaSelect(rutina)
                            dismissMenu()
                        },
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
            FormFieldLabel("Día", required = true)
            DropdownField(
                placeholder = if (state.rutinaSeleccionadaId == null) {
                    "Seleccioná una rutina primero"
                } else {
                    "Seleccioná un día"
                },
                selectedText = state.diaSeleccionado?.label,
                isError = state.diaError != null,
                errorMessage = state.diaError
            ) { dismissMenu ->
                val dias = state.diasDisponibles
                dias.forEach { dia ->
                    DropdownMenuItem(
                        text = { Text(dia.label, color = Color.White, fontSize = 14.sp) },
                        onClick = {
                            viewModel.onDiaSelect(dia)
                            dismissMenu()
                        },
                        modifier = Modifier.background(SurfaceField)
                    )
                }
                if (dias.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Seleccioná una rutina para ver los días", color = SubtitleGray, fontSize = 14.sp) },
                        onClick = {},
                        modifier = Modifier.background(SurfaceField)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Horario ───────────────────────────────────────────────────────
            FormFieldLabel("Horario", required = true)
            DropdownField(
                placeholder = if (state.rutinaSeleccionadaId == null) {
                    "Seleccioná una rutina primero"
                } else {
                    "Seleccioná un horario"
                },
                selectedText = state.horario,
                isError = state.horarioError != null,
                errorMessage = state.horarioError
            ) { dismissMenu ->
                val horarios = state.horariosDisponibles
                horarios.forEach { h ->
                    DropdownMenuItem(
                        text = { Text(h, color = Color.White, fontSize = 14.sp) },
                        onClick = {
                            viewModel.onHorarioChange(h)
                            dismissMenu()
                        },
                        modifier = Modifier.background(SurfaceField)
                    )
                }
                if (horarios.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Seleccioná una rutina para ver los horarios", color = SubtitleGray, fontSize = 14.sp) },
                        onClick = {},
                        modifier = Modifier.background(SurfaceField)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Foto (opcional) ───────────────────────────────────────────────
            FormFieldLabel("Foto (opcional)")
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
            FormFieldLabel("Notas (opcional)")
            val maxNotas = 120
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

@Composable
fun EditarTareaScreen(
    taskId: String,
    viewModel: TareasViewModel,
    onBack: () -> Unit,
    onTaskEdited: () -> Unit = onBack
) {
    TareaFormScreen(
        title = "Editar tarea",
        backIcon = FormBackIcon.Arrow,
        viewModel = viewModel,
        loadData = { viewModel.loadEditFormData(taskId) },
        onBack = onBack,
        onSuccess = onTaskEdited,
        onSubmit = { viewModel.onEditarTarea(taskId) }
    )
}

@Composable
private fun TareaFormScreen(
    title: String,
    backIcon: FormBackIcon,
    viewModel: TareasViewModel,
    loadData: () -> Unit,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onSubmit: () -> Unit
) {
    val state by viewModel.formState.collectAsState()

    LaunchedEffect(Unit) { loadData() }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.consumeSuccess()
            onSuccess()
        }
    }

    TareaFormContent(
        title = title,
        backIcon = backIcon,
        state = state,
        onBack = onBack,
        onTituloChange = viewModel::onTituloChange,
        onCategoriaSelect = viewModel::onCategoriaSelect,
        onRutinaSelect = viewModel::onRutinaSelect,
        onDiaSelect = viewModel::onDiaSelect,
        onHorarioChange = viewModel::onHorarioChange,
        onNotasChange = viewModel::onNotasChange,
        onSubmit = onSubmit
    )
}

private enum class FormBackIcon { Close, Arrow }

@Composable
private fun TareaFormContent(
    title: String,
    backIcon: FormBackIcon,
    state: CrearTareaUiState,
    onBack: () -> Unit,
    onTituloChange: (String) -> Unit,
    onCategoriaSelect: (CategoriaTarea) -> Unit,
    onRutinaSelect: (Rutina?) -> Unit,
    onDiaSelect: (DiaSemana?) -> Unit,
    onHorarioChange: (String) -> Unit,
    onNotasChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val isEditing = title == "Editar tarea"
    val maxNotas = 120

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 100.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceField)
                        .border(1.dp, FieldBorder, RoundedCornerShape(14.dp))
                ) {
                    Icon(
                        imageVector = if (backIcon == FormBackIcon.Arrow) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                        contentDescription = if (backIcon == FormBackIcon.Arrow) "Volver" else "Cerrar",
                        tint = SubtitleGray
                    )
                }
                Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(48.dp))
            }

            Spacer(Modifier.height(24.dp))

            FormFieldLabel("Título de la tarea", required = true)
            AppTextField(
                label = "",
                value = state.titulo,
                onValueChange = onTituloChange,
                placeholder = "Comprar comida",
                isError = state.tituloError != null,
                errorMessage = state.tituloError
            )

            Spacer(Modifier.height(20.dp))

            FormFieldLabel("Categoría", required = true)
            CategoriaSelector(
                categorias = state.categoriasDisponibles,
                seleccionada = state.categoriaSeleccionada,
                onSelect = onCategoriaSelect
            )
            if (state.categoriaError != null) {
                Spacer(Modifier.height(4.dp))
                Text(state.categoriaError, color = ErrorRed, fontSize = 12.sp)
            }

            Spacer(Modifier.height(20.dp))

            FormFieldLabel("Rutina asociada", required = true)
            DropdownField(
                placeholder = "Seleccioná una rutina",
                selectedText = state.rutinaSeleccionadaNombre,
                isError = state.rutinaError != null,
                errorMessage = state.rutinaError
            ) { dismissMenu ->
                state.rutinasDisponibles.forEach { rutina ->
                    DropdownMenuItem(
                        text = { Text(rutina.nombre, color = Color.White, fontSize = 14.sp) },
                        onClick = {
                            onRutinaSelect(rutina)
                            dismissMenu()
                        },
                        modifier = Modifier.background(SurfaceField)
                    )
                }
                if (state.rutinasDisponibles.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No hay rutinas creadas", color = SubtitleGray, fontSize = 14.sp) },
                        onClick = { dismissMenu() },
                        modifier = Modifier.background(SurfaceField)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            FormFieldLabel("Día", required = true)
            DropdownField(
                placeholder = if (state.rutinaSeleccionadaId == null) {
                    "Seleccioná una rutina primero"
                } else {
                    "Seleccioná un día"
                },
                selectedText = state.diaSeleccionado?.label,
                isError = state.diaError != null,
                errorMessage = state.diaError
            ) { dismissMenu ->
                state.diasDisponibles.forEach { dia ->
                    DropdownMenuItem(
                        text = { Text(dia.label, color = Color.White, fontSize = 14.sp) },
                        onClick = {
                            onDiaSelect(dia)
                            dismissMenu()
                        },
                        modifier = Modifier.background(SurfaceField)
                    )
                }
                if (state.diasDisponibles.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Seleccioná una rutina para ver los días", color = SubtitleGray, fontSize = 14.sp) },
                        onClick = { dismissMenu() },
                        modifier = Modifier.background(SurfaceField)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            FormFieldLabel("Horario", required = true)
            DropdownField(
                placeholder = if (state.rutinaSeleccionadaId == null) {
                    "Seleccioná una rutina primero"
                } else {
                    "Seleccioná un horario"
                },
                selectedText = state.horario,
                isError = state.horarioError != null,
                errorMessage = state.horarioError
            ) { dismissMenu ->
                state.horariosDisponibles.forEach { horario ->
                    DropdownMenuItem(
                        text = { Text(horario, color = Color.White, fontSize = 14.sp) },
                        onClick = {
                            onHorarioChange(horario)
                            dismissMenu()
                        },
                        modifier = Modifier.background(SurfaceField)
                    )
                }
                if (state.horariosDisponibles.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Seleccioná una rutina para ver los horarios", color = SubtitleGray, fontSize = 14.sp) },
                        onClick = { dismissMenu() },
                        modifier = Modifier.background(SurfaceField)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            FormFieldLabel("Foto (opcional)")
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceField,
                border = androidx.compose.foundation.BorderStroke(1.dp, FieldBorder),
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
                            .background(if (isEditing) Color(0xFF173016) else Color(0xFF242A5F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = StrengthGreen, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (isEditing) "Cambiar foto" else "Agregar foto", color = LabelGray, fontSize = 16.sp)
                        Text(
                            if (isEditing) "Toca para reemplazar" else "Desde cámara o galería",
                            color = PlaceholderGray,
                            fontSize = 13.sp
                        )
                    }
                    if (isEditing) {
                        Icon(Icons.Default.Close, contentDescription = "Quitar foto", tint = ErrorRed)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            FormFieldLabel("Notas (opcional)")
            OutlinedTextField(
                value = state.notas,
                onValueChange = { if (it.length <= maxNotas) onNotasChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
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
                Text("${state.notas.length}/$maxNotas", color = SubtitleGray, fontSize = 13.sp)
            }

            Spacer(Modifier.height(28.dp))
        }

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StrengthGreen)
        ) {
            Text("Guardar cambios", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

// ── Componentes reutilizables ─────────────────────────────────────────────────

@Composable
private fun CategoriaSelector(
    categorias: List<CategoriaTarea>,
    seleccionada: CategoriaTarea?,
    onSelect: (CategoriaTarea) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categorias.chunked(3).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fila.forEach { cat ->
                    val selected = cat == seleccionada
                    val categoryColor = cat.categoryColor()
                    Surface(
                        onClick = { onSelect(cat) },
                        shape = RoundedCornerShape(8.dp),
                        color = categoryColor.copy(alpha = if (selected) 0.95f else 0.18f),
                        border = if (selected) null else androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = categoryColor.copy(alpha = 0.52f)
                        )
                    ) {
                        Box(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                            Text(
                                cat.label,
                                fontSize = 12.sp,
                                color = if (selected) Color.White else categoryColor,
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
    menuContent: @Composable ColumnScope.(dismissMenu: () -> Unit) -> Unit
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
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
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
            menuContent { expanded = false }
        }
    }
    if (isError && errorMessage != null) {
        Spacer(Modifier.height(4.dp))
        Text(errorMessage, color = ErrorRed, fontSize = 12.sp)
    }
}
