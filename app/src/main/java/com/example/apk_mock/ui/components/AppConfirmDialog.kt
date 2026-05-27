package com.example.apk_mock.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.apk_mock.ui.theme.AccentBlue
import com.example.apk_mock.ui.theme.CancelRed
import com.example.apk_mock.ui.theme.FieldBorder
import com.example.apk_mock.ui.theme.LabelGray
import com.example.apk_mock.ui.theme.SubtitleGray
import com.example.apk_mock.ui.theme.SurfaceField

object AppConfirmDialogDefaults {
    val OverlayColor = Color.Black.copy(alpha = 0.62f)
    val ContainerColor = SurfaceField
    val DestructiveColor = CancelRed
    val DialogCornerRadius = 18.dp
    val ButtonCornerRadius = 12.dp
    val TitleFontSize = 20.sp
    val BodyFontSize = 15.sp
    val BodyLineHeight = 20.sp
    val ButtonHeight = 48.dp
    val HorizontalPadding = 28.dp
    val TopPadding = 92.dp
    val BottomPadding = 128.dp
    val CardPadding = 22.dp
    val ButtonGap = 52.dp
}

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
    containerColor: Color = AppConfirmDialogDefaults.ContainerColor,
    messageColor: Color = LabelGray,
    supportColor: Color = messageColor,
    dismissContainerColor: Color = Color.Transparent,
    dialogCornerRadius: Dp = AppConfirmDialogDefaults.DialogCornerRadius,
    buttonCornerRadius: Dp = AppConfirmDialogDefaults.ButtonCornerRadius,
    titleFontSize: TextUnit = AppConfirmDialogDefaults.TitleFontSize,
    bodyFontSize: TextUnit = AppConfirmDialogDefaults.BodyFontSize,
    bodyLineHeight: TextUnit = AppConfirmDialogDefaults.BodyLineHeight
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppConfirmDialogDefaults.OverlayColor)
                .padding(horizontal = AppConfirmDialogDefaults.HorizontalPadding)
                .padding(
                    top = AppConfirmDialogDefaults.TopPadding,
                    bottom = AppConfirmDialogDefaults.BottomPadding
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                color = containerColor,
                shape = RoundedCornerShape(dialogCornerRadius),
                border = BorderStroke(1.dp, FieldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(AppConfirmDialogDefaults.CardPadding)) {
                    Text(
                        title,
                        color = Color.White,
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(18.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            message,
                            color = messageColor,
                            fontSize = bodyFontSize,
                            lineHeight = bodyLineHeight
                        )
                        if (support != null) {
                            Text(
                                support,
                                color = supportColor,
                                fontSize = bodyFontSize,
                                lineHeight = bodyLineHeight
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .height(AppConfirmDialogDefaults.ButtonHeight)
                                .weight(1f),
                            shape = RoundedCornerShape(buttonCornerRadius),
                            border = BorderStroke(1.dp, FieldBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = dismissContainerColor,
                                contentColor = Color.White
                            )
                        ) {
                            Text(dismissText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.width(AppConfirmDialogDefaults.ButtonGap))
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier
                                .height(AppConfirmDialogDefaults.ButtonHeight)
                                .weight(1f),
                            shape = RoundedCornerShape(buttonCornerRadius),
                            colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
                        ) {
                            Text(confirmText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppDeleteConfirmDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    support: String? = "Esta accion no se puede deshacer.",
    confirmText: String = "Eliminar"
) {
    AppConfirmDialog(
        title = title,
        message = message,
        support = support,
        confirmText = confirmText,
        confirmColor = AppConfirmDialogDefaults.DestructiveColor,
        messageColor = LabelGray,
        supportColor = SubtitleGray,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@Composable
fun AppLogoutConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AppConfirmDialog(
        title = "Cerrar sesion",
        message = "Estas seguro que deseas cerrar sesion?",
        support = null,
        confirmText = "Confirmar",
        confirmColor = AccentBlue,
        messageColor = LabelGray,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}
