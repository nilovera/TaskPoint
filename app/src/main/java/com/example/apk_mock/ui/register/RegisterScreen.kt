package com.example.apk_mock.ui.register

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.domain.repository.User
import com.example.apk_mock.ui.components.AppTextField
import com.example.apk_mock.ui.components.AuthBottomLink
import com.example.apk_mock.ui.components.AuthErrorBanner
import com.example.apk_mock.ui.components.AuthHeader
import com.example.apk_mock.ui.components.AuthPrimaryButton
import com.example.apk_mock.ui.theme.TaskPointTheme

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: (User) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = TaskPointTheme.colors

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.onSuccessConsumed()
            state.registeredUser?.let(onRegisterSuccess)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 80.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            AuthHeader(
                title = "Crear cuenta",
                subtitle = "Empezá a organizar tus tareas en segundos."
            )
            Spacer(Modifier.height(34.dp))

            AppTextField(
                label = "Nombre completo",
                value = state.name,
                onValueChange = viewModel::onNameChange,
                placeholder = "Nicolás Perez",
                isError = state.nameError != null
            )
            Spacer(Modifier.height(16.dp))

            AppTextField(
                label = "Correo electrónico",
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = "nico@ejemplo.com",
                keyboardType = KeyboardType.Email,
                isError = state.emailError != null
            )
            Spacer(Modifier.height(16.dp))

            AppTextField(
                label = "Contraseña",
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                isPassword = true,
                isError = state.passwordError != null
            )
            Spacer(Modifier.height(16.dp))

            AppTextField(
                label = "Confirmar contraseña",
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                isPassword = true,
                isError = state.confirmPasswordError != null
            )
            Spacer(Modifier.height(12.dp))

            PasswordStrengthBar(password = state.password)
            val generalError = state.generalError
            if (generalError != null) {
                Spacer(Modifier.height(14.dp))
                AuthErrorBanner(message = generalError)
                Spacer(Modifier.height(14.dp))
            } else {
                Spacer(Modifier.height(28.dp))
            }

            AuthPrimaryButton(
                text = "Crear cuenta ↗",
                onClick = viewModel::onRegisterClick
            )

            Spacer(Modifier.height(20.dp))
            AuthBottomLink(
                text = "¿Ya tenés cuenta? ",
                actionText = "Iniciá sesión",
                onClick = onNavigateToLogin
            )
        }
    }
}

@Composable
fun PasswordStrengthBar(password: String) {
    val colors = TaskPointTheme.colors
    val strength = when {
        password.length >= 10 && password.any { it.isDigit() } && password.any { it.isUpperCase() } -> 3
        password.length >= 6 -> 2
        password.isNotEmpty() -> 1
        else -> 0
    }
    val label = when (strength) {
        1 -> "Contraseña débil"
        2, 3 -> "Contraseña segura"
        else -> ""
    }
    val activeColor = when (strength) {
        1 -> colors.destructive
        else -> colors.success
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(3) { index ->
                val filled = index < strength
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (filled) activeColor else colors.border)
                )
            }
        }
        if (label.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(label, color = activeColor, fontSize = 17.sp)
        }
    }
}
