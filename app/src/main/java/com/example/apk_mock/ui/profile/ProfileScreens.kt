package com.example.apk_mock.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.example.apk_mock.ui.components.AppDeleteConfirmDialog
import com.example.apk_mock.ui.components.AppLogoutConfirmDialog
import com.example.apk_mock.ui.components.AppTopBar
import com.example.apk_mock.ui.components.AppTopBarSize
import com.example.apk_mock.ui.components.AppTextField
import com.example.apk_mock.ui.theme.TaskPointTheme

private val ProfileFieldHeight = 56.dp
private val ProfileButtonHeight = 65.dp
private val ProfileMessageHeight = 50.dp
private val ProfileLabelSize = 17.sp
private val ProfileValueSize = 19.sp
private val ProfileActionSize = 20.sp
private val ProfileMessageSize = 17.sp

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
    val colors = TaskPointTheme.colors

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
            .background(colors.background)
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

            ProfileSectionLabel("Contraseña")
            Spacer(Modifier.height(6.dp))
            ProfileActionButton(
                text = "Cambiar contraseña",
                color = colors.primary,
                onClick = {
                    viewModel.onOpenChangePassword()
                    onChangePassword()
                }
            )

            Spacer(Modifier.height(12.dp))
            ProfileSectionLabel("Cuenta")
            Spacer(Modifier.height(6.dp))
            ProfileActionButton(
                text = "Cerrar sesion",
                color = colors.primary,
                onClick = { showLogoutDialog = true }
            )
            Spacer(Modifier.height(10.dp))
            ProfileActionButton(
                text = "Eliminar cuenta",
                color = colors.destructive,
                onClick = { showDeleteDialog = true }
            )

            if (state.generalError != null) {
                Spacer(Modifier.height(16.dp))
                MessageBanner(text = state.generalError ?: "", color = colors.destructive)
            }

            if (state.showPasswordSavedMessage) {
                Spacer(Modifier.height(36.dp))
                MessageBanner(text = "Cambios guardados correctamente.", color = colors.success)
            }
        }
    }

    if (showDeleteDialog) {
        AppDeleteConfirmDialog(
            title = "Eliminar cuenta",
            message = "Estas seguro que queres eliminar tu cuenta?",
            support = "Esta accion no se puede deshacer.",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                viewModel.onDeleteAccountConfirmed()
            }
        )
    }

    if (showLogoutDialog) {
        AppLogoutConfirmDialog(
            onDismiss = { showLogoutDialog = false },
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
    val colors = TaskPointTheme.colors

    LaunchedEffect(state.navigateToProfileAfterPasswordSave) {
        if (state.navigateToProfileAfterPasswordSave) {
            viewModel.onPasswordNavigationConsumed()
            onPasswordChanged()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
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
            MessageBanner(text = state.generalError ?: "", color = colors.destructive)
        }

        Spacer(Modifier.height(24.dp))
        ProfileActionButton(
            text = "Cambiar contraseña",
            color = colors.success,
            onClick = viewModel::onSavePasswordClick
        )
    }
}

@Composable
private fun ProfileTopBar(title: String, onBack: () -> Unit) {
    AppTopBar(
        title = title,
        onBack = onBack,
        modifier = Modifier.height(44.dp),
        size = AppTopBarSize.Profile,
        titleFontWeight = FontWeight.Bold
    )
}

@Composable
private fun ProfileAvatar(modifier: Modifier = Modifier) {
    val colors = TaskPointTheme.colors

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(colors.avatarContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = "Avatar de perfil",
            tint = colors.avatarIcon,
            modifier = Modifier.size(116.dp)
        )
    }
}

@Composable
private fun ProfileSectionLabel(text: String) {
    val colors = TaskPointTheme.colors

    Text(
        text = text,
        color = colors.textPrimary,
        fontSize = ProfileLabelSize,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ProfileReadOnlyField(label: String, value: String) {
    val colors = TaskPointTheme.colors

    Column {
        Text(
            text = label,
            color = colors.label,
            fontSize = ProfileLabelSize
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ProfileFieldHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.fieldBackground)
                .border(1.dp, colors.fieldBorder.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = value,
                color = colors.placeholder,
                fontSize = ProfileValueSize
            )
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
            .height(ProfileButtonHeight),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = ProfileActionSize,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MessageBanner(text: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ProfileMessageHeight)
            .clip(RoundedCornerShape(10.dp))
            .background(color),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = ProfileMessageSize,
            modifier = Modifier.padding(horizontal = 14.dp)
        )
    }
}

@Composable
private fun PasswordErrorBanner(text: String) {
    val colors = TaskPointTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (text.length > 44) 60.dp else ProfileMessageHeight)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.errorBackground)
            .border(1.dp, colors.warningText, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = colors.warningText,
            fontSize = ProfileMessageSize,
            lineHeight = 18.sp
        )
    }
}

