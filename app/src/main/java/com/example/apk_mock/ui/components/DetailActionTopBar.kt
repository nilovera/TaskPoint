package com.example.apk_mock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.apk_mock.ui.theme.LabelGray

@Composable
fun DetailActionTopBar(
    title: String,
    onBack: () -> Unit,
    editLabel: String,
    deleteLabel: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    backIconTint: Color = LabelGray,
    actionIconTint: Color = LabelGray
) {
    var menuExpanded by remember { mutableStateOf(false) }

    AppTopBar(
        title = title,
        onBack = onBack,
        modifier = modifier,
        size = AppTopBarSize.Detail,
        titleFontWeight = FontWeight.Bold,
        backIconTint = backIconTint,
        actions = {
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Transparent)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Acciones",
                        tint = actionIconTint
                    )
                }
                AppDropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    AppDropdownMenuItem(
                        text = editLabel,
                        icon = Icons.Default.Edit,
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )
                    AppDropdownMenuItem(
                        text = deleteLabel,
                        icon = Icons.Default.Delete,
                        color = AppDropdownMenuDefaults.DangerText,
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    )
}
