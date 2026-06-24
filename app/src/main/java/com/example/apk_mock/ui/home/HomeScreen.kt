package com.example.apk_mock.ui.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.Tarea
import com.example.apk_mock.domain.model.perteneceARutina
import com.example.apk_mock.ui.components.AppEmptyStateCard
import com.example.apk_mock.ui.components.CreateActionPill
import com.example.apk_mock.ui.components.MainScreenHeader
import com.example.apk_mock.ui.components.RequirementActionPanel
import com.example.apk_mock.ui.rutinas.RutinasViewModel
import com.example.apk_mock.ui.tareas.TareasViewModel
import com.example.apk_mock.ui.theme.TaskPointTheme
import com.example.apk_mock.ui.theme.categoryChipColors
import com.example.apk_mock.ui.utils.homeDateLabel
import com.example.apk_mock.ui.utils.toDiaSemana
import java.time.LocalDate

private data class HomeRoutineSection(
    val rutina: Rutina?,
    val title: String,
    val subtitle: String,
    val icon: String,
    val iconColor: Color,
    val tareas: List<Tarea>
)

@Composable
fun HomeScreen(
    userName: String,
    rutinasViewModel: RutinasViewModel,
    tareasViewModel: TareasViewModel,
    isInitialDataSyncInProgress: Boolean = false,
    onCrearRutina: () -> Unit,
    onCrearTarea: () -> Unit = {},
    onTaskClick: (String) -> Unit = {},
    onProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    innerPadding: PaddingValues = PaddingValues()
) {
    val rutinasState by rutinasViewModel.listState.collectAsState()
    val tareasState by tareasViewModel.listState.collectAsState()
    val isOnline = rememberIsOnline()
    val today = LocalDate.now()
    val todayDia = today.toDiaSemana()
    val displayName = userName.ifBlank { "Usuario" }
    val rutinas = rutinasState.rutinas
    val todayTasks = tareasState.tareas.filter { it.dia == todayDia || it.dia == null }
    val canCreateTask = rutinas.isNotEmpty()
    val colors = TaskPointTheme.colors
    val sections = remember(rutinas, todayTasks, colors.primary) {
        buildHomeSections(rutinas, todayTasks, colors.primary)
    }

    Scaffold(
        containerColor = colors.background,
        floatingActionButton = {
            CreateActionPill(
                text = "Nueva tarea",
                onClick = onCrearTarea,
                enabled = canCreateTask,
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { selfPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                // Deja lugar para que la última tarjeta pueda desplazarse por encima
                // del botón de crear y de la navegación inferior.
                bottom = innerPadding.calculateBottomPadding() + selfPadding.calculateBottomPadding() + 112.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MainScreenHeader(
                    title = "Hoy",
                    subtitle = today.homeDateLabel(),
                    userName = displayName,
                    onProfile = onProfile,
                    onLogout = onLogout
                )
            }

            if (!isOnline) {
                item { OfflineBanner() }
            }

            when {
                rutinas.isEmpty() && isInitialDataSyncInProgress -> {
                    item {
                        AppEmptyStateCard(
                            message = "Cargando tus rutinas\ny tareas..."
                        )
                    }
                }

                rutinas.isEmpty() -> {
                    item {
                        EmptyTasksCard(showCreateButton = false, onCrearTarea = onCrearTarea)
                    }
                    item {
                        NoRoutinesPanel(onCrearRutina = onCrearRutina)
                    }
                }

                todayTasks.isEmpty() -> {
                    item {
                        EmptyTasksCard(showCreateButton = true, onCrearTarea = onCrearTarea)
                    }
                }

                else -> {
                    items(sections, key = { it.rutina?.id ?: it.title }) { section ->
                        HomeRoutineCard(
                            section = section,
                            onTaskClick = { tarea -> onTaskClick(tarea.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTasksCard(
    showCreateButton: Boolean,
    onCrearTarea: () -> Unit
) {
    AppEmptyStateCard(
        message = "No tenes tareas\nel dia de hoy.",
        actionText = if (showCreateButton) "Crear tarea \u2197" else null,
        onAction = onCrearTarea
    )
}

@Composable
private fun NoRoutinesPanel(onCrearRutina: () -> Unit) {
    RequirementActionPanel(
        message = "No tenes rutinas creadas.",
        actionText = "Carga tu rutina",
        onAction = onCrearRutina
    )
}

@Composable
private fun OfflineBanner() {
    val colors = TaskPointTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.warningBackground)
            .border(1.dp, colors.warningText, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            "NO TENES INTERNET",
            color = colors.warningText,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "Recupera tu conexion para guardar tus cambios",
            color = colors.warningText,
            fontSize = 13.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun HomeRoutineCard(
    section: HomeRoutineSection,
    onTaskClick: (Tarea) -> Unit
) {
    val colors = TaskPointTheme.colors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = colors.routineCard,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(section.iconColor.copy(alpha = 0.34f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(section.icon, fontSize = 17.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        section.title,
                        color = colors.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        section.subtitle,
                        color = colors.textSecondary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                CountChip(section.tareas.size)
            }

            HorizontalDivider(color = colors.border)

            section.tareas.forEachIndexed { index, tarea ->
                HomeTaskRow(
                    tarea = tarea,
                    onClick = { onTaskClick(tarea) }
                )
                if (index < section.tareas.lastIndex) {
                    HorizontalDivider(color = colors.border.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
private fun CountChip(count: Int) {
    val colors = TaskPointTheme.colors
    val isLight = colors.background.luminance() > 0.5f
    val container = if (isLight) colors.primary else colors.subTaskCard
    val border = if (isLight) colors.primary else colors.border

    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(container)
            .border(1.dp, border, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("$count", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HomeTaskRow(
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
                color = if (tarea.requiereRevisionHorario) {
                    colors.warningBackground
                } else {
                    colors.subTaskCard
                }
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tarea.horario ?: "--:--",
            color = colors.textSecondary,
            fontSize = 15.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                tarea.titulo,
                color = colors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(categoryColors.container)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    tarea.categoria.label,
                    color = categoryColors.content,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }

        if (tarea.requiereRevisionHorario) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(colors.warningText.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.WarningAmber,
                    contentDescription = "Requiere revision",
                    tint = colors.warningText,
                    modifier = Modifier.size(19.dp)
                )
            }
        } else if (tarea.categoria.code == "SUPERMERCADO") {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(colors.primary.copy(alpha = 0.28f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    tint = Color(0xFFFFD54A),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun buildHomeSections(
    rutinas: List<Rutina>,
    tareas: List<Tarea>,
    fallbackIconColor: Color
): List<HomeRoutineSection> {
    val routineSections = rutinas.mapNotNull { rutina ->
        val routineTasks = tareas.filter { it.perteneceARutina(rutina) }
        if (routineTasks.isEmpty()) return@mapNotNull null

        HomeRoutineSection(
            rutina = rutina,
            title = rutina.nombre,
            subtitle = listOf(
                "${rutina.horarioInicio} - ${rutina.horarioFin}",
                rutina.direccion
            ).filter { it.isNotBlank() }.joinToString(" · "),
            icon = rutina.icono.emoji,
            iconColor = Color(rutina.icono.colorHex),
            tareas = routineTasks.sortedBy { it.horario ?: "" }
        )
    }

    val unassignedTasks = tareas.filter { tarea ->
        tarea.rutinaId == null && tarea.rutinaNombre == null
    }

    return routineSections + if (unassignedTasks.isNotEmpty()) {
        listOf(
            HomeRoutineSection(
                rutina = null,
                title = "Tareas de hoy",
                subtitle = "Sin rutina asignada",
                icon = "+",
                iconColor = fallbackIconColor,
                tareas = unassignedTasks.sortedBy { it.horario ?: "" }
            )
        )
    } else {
        emptyList()
    }
}

@Composable
private fun rememberIsOnline(): Boolean {
    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(context.hasValidatedConnection()) }

    DisposableEffect(context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mainHandler = Handler(Looper.getMainLooper())
        fun updateOnline(value: Boolean) {
            mainHandler.post { isOnline = value }
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                updateOnline(context.hasValidatedConnection())
            }

            override fun onLost(network: Network) {
                updateOnline(context.hasValidatedConnection())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                updateOnline(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)
        onDispose { connectivityManager.unregisterNetworkCallback(callback) }
    }

    return isOnline
}

private fun Context.hasValidatedConnection(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

