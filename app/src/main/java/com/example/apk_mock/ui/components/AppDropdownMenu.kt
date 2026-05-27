package com.example.apk_mock.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AppDropdownMenuDefaults {
    val Width = 216.dp
    val Shape = RoundedCornerShape(18.dp)
    val ContainerColor = Color(0xFF0B1540)
    val DividerColor = Color(0xFF2E3D83)
    val DangerText = Color(0xFFFF6E82)
    val HeaderPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
    val ItemPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
    val HeaderFontSize = 18.sp
    val ItemFontSize = 18.sp
    val ItemIconSize = 24.dp
}

@Composable
fun AppDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = AppDropdownMenuDefaults.Width,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.width(width),
        shape = AppDropdownMenuDefaults.Shape,
        containerColor = AppDropdownMenuDefaults.ContainerColor,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        content = content
    )
}

@Composable
fun AppDropdownMenuHeader(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = AppDropdownMenuDefaults.HeaderFontSize,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(AppDropdownMenuDefaults.HeaderPadding)
    )
}

@Composable
fun AppDropdownMenuDivider() {
    HorizontalDivider(color = AppDropdownMenuDefaults.DividerColor)
}

@Composable
fun AppDropdownMenuItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: TextUnit = AppDropdownMenuDefaults.ItemFontSize,
    iconSize: Dp = AppDropdownMenuDefaults.ItemIconSize
) {
    DropdownMenuItem(
        text = { Text(text, color = color, fontSize = fontSize) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(iconSize)
            )
        },
        onClick = onClick,
        modifier = modifier,
        contentPadding = AppDropdownMenuDefaults.ItemPadding
    )
}
