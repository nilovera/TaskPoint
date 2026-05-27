package com.example.apk_mock.ui.register

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.ui.components.AppTextField
import com.example.apk_mock.ui.theme.*

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.onSuccessConsumed()
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 48.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = "Crear cuenta",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Empezá a organizar tus tareas en segundos.",
                fontSize = 14.sp,
                color = SubtitleGray,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(28.dp))

            // Nombre completo
            AppTextField(
                label = "Nombre completo",
                value = state.name,
                onValueChange = viewModel::onNameChange,
                placeholder = "Nicolás Perez",
                isError = state.nameError != null,
                errorMessage = state.nameError
            )
            Spacer(Modifier.height(16.dp))

            // Correo electrónico
            AppTextField(
                label = "Correo electrónico",
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = "nico@ejemplo.com",
                keyboardType = KeyboardType.Email,
                isError = state.emailError != null,
                errorMessage = state.emailError
            )
            Spacer(Modifier.height(16.dp))

            // Contraseña
            AppTextField(
                label = "Contraseña",
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                isPassword = true,
                isError = state.passwordError != null,
                errorMessage = state.passwordError
            )
            Spacer(Modifier.height(16.dp))

            // Confirmar contraseña
            AppTextField(
                label = "Confirmar contraseña",
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                isPassword = true,
                isError = state.confirmPasswordError != null,
                errorMessage = state.confirmPasswordError
            )
            Spacer(Modifier.height(12.dp))

            // Password strength bar
            PasswordStrengthBar(password = state.password)
            Spacer(Modifier.height(28.dp))

            // Crear cuenta button
            Button(
                onClick = viewModel::onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text(
                    text = "Crear cuenta ↗",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("¿Ya tenés cuenta? ", color = SubtitleGray, fontSize = 14.sp)
                Text(
                    text = "Iniciá sesión",
                    color = AccentBlue,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

@Composable
fun PasswordStrengthBar(password: String) {
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
        1 -> StrengthRed
        else -> StrengthGreen
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
                        .background(if (filled) activeColor else SurfaceField)
                )
            }
        }
        if (label.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(label, color = activeColor, fontSize = 12.sp)
        }
    }
}

