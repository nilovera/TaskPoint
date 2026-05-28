package com.example.apk_mock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.ui.theme.TaskPointTheme

enum class AppTopBarSize(
    val buttonSize: Dp,
    val iconSize: Dp,
    val titleSize: androidx.compose.ui.unit.TextUnit,
    val cornerRadius: Dp,
    val bordered: Boolean
) {
    Compact(36.dp, 24.dp, 22.sp, 10.dp, false),
    Regular(48.dp, 24.dp, 20.sp, 14.dp, true),
    Detail(48.dp, 24.dp, 22.sp, 14.dp, true),
    Profile(34.dp, 20.dp, 20.sp, 10.dp, false)
}

@Composable
fun AppTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    size: AppTopBarSize = AppTopBarSize.Regular,
    titleFontWeight: FontWeight = FontWeight.SemiBold,
    backIconTint: Color? = null,
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    val colors = TaskPointTheme.colors
    val resolvedBackIconTint = backIconTint ?: if (size == AppTopBarSize.Detail) {
        colors.label
    } else {
        colors.textSecondary
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppTopBarBackButton(
            onBack = onBack,
            size = size,
            tint = resolvedBackIconTint
        )

        Text(
            text = title,
            color = colors.textPrimary,
            fontSize = size.titleSize,
            fontWeight = titleFontWeight,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        if (actions != null) {
            actions()
        } else {
            Spacer(Modifier.size(size.buttonSize))
        }
    }
}

@Composable
private fun AppTopBarBackButton(
    onBack: () -> Unit,
    size: AppTopBarSize,
    tint: Color
) {
    val colors = TaskPointTheme.colors
    val shape = RoundedCornerShape(size.cornerRadius)
    val baseModifier = Modifier
        .size(size.buttonSize)
        .clip(shape)
        .background(
            if (size == AppTopBarSize.Profile) colors.surface.copy(alpha = 0.45f) else colors.surface
        )

    IconButton(
        onClick = onBack,
        modifier = if (size.bordered) {
            baseModifier.border(1.dp, colors.border, shape)
        } else {
            baseModifier
        }
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Volver",
            tint = tint,
            modifier = Modifier.size(size.iconSize)
        )
    }
}
