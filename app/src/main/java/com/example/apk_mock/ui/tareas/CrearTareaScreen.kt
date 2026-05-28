package com.example.apk_mock.ui.tareas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.ui.components.AppTextArea
import com.example.apk_mock.ui.components.AppTextField
import com.example.apk_mock.ui.components.AppTopBar
import com.example.apk_mock.ui.components.AppTopBarSize
import com.example.apk_mock.ui.components.FormFieldLabel
import com.example.apk_mock.ui.components.appTextFieldColors
import com.example.apk_mock.ui.theme.TaskPointTheme
import com.example.apk_mock.ui.theme.categoryChipColors

@Composable
fun CrearTareaScreen(
    viewModel: TareasViewModel,
    onBack: () -> Unit,
    onCapturePhoto: () -> Unit = {},
    onTaskCreated: () -> Unit = onBack
) {
    TareaFormScreen(
        title = "Nueva tarea",
        isEditing = false,
        topBarSize = AppTopBarSize.Compact,
        submitButtonText = "Crear tarea",
        notesCounterFontSize = 11.sp,
        viewModel = viewModel,
        loadData = viewModel::loadFormData,
        onBack = onBack,
        onSuccess = onTaskCreated,
        onCapturePhoto = onCapturePhoto,
        onSubmit = viewModel::onCrearTarea
    )
}

@Composable
fun EditarTareaScreen(
    taskId: String,
    viewModel: TareasViewModel,
    onBack: () -> Unit,
    onCapturePhoto: () -> Unit = {},
    onTaskEdited: () -> Unit = onBack
) {
    TareaFormScreen(
        title = "Editar tarea",
        isEditing = true,
        topBarSize = AppTopBarSize.Regular,
        submitButtonText = "Guardar cambios",
        notesCounterFontSize = 13.sp,
        viewModel = viewModel,
        loadData = { viewModel.loadEditFormData(taskId) },
        onBack = onBack,
        onSuccess = onTaskEdited,
        onCapturePhoto = onCapturePhoto,
        onSubmit = { viewModel.onEditarTarea(taskId) }
    )
}

@Composable
private fun TareaFormScreen(
    title: String,
    isEditing: Boolean,
    topBarSize: AppTopBarSize,
    submitButtonText: String,
    notesCounterFontSize: androidx.compose.ui.unit.TextUnit,
    viewModel: TareasViewModel,
    loadData: () -> Unit,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onCapturePhoto: () -> Unit,
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
        isEditing = isEditing,
        topBarSize = topBarSize,
        submitButtonText = submitButtonText,
        notesCounterFontSize = notesCounterFontSize,
        state = state,
        onBack = onBack,
        onTituloChange = viewModel::onTituloChange,
        onCategoriaSelect = viewModel::onCategoriaSelect,
        onRutinaSelect = viewModel::onRutinaSelect,
        onDiaSelect = viewModel::onDiaSelect,
        onHorarioChange = viewModel::onHorarioChange,
        onNotasChange = viewModel::onNotasChange,
        onPhotoClick = onCapturePhoto,
        onPhotoRemove = viewModel::onPhotoRemoved,
        onSubmit = onSubmit
    )
}

@Composable
private fun TareaFormContent(
    title: String,
    isEditing: Boolean,
    topBarSize: AppTopBarSize,
    submitButtonText: String,
    notesCounterFontSize: androidx.compose.ui.unit.TextUnit,
    state: CrearTareaUiState,
    onBack: () -> Unit,
    onTituloChange: (String) -> Unit,
    onCategoriaSelect: (CategoriaTarea) -> Unit,
    onRutinaSelect: (Rutina?) -> Unit,
    onDiaSelect: (DiaSemana?) -> Unit,
    onHorarioChange: (String) -> Unit,
    onNotasChange: (String) -> Unit,
    onPhotoClick: () -> Unit,
    onPhotoRemove: () -> Unit,
    onSubmit: () -> Unit
) {
    val maxNotas = 120
    val colors = TaskPointTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
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

            AppTopBar(
                title = title,
                onBack = onBack,
                size = topBarSize
            )

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
                Text(state.categoriaError, color = colors.destructive, fontSize = 12.sp)
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
                        text = { Text(rutina.nombre, color = colors.textPrimary, fontSize = 14.sp) },
                        onClick = {
                            onRutinaSelect(rutina)
                            dismissMenu()
                        },
                        modifier = Modifier.background(colors.fieldBackground)
                    )
                }
                if (state.rutinasDisponibles.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No hay rutinas creadas", color = colors.textSecondary, fontSize = 14.sp) },
                        onClick = { dismissMenu() },
                        modifier = Modifier.background(colors.fieldBackground)
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
                        text = { Text(dia.label, color = colors.textPrimary, fontSize = 14.sp) },
                        onClick = {
                            onDiaSelect(dia)
                            dismissMenu()
                        },
                        modifier = Modifier.background(colors.fieldBackground)
                    )
                }
                if (state.diasDisponibles.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Seleccioná una rutina para ver los días", color = colors.textSecondary, fontSize = 14.sp) },
                        onClick = { dismissMenu() },
                        modifier = Modifier.background(colors.fieldBackground)
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
                        text = { Text(horario, color = colors.textPrimary, fontSize = 14.sp) },
                        onClick = {
                            onHorarioChange(horario)
                            dismissMenu()
                        },
                        modifier = Modifier.background(colors.fieldBackground)
                    )
                }
                if (state.horariosDisponibles.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Seleccioná una rutina para ver los horarios", color = colors.textSecondary, fontSize = 14.sp) },
                        onClick = { dismissMenu() },
                        modifier = Modifier.background(colors.fieldBackground)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            FormFieldLabel("Foto (opcional)")
            Surface(
                onClick = onPhotoClick,
                shape = RoundedCornerShape(12.dp),
                color = colors.fieldBackground,
                border = BorderStroke(1.dp, colors.fieldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TaskPhotoImage(
                        photoPath = state.photoPath,
                        contentDescription = "Foto seleccionada",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (state.photoPath != null) "Foto adjunta" else if (isEditing) "Cambiar foto" else "Agregar foto",
                            color = colors.label,
                            fontSize = 16.sp
                        )
                        Text(
                            if (state.photoPath != null || isEditing) "Toca para reemplazar" else "Desde camara",
                            color = colors.placeholder,
                            fontSize = 13.sp
                        )
                    }
                    if (state.photoPath != null) {
                        IconButton(onClick = onPhotoRemove) {
                            Icon(Icons.Default.Close, contentDescription = "Quitar foto", tint = colors.destructive)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            FormFieldLabel("Notas (opcional)")
            AppTextArea(
                value = state.notas,
                onValueChange = onNotasChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = "Agregá una nota...",
                maxLength = maxNotas,
                counterFontSize = notesCounterFontSize
            )

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
            colors = ButtonDefaults.buttonColors(containerColor = colors.success)
        ) {
            Text(submitButtonText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

// Componentes reutilizables

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
                    val categoryColors = cat.categoryChipColors(selected = selected)
                    Surface(
                        onClick = { onSelect(cat) },
                        shape = RoundedCornerShape(8.dp),
                        color = categoryColors.container,
                        border = categoryColors.border?.let {
                            androidx.compose.foundation.BorderStroke(width = 1.dp, color = it)
                        }
                    ) {
                        Box(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                            Text(
                                cat.label,
                                fontSize = 12.sp,
                                color = categoryColors.content,
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
    val colors = TaskPointTheme.colors

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
            placeholder = { Text(placeholder, color = colors.placeholder, fontSize = 14.sp) },
            trailingIcon = {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colors.textSecondary
                )
            },
            isError = isError,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = appTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.fieldBackground)
        ) {
            menuContent { expanded = false }
        }
    }
    if (isError && errorMessage != null) {
        Spacer(Modifier.height(4.dp))
        Text(errorMessage, color = colors.destructive, fontSize = 12.sp)
    }
}
