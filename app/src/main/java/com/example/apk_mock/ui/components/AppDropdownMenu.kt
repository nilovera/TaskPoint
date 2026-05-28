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
import com.example.apk_mock.ui.theme.TaskPointTheme

object AppDropdownMenuDefaults {
    val Width = 216.dp
    val Shape = RoundedCornerShape(18.dp)
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
    val colors = TaskPointTheme.colors

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.width(width),
        shape = AppDropdownMenuDefaults.Shape,
        containerColor = colors.surface,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        content = content
    )
}

@Composable
fun AppDropdownMenuHeader(text: String) {
    val colors = TaskPointTheme.colors

    Text(
        text = text,
        color = colors.textPrimary,
        fontSize = AppDropdownMenuDefaults.HeaderFontSize,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(AppDropdownMenuDefaults.HeaderPadding)
    )
}

@Composable
fun AppDropdownMenuDivider() {
    HorizontalDivider(color = TaskPointTheme.colors.border)
}

@Composable
fun AppDropdownMenuItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color? = null,
    fontSize: TextUnit = AppDropdownMenuDefaults.ItemFontSize,
    iconSize: Dp = AppDropdownMenuDefaults.ItemIconSize
) {
    val resolvedColor = color ?: TaskPointTheme.colors.textPrimary

    DropdownMenuItem(
        text = { Text(text, color = resolvedColor, fontSize = fontSize) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                tint = resolvedColor,
                modifier = Modifier.size(iconSize)
            )
        },
        onClick = onClick,
        modifier = modifier,
        contentPadding = AppDropdownMenuDefaults.ItemPadding
    )
}
