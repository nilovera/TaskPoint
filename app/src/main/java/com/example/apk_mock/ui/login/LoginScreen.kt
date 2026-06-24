package com.example.apk_mock.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToHome: (User) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = TaskPointTheme.colors
    val errorMessage = state.errorMessage
    val hasError = errorMessage != null

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.onSuccessConsumed()
            state.loggedInUser?.let(onNavigateToHome)
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
                .padding(top = 80.dp, bottom = 32.dp)
        ) {
            AuthHeader(
                title = "Bienvenido de\nnuevo",
                subtitle = "Ingresá a tu cuenta para ver tus recordatorios."
            )
            Spacer(Modifier.height(34.dp))

            AppTextField(
                label = "Correo electrónico",
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = "usuario@ejemplo.com",
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

            if (errorMessage != null) {
                Spacer(Modifier.height(14.dp))
                AuthErrorBanner(message = errorMessage)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    color = colors.primary,
                    fontSize = 17.sp,
                    modifier = Modifier.clickable { onNavigateToForgotPassword() }
                )
            }

            Spacer(Modifier.height(20.dp))

            AuthPrimaryButton(
                text = if(errorMessage != null) "Intente de nuevo" else "Iniciar sesión ↗",
                onClick = viewModel::onLoginClick
            )

            Spacer(Modifier.height(20.dp))
            AuthBottomLink(
                text = "¿No tenés cuenta? ",
                actionText = "Registrate",
                onClick = onNavigateToRegister
            )
        }
    }
}
