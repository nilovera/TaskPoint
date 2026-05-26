package com.example.apk_mock.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.ui.components.AppTopBar
import com.example.apk_mock.ui.components.AppTopBarSize
import com.example.apk_mock.ui.register.AppTextField
import com.example.apk_mock.ui.theme.AccentBlue
import com.example.apk_mock.ui.theme.BackgroundDark
import com.example.apk_mock.ui.theme.FieldBorder
import com.example.apk_mock.ui.theme.LabelGray
import com.example.apk_mock.ui.theme.PlaceholderGray
import com.example.apk_mock.ui.theme.SubtitleGray
import com.example.apk_mock.ui.theme.SurfaceField

private val ProfileBackground = Color(0xFF090A13)
private val ProfileDialog = Color(0xFF181C2D)
private val ProfileLavender = Color(0xFFE5D6FF)
private val ProfileInk = Color(0xFF1E274F)
private val DangerRed = Color(0xFFE9364B)
private val ErrorBannerBackground = Color(0xFF421118)
private val ErrorBannerBorder = Color(0xFF9E2635)
private val ErrorBannerText = Color(0xFFFF6975)
private val SaveGreen = Color(0xFF41B37F)
private val FieldFill = Color(0xFF191C30)

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    userName: String,
    onBack: () -> Unit,
    onChangePassword: () -> Unit,
    onSessionEnded: () -> Unit,
    innerPadding: PaddingValues = PaddingValues()
) {
    val state by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userName) {
        viewModel.refreshUser(userName)
    }

    LaunchedEffect(state.sessionEnded) {
        if (state.sessionEnded) {
            viewModel.onSessionEndedConsumed()
            onSessionEnded()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackground)
            .padding(horizontal = 27.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = innerPadding.calculateTopPadding() + 30.dp,
                    bottom = innerPadding.calculateBottomPadding() + 18.dp
                )
        ) {
            ProfileTopBar(title = "Mi perfil", onBack = onBack)
            Spacer(Modifier.height(18.dp))

            ProfileAvatar(
                modifier = Modifier
                    .size(132.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(21.dp))
            ProfileReadOnlyField(label = "Nombre completo", value = state.name)
            Spacer(Modifier.height(13.dp))
            ProfileReadOnlyField(label = "Email", value = state.email)
            Spacer(Modifier.height(13.dp))

            Text("Contraseña", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            ProfileActionButton(
                text = "Cambiar contraseña",
                color = AccentBlue,
                onClick = {
                    viewModel.onOpenChangePassword()
                    onChangePassword()
                }
            )

            Spacer(Modifier.height(12.dp))
            Text("Cuenta", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            ProfileActionButton(
                text = "Cerrar sesion",
                color = AccentBlue,
                onClick = { showLogoutDialog = true }
            )
            Spacer(Modifier.height(10.dp))
            ProfileActionButton(
                text = "Eliminar cuenta",
                color = DangerRed,
                onClick = { showDeleteDialog = true }
            )

            if (state.generalError != null) {
                Spacer(Modifier.height(16.dp))
                MessageBanner(text = state.generalError ?: "", color = DangerRed)
            }

            if (state.showPasswordSavedMessage) {
                Spacer(Modifier.height(36.dp))
                MessageBanner(text = "Cambios guardados correctamente.", color = SaveGreen)
            }
        }
    }

    if (showDeleteDialog) {
        ProfileConfirmDialog(
            title = "Eliminar cuenta",
            message = "Estas seguro que queres eliminar tu cuenta?",
            support = "Esta accion no se puede deshacer.",
            confirmText = "Eliminar",
            confirmColor = DangerRed,
            onCancel = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                viewModel.onDeleteAccountConfirmed()
            }
        )
    }

    if (showLogoutDialog) {
        ProfileConfirmDialog(
            title = "Cerrar sesion",
            message = "Estas seguro que deseas cerrar sesion?",
            support = null,
            confirmText = "Confirmar",
            confirmColor = AccentBlue,
            onCancel = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                viewModel.onLogoutConfirmed()
            }
        )
    }
}

@Composable
fun ChangePasswordScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onPasswordChanged: () -> Unit,
    innerPadding: PaddingValues = PaddingValues()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.navigateToProfileAfterPasswordSave) {
        if (state.navigateToProfileAfterPasswordSave) {
            viewModel.onPasswordNavigationConsumed()
            onPasswordChanged()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 27.dp)
            .padding(
                top = innerPadding.calculateTopPadding() + 30.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            )
    ) {
        ProfileTopBar(title = "Contraseña", onBack = onBack)
        Spacer(Modifier.height(28.dp))

        AppTextField(
            label = "Contraseña actual",
            value = state.currentPassword,
            onValueChange = viewModel::onCurrentPasswordChange,
            keyboardType = KeyboardType.Password,
            isPassword = true,
            isError = false
        )
        if (state.currentPasswordError != null) {
            Spacer(Modifier.height(14.dp))
            PasswordErrorBanner(text = state.currentPasswordError ?: "")
        }
        Spacer(Modifier.height(14.dp))

        AppTextField(
            label = "Nueva contraseña",
            value = state.newPassword,
            onValueChange = viewModel::onNewPasswordChange,
            keyboardType = KeyboardType.Password,
            isPassword = true,
            isError = false
        )
        if (state.newPasswordError != null) {
            Spacer(Modifier.height(14.dp))
            PasswordErrorBanner(text = state.newPasswordError ?: "")
        }
        Spacer(Modifier.height(14.dp))

        AppTextField(
            label = "Confirmar nueva contraseña",
            value = state.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            keyboardType = KeyboardType.Password,
            isPassword = true,
            isError = false
        )
        if (state.confirmPasswordError != null) {
            Spacer(Modifier.height(14.dp))
            PasswordErrorBanner(text = state.confirmPasswordError ?: "")
        }

        if (state.generalError != null) {
            Spacer(Modifier.height(14.dp))
            MessageBanner(text = state.generalError ?: "", color = DangerRed)
        }

        Spacer(Modifier.height(24.dp))
        ProfileActionButton(
            text = "Cambiar contraseña",
            color = SaveGreen,
            onClick = viewModel::onSavePasswordClick
        )
    }
}

@Composable
private fun ProfileTopBar(title: String, onBack: () -> Unit) {
    AppTopBar(
        title = title,
        onBack = onBack,
        modifier = Modifier.height(38.dp),
        size = AppTopBarSize.Profile,
        titleFontWeight = FontWeight.Bold
    )
}

@Composable
private fun ProfileAvatar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(ProfileLavender),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = "Avatar de perfil",
            tint = ProfileInk,
            modifier = Modifier.size(116.dp)
        )
    }
}

@Composable
private fun ProfileReadOnlyField(label: String, value: String) {
    Column {
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(FieldFill)
                .border(1.dp, FieldBorder.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(value, color = PlaceholderGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ProfileActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        shape = RoundedCornerShape(7.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MessageBanner(text: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(31.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(color),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 14.dp)
        )
    }
}

@Composable
private fun PasswordErrorBanner(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (text.length > 44) 50.dp else 39.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(ErrorBannerBackground)
            .border(1.dp, ErrorBannerBorder, RoundedCornerShape(9.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = ErrorBannerText,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun ProfileConfirmDialog(
    title: String,
    message: String,
    support: String?,
    confirmText: String,
    confirmColor: Color,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = ProfileDialog,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(message, color = LabelGray, fontSize = 13.sp, lineHeight = 18.sp)
                if (support != null) {
                    Text(support, color = LabelGray, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, FieldBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = BackgroundDark,
                    contentColor = Color.White
                )
            ) {
                Text("Cancelar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
            ) {
                Text(confirmText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    )
}
