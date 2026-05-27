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
import com.example.apk_mock.ui.theme.AccentBlue
import com.example.apk_mock.ui.theme.ErrorFieldBg
import com.example.apk_mock.ui.theme.ErrorRed
import com.example.apk_mock.ui.theme.SubtitleGray
import com.example.apk_mock.ui.theme.loginAndRegisterBlue

@Composable
fun AuthHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 34.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        lineHeight = 38.sp,
        modifier = modifier
    )
    Text(
        text = subtitle,
        fontSize = 18.sp,
        color = SubtitleGray,
        lineHeight = 20.sp
    )
}

@Composable
fun AuthErrorBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = ErrorFieldBg,
        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.55f))
    ) {
        Text(
            text = message,
            color = ErrorRed,
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
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(65.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
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
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text, color = SubtitleGray, fontSize = 17.sp)
        Text(
            text = actionText,
            color = loginAndRegisterBlue,
            fontSize = 17.sp,
            modifier = Modifier.clickable { onClick() }
        )
    }
}
