package com.example.apk_mock.ui.forgotPassword

import androidx.lifecycle.ViewModel
import com.example.apk_mock.domain.repository.ResetResult
import com.example.apk_mock.domain.useCase.ChangePasswordUseCase
import com.example.apk_mock.domain.useCase.SendResetCodeUseCase
import com.example.apk_mock.domain.useCase.VerifyResetCodeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ── UiState ──────────────────────────────────────────────────────────────────

data class ForgotPasswordUiState(
    // Step 1 – email
    val email: String = "",
    val emailError: String? = null,
    val codeSent: Boolean = false,

    // Step 2 – code (6 digits as list)
    val codeDigits: List<String> = List(6) { "" },
    val codeError: String? = null,
    val codeVerified: Boolean = false,

    // Step 3 – new password
    val newPassword: String = "",
    val confirmPassword: String = "",
    val newPasswordError: String? = null,
    val passwordChanged: Boolean = false
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class ForgotPasswordViewModel(
    private val sendCode: SendResetCodeUseCase,
    private val verifyCode: VerifyResetCodeUseCase,
    private val changePassword: ChangePasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    // Step 1
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, emailError = null) }

    fun onSendCode() {
        when (val r = sendCode(_uiState.value.email)) {
            is ResetResult.CodeSent  -> _uiState.update { it.copy(codeSent = true, emailError = null) }
            is ResetResult.Error     -> _uiState.update { it.copy(emailError = r.message) }
            else                     -> Unit
        }
    }

    // Step 2
    fun onDigitChange(index: Int, value: String) {
        val digits = _uiState.value.codeDigits.toMutableList()
        digits[index] = value.takeLast(1)
        _uiState.update { it.copy(codeDigits = digits, codeError = null) }
    }

    fun onVerifyCode() {
        val code = _uiState.value.codeDigits.joinToString("")
        when (val r = verifyCode(_uiState.value.email, code)) {
            is ResetResult.CodeValid -> _uiState.update { it.copy(codeVerified = true, codeError = null) }
            is ResetResult.Error     -> _uiState.update { it.copy(codeError = r.message) }
            else                     -> Unit
        }
    }

    // Step 3
    fun onNewPasswordChange(v: String) = _uiState.update { it.copy(newPassword = v, newPasswordError = null) }
    fun onConfirmPasswordChange(v: String) = _uiState.update { it.copy(confirmPassword = v, newPasswordError = null) }

    fun onChangePassword() {
        val s = _uiState.value
        when (val r = changePassword(s.email, s.newPassword, s.confirmPassword)) {
            is ResetResult.PasswordChanged -> _uiState.update { it.copy(passwordChanged = true) }
            is ResetResult.Error           -> _uiState.update { it.copy(newPasswordError = r.message) }
            else                           -> Unit
        }
    }

    fun consumePasswordChanged() = _uiState.update { it.copy(passwordChanged = false) }
}
