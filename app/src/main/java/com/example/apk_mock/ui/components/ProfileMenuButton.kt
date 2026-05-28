package com.example.apk_mock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.apk_mock.ui.theme.TaskPointTheme

@Composable
fun ProfileMenuButton(
    userName: String,
    onProfile: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayName = userName.ifBlank { "Nicolas Perez" }
    val colors = TaskPointTheme.colors
    var menuExpanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.avatarContainer)
                .clickable { menuExpanded = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Perfil de $displayName",
                tint = colors.avatarIcon,
                modifier = Modifier.size(30.dp)
            )
        }

        AppDropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            AppDropdownMenuHeader(displayName)
            AppDropdownMenuDivider()
            AppDropdownMenuItem(
                text = "Mi perfil",
                icon = Icons.Default.AccountCircle,
                onClick = {
                    menuExpanded = false
                    onProfile()
                }
            )
            AppDropdownMenuItem(
                text = "Cerrar sesion",
                icon = Icons.AutoMirrored.Filled.Logout,
                color = colors.destructive,
                onClick = {
                    menuExpanded = false
                    showLogoutDialog = true
                }
            )
        }
    }

    if (showLogoutDialog) {
        AppLogoutConfirmDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            }
        )
    }
}
