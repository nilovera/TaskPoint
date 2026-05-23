package com.example.apk_mock.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.ui.register.AppTextField
import com.example.apk_mock.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToHome: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val hasError = state.errorMessage != null

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.onSuccessConsumed()
            onNavigateToHome(state.loggedInName)
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
                .padding(top = 48.dp, bottom = 32.dp)
        ) {
            Text(
                text = "Bienvenido de\nnuevo",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 38.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Ingresá a tu cuenta para ver tus recordatorios.",
                fontSize = 14.sp,
                color = SubtitleGray,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(28.dp))

            AppTextField(
                label = "Correo electrónico",
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = "nico@ejemplo.com",
                keyboardType = KeyboardType.Email,
                isError = hasError
            )
            Spacer(Modifier.height(16.dp))

            AppTextField(
                label = "Contraseña",
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                isPassword = true,
                isError = hasError
            )

            if (hasError) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ErrorBg, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(state.errorMessage ?: "", color = ErrorRed, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    color = AccentBlue,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onNavigateToForgotPassword() }
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = viewModel::onLoginClick,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text(
                    text = if (hasError) "Intentar de nuevo" else "Iniciar sesión ↗",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¿No tenés cuenta? ", color = SubtitleGray, fontSize = 14.sp)
                Text(
                    text = "Registrate",
                    color = AccentBlue,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
    }
}