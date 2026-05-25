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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.Tarea
import com.example.apk_mock.ui.rutinas.RutinasViewModel
import com.example.apk_mock.ui.tareas.TareasViewModel
import com.example.apk_mock.ui.theme.AccentBlue
import com.example.apk_mock.ui.theme.BackgroundDark
import com.example.apk_mock.ui.theme.SubtitleGray
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val HomeBackground = Color(0xFF080B12)
private val HomeCard = Color(0xFF14182A)
private val HomeCardBorder = Color(0xFF252B44)
private val HomeChip = Color(0xFF0C101D)
private val MenuBackground = Color(0xFF0B1540)
private val ProfileLavender = Color(0xFFE4D4FF)
private val OfflineBackground = Color(0xFF551017)
private val OfflineBorder = Color(0xFFA93244)
private val OfflineText = Color(0xFFFF6E82)

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
    onCrearRutina: () -> Unit,
    onCrearTarea: () -> Unit = {},
    onProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    innerPadding: PaddingValues = PaddingValues()
) {
    val rutinasState by rutinasViewModel.listState.collectAsState()
    val tareasState by tareasViewModel.listState.collectAsState()
    val isOnline = rememberIsOnline()
    val today = LocalDate.now()
    val todayDia = today.toDiaSemana()
    val displayName = userName.ifBlank { "Nicolas Perez" }
    val rutinas = rutinasState.rutinas
    val todayTasks = tareasState.tareas.filter { it.dia == todayDia || it.dia == null }
    val canCreateTask = rutinas.isNotEmpty() || tareasState.tareas.isNotEmpty()
    val sections = remember(rutinas, todayTasks) { buildHomeSections(rutinas, todayTasks) }

    LaunchedEffect(Unit) {
        rutinasViewModel.refreshRutinas()
        tareasViewModel.refreshTareas()
    }

    Scaffold(
        containerColor = HomeBackground,
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (canCreateTask) AccentBlue else Color(0xFF4E5562))
                    .clickable(enabled = canCreateTask, onClick = onCrearTarea)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Nueva tarea +",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { selfPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeBackground)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 34.dp,
                bottom = innerPadding.calculateBottomPadding() + selfPadding.calculateBottomPadding() + 86.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HomeHeader(
                    dateLabel = today.homeDateLabel(),
                    userName = displayName,
                    onProfile = onProfile,
                    onLogout = onLogout
                )
            }

            if (!isOnline) {
                item { OfflineBanner() }
            }

            when {
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
                        HomeRoutineCard(section = section)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    dateLabel: String,
    userName: String,
    onProfile: () -> Unit,
    onLogout: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(dateLabel, color = SubtitleGray, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Text("Hoy", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        }

        Box {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ProfileLavender)
                    .clickable { menuExpanded = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Perfil de $userName",
                    tint = HomeBackground,
                    modifier = Modifier.size(30.dp)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                containerColor = MenuBackground
            ) {
                Text(
                    text = userName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
                HorizontalDivider(color = HomeCardBorder)
                DropdownMenuItem(
                    text = { Text("Mi perfil", color = Color.White, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onProfile()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Cerrar sesion", color = OfflineText, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = null,
                            tint = OfflineText,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        showLogoutDialog = true
                    }
                )
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = HomeCard,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    "Cerrar sesion",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Estas seguro que deseas cerrar sesion?",
                    color = SubtitleGray,
                    fontSize = 14.sp,
                    lineHeight = 19.sp
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape = RoundedCornerShape(9.dp),
                    border = BorderStroke(1.dp, HomeCardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Cancelar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    shape = RoundedCornerShape(9.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Confirmar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun EmptyTasksCard(
    showCreateButton: Boolean,
    onCrearTarea: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (showCreateButton) 158.dp else 156.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(HomeCard)
            .border(1.dp, HomeCardBorder, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No tenes tareas\nel dia de hoy.",
                color = SubtitleGray.copy(alpha = 0.72f),
                fontSize = 14.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            if (showCreateButton) {
                Spacer(Modifier.height(26.dp))
                OutlinedButton(
                    onClick = onCrearTarea,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.65f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = AccentBlue.copy(alpha = 0.18f),
                        contentColor = AccentBlue
                    )
                ) {
                    Text("Crear tarea \u2197", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun NoRoutinesPanel(onCrearRutina: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(OfflineBackground)
                .border(1.dp, OfflineBorder, RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No tenes rutinas creadas.",
                color = OfflineText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Button(
            onClick = onCrearRutina,
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            shape = RoundedCornerShape(7.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) {
            Text("Carga tu rutina", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun OfflineBanner() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(OfflineBackground)
            .border(1.dp, OfflineBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            "NO TENES INTERNET",
            color = OfflineText,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "Recupera tu conexion para guardar tus cambios",
            color = OfflineText,
            fontSize = 13.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun HomeRoutineCard(section: HomeRoutineSection) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = HomeCard,
        border = BorderStroke(1.dp, HomeCardBorder)
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
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        section.subtitle,
                        color = SubtitleGray,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                CountChip(section.tareas.size)
            }

            HorizontalDivider(color = HomeCardBorder)

            section.tareas.forEachIndexed { index, tarea ->
                HomeTaskRow(tarea = tarea)
                if (index < section.tareas.lastIndex) {
                    HorizontalDivider(color = HomeCardBorder.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
private fun CountChip(count: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(HomeChip)
            .border(1.dp, HomeCardBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("$count", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HomeTaskRow(tarea: Tarea) {
    val categoryColor = categoriaColor(tarea.categoria)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tarea.horario ?: "--:--",
            color = SubtitleGray,
            fontSize = 11.sp,
            modifier = Modifier.width(38.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                tarea.titulo,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(categoryColor.copy(alpha = 0.28f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    tarea.categoria.label,
                    color = categoryColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }

        if (tarea.categoria.code == "SUPERMERCADO") {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(AccentBlue.copy(alpha = 0.28f)),
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
    tareas: List<Tarea>
): List<HomeRoutineSection> {
    val routineSections = rutinas.mapNotNull { rutina ->
        val routineTasks = tareas.filter { it.rutinaId == rutina.id || it.rutinaNombre == rutina.nombre }
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
                iconColor = AccentBlue,
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

private fun LocalDate.homeDateLabel(): String {
    val monthName = month.getDisplayName(TextStyle.FULL, Locale("es", "AR"))
    return "$dayOfMonth de $monthName · $year"
}

private fun LocalDate.toDiaSemana(): DiaSemana = when (dayOfWeek) {
    DayOfWeek.MONDAY -> DiaSemana.LUN
    DayOfWeek.TUESDAY -> DiaSemana.MAR
    DayOfWeek.WEDNESDAY -> DiaSemana.MIE
    DayOfWeek.THURSDAY -> DiaSemana.JUE
    DayOfWeek.FRIDAY -> DiaSemana.VIE
    DayOfWeek.SATURDAY -> DiaSemana.SAB
    DayOfWeek.SUNDAY -> DiaSemana.DOM
}

private fun categoriaColor(cat: CategoriaTarea): Color = when (cat.code) {
    "PERSONAL" -> Color(0xFF5E7CFF)
    "SUPERMERCADO" -> Color(0xFF35D07F)
    "INDUMENTARIA" -> Color(0xFFE85D75)
    "FACULTAD" -> Color(0xFFD79728)
    "ESTUDIO" -> Color(0xFF31B7D7)
    "FARMACIA", "MEDICO" -> Color(0xFFE85D75)
    "GIMNASIO" -> Color(0xFF35D07F)
    "BANCO", "TRANSPORTE" -> Color(0xFF5E7CFF)
    "ESCUELA", "LIBRERIA" -> Color(0xFFD79728)
    "VETERINARIA", "FERRETERIA", "PANADERIA", "PELUQUERIA" -> Color(0xFFFF9F0A)
    "CASA" -> Color(0xFF31B7D7)
    else -> Color(0xFF8A8FA8)
}
