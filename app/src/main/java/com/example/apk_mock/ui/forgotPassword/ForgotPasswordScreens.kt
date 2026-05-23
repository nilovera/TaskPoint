package com.example.apk_mock.ui.forgotPassword

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.ui.register.AppTextField
import com.example.apk_mock.ui.register.PasswordStrengthBar
import com.example.apk_mock.ui.theme.*

// ═══════════════════════════════════════════════════════════════════════════════
// Paso 1 — Ingresar email
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ForgotPasswordEmailScreen(
    viewModel: ForgotPasswordViewModel,
    onCancel: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // Cuando el código fue enviado, avanzamos al paso 2
    if (state.codeSent) {
        ForgotPasswordCodeScreen(viewModel = viewModel, onCancel = onCancel)
        return
    }

    ForgotPasswordScaffold(
        title = "¿Olvidaste tu\ncontraseña?",
        subtitle = "¡Ingresá tu mail para comenzar con la recuperación!\nSe enviará un token de recuperación al mail ingresado.",
        onCancel = onCancel
    ) {
        AppTextField(
            label = "Correo electrónico",
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = "nico@ejemplo.com",
            keyboardType = KeyboardType.Email,
            isError = state.emailError != null,
            errorMessage = state.emailError
        )
        Spacer(Modifier.height(28.dp))
        PrimaryButton(text = "Enviar mail", onClick = viewModel::onSendCode)
        Spacer(Modifier.height(12.dp))
        CancelButton(onClick = onCancel)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Paso 2 — Ingresar código de 6 dígitos
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ForgotPasswordCodeScreen(
    viewModel: ForgotPasswordViewModel,
    onCancel: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    if (state.codeVerified) {
        ForgotPasswordNewPasswordScreen(viewModel = viewModel, onCancel = onCancel)
        return
    }

    ForgotPasswordScaffold(
        title = "¿Olvidaste tu\ncontraseña?",
        subtitle = "Ingresá el token de recuperación para cambiar tu contraseña.",
        onCancel = onCancel
    ) {
        Text("Código de recuperación", color = LabelGray, fontSize = 13.sp)
        Spacer(Modifier.height(10.dp))
        OtpInputRow(
            digits = state.codeDigits,
            onDigitChange = viewModel::onDigitChange,
            isError = state.codeError != null
        )
        if (state.codeError != null) {
            Spacer(Modifier.height(6.dp))
            Text(state.codeError!!, color = ErrorRed, fontSize = 12.sp)
        }
        Spacer(Modifier.height(28.dp))
        PrimaryButton(text = "Continuar", onClick = viewModel::onVerifyCode)
        Spacer(Modifier.height(12.dp))
        CancelButton(onClick = onCancel)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Paso 3 — Nueva contraseña
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ForgotPasswordNewPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onCancel: () -> Unit,
    onPasswordChanged: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.passwordChanged) {
        if (state.passwordChanged) {
            viewModel.consumePasswordChanged()
            onPasswordChanged?.invoke() ?: onCancel()
        }
    }

    ForgotPasswordScaffold(
        title = "¿Olvidaste tu\ncontraseña?",
        subtitle = "Ingresá tu nueva contraseña.",
        onCancel = onCancel
    ) {
        AppTextField(
            label = "Contraseña",
            value = state.newPassword,
            onValueChange = viewModel::onNewPasswordChange,
            isPassword = true,
            isError = state.newPasswordError != null
        )
        Spacer(Modifier.height(16.dp))
        AppTextField(
            label = "Confirmar contraseña",
            value = state.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            isPassword = true,
            isError = state.newPasswordError != null,
            errorMessage = state.newPasswordError
        )
        Spacer(Modifier.height(8.dp))
        PasswordStrengthBar(password = state.newPassword)
        Spacer(Modifier.height(28.dp))
        // Botón verde como en el diseño
        Button(
            onClick = viewModel::onChangePassword,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StrengthGreen)
        ) {
            Text("Confirmar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Spacer(Modifier.height(12.dp))
        CancelButton(onClick = onCancel)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Componentes compartidos del flujo
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ForgotPasswordScaffold(
    title: String,
    subtitle: String,
    onCancel: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
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
                text = title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 38.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = SubtitleGray,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(28.dp))
            content()
        }
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

@Composable
private fun CancelButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CancelRed)
    ) {
        Text("Cancelar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

// ── OTP input row ─────────────────────────────────────────────────────────────

@Composable
fun OtpInputRow(
    digits: List<String>,
    onDigitChange: (Int, String) -> Unit,
    isError: Boolean
) {
    val focusRequesters = remember { List(6) { FocusRequester() } }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        digits.forEachIndexed { index, digit ->
            OutlinedTextField(
                value = digit,
                onValueChange = { value ->
                    val clean = value.filter { it.isDigit() }.takeLast(1)
                    onDigitChange(index, clean)
                    if (clean.isNotEmpty() && index < 5) {
                        focusRequesters[index + 1].requestFocus()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(0.85f)
                    .focusRequester(focusRequesters[index]),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceField,
                    unfocusedContainerColor = SurfaceField,
                    focusedBorderColor = if (isError) ErrorRed else AccentBlue,
                    unfocusedBorderColor = if (digit.isNotEmpty()) AccentBlue else FieldBorder,
                    errorBorderColor = ErrorRed,
                    cursorColor = AccentBlue
                )
            )
        }
    }
}