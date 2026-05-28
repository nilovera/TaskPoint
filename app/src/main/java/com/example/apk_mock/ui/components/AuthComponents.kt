package com.example.apk_mock.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.ui.theme.TaskPointTheme

@Composable
fun AuthHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val colors = TaskPointTheme.colors

    Text(
        text = title,
        fontSize = 34.sp,
        fontWeight = FontWeight.Bold,
        color = colors.textPrimary,
        lineHeight = 38.sp,
        modifier = modifier
    )
    Text(
        text = subtitle,
        fontSize = 18.sp,
        color = colors.textSecondary,
        lineHeight = 20.sp
    )
}

@Composable
fun AuthErrorBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    val colors = TaskPointTheme.colors

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = colors.errorBackground,
        border = BorderStroke(1.dp, colors.destructive.copy(alpha = 0.55f))
    ) {
        Text(
            text = message,
            color = colors.destructive,
            fontSize = 17.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}

@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = TaskPointTheme.colors

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(65.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
fun AuthBottomLink(
    text: String,
    actionText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = TaskPointTheme.colors

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text, color = colors.textSecondary, fontSize = 17.sp)
        Text(
            text = actionText,
            color = colors.primary,
            fontSize = 17.sp,
            modifier = Modifier.clickable { onClick() }
        )
    }
}
