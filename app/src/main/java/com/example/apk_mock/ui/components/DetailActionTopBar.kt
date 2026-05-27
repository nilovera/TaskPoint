package com.example.apk_mock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    deleteColor: Color,
    backIconTint: Color = LabelGray,
    actionIconTint: Color = LabelGray,
    menuContainerColor: Color = Color(0xFF0F1450),
    menuBorderColor: Color? = Color(0xFF161D68),
    itemFontSize: TextUnit = 14.sp,
    itemIconSize: Dp = 18.dp
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
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = menuContainerColor,
                    modifier = menuBorderColor?.let {
                        Modifier.border(1.dp, it, RoundedCornerShape(12.dp))
                    } ?: Modifier
                ) {
                    DropdownMenuItem(
                        text = { Text(editLabel, color = Color.White, fontSize = itemFontSize) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(itemIconSize)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(deleteLabel, color = deleteColor, fontSize = itemFontSize) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = deleteColor,
                                modifier = Modifier.size(itemIconSize)
                            )
                        },
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
