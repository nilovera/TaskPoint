package com.example.apk_mock.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.ui.theme.BackgroundDark
import com.example.apk_mock.ui.theme.FieldBorder
import com.example.apk_mock.ui.theme.LabelGray

@Composable
fun AppConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    confirmColor: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    support: String? = null,
    dismissText: String = "Cancelar",
    containerColor: Color,
    messageColor: Color = LabelGray,
    supportColor: Color = messageColor,
    dismissContainerColor: Color = BackgroundDark,
    dialogCornerRadius: Dp = 16.dp,
    buttonCornerRadius: Dp = 9.dp,
    titleFontSize: TextUnit = 18.sp,
    bodyFontSize: TextUnit = 14.sp,
    bodyLineHeight: TextUnit = 19.sp
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = containerColor,
        shape = RoundedCornerShape(dialogCornerRadius),
        title = {
            Text(
                title,
                color = Color.White,
                fontSize = titleFontSize,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(message, color = messageColor, fontSize = bodyFontSize, lineHeight = bodyLineHeight)
                if (support != null) {
                    Text(support, color = supportColor, fontSize = bodyFontSize, lineHeight = bodyLineHeight)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(buttonCornerRadius),
                border = BorderStroke(1.dp, FieldBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = dismissContainerColor,
                    contentColor = Color.White
                )
            ) {
                Text(dismissText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(buttonCornerRadius),
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
            ) {
                Text(confirmText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    )
}
