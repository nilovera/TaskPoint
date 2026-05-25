package com.example.apk_mock.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.ui.theme.AccentBlue
import com.example.apk_mock.ui.theme.BackgroundDark
import com.example.apk_mock.ui.theme.SubtitleGray

private val ProfileLavender = Color(0xFFE4D4FF)
private val MenuBackground = Color(0xFF0B1540)
private val DialogCard = Color(0xFF14182A)
private val DialogBorder = Color(0xFF252B44)
private val DangerText = Color(0xFFFF6E82)

@Composable
fun ProfileMenuButton(
    userName: String,
    onProfile: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayName = userName.ifBlank { "Nicolas Perez" }
    var menuExpanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
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
                contentDescription = "Perfil de $displayName",
                tint = BackgroundDark,
                modifier = Modifier.size(30.dp)
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            containerColor = MenuBackground
        ) {
            Text(
                text = displayName,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
            )
            HorizontalDivider(color = DialogBorder)
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
                text = { Text("Cerrar sesion", color = DangerText, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = null,
                        tint = DangerText,
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

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = DialogCard,
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
                    border = BorderStroke(1.dp, DialogBorder),
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
