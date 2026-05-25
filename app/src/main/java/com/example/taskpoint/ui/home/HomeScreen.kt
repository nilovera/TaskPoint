package com.example.taskpoint.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskpoint.ui.theme.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.text.style.TextAlign

@Composable
fun HomeScreen(
    userName: String,
    onCrearTarea: () -> Unit = {},
    innerPadding: PaddingValues = PaddingValues()
) {
    val today = LocalDate.now()
    val monthName = today.month.getDisplayName(TextStyle.FULL, Locale("es", "AR"))
    val dateLabel = "${today.dayOfMonth} de $monthName · ${today.year}"

    // Un solo Scaffold propio — recibe innerPadding limpio del Scaffold externo
    Scaffold(
        containerColor = BackgroundDark,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCrearTarea,
                containerColor = AccentBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(50)
            ) {
                Text("Nueva tarea", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { selfPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Combinamos el padding del Scaffold externo (bottom bar)
                // con el del Scaffold propio (por si agrega algo)
                .padding(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = selfPadding.calculateBottomPadding(),
                    start = 20.dp,
                    end = 20.dp
                )
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(dateLabel, color = SubtitleGray, fontSize = 13.sp)
                    Text(
                        "Hoy",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SurfaceField),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Perfil de $userName",
                        tint = SubtitleGray,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Empty state ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceField)
                    .padding(vertical = 48.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No tenés tareas\nel día de hoy.",
                        color = SubtitleGray,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onCrearTarea,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue.copy(alpha = 0.25f)
                        )
                    ) {
                        Text(
                            "Crear tarea ↗",
                            color = AccentBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}