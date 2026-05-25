package com.example.apk_mock.ui.register

import androidx.lifecycle.ViewModel
import com.example.apk_mock.domain.repository.AuthResult
import com.example.apk_mock.domain.useCase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isSuccess: Boolean = false
)

class RegisterViewModel(private val registerUseCase: RegisterUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v, nameError = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, emailError = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, passwordError = null) }
    fun onConfirmPasswordChange(v: String) = _uiState.update { it.copy(confirmPassword = v, confirmPasswordError = null) }

    fun onRegisterClick() {
        val s = _uiState.value
        // Validación local antes de delegar al use case
        if (s.confirmPassword != s.password) {
            _uiState.update { it.copy(confirmPasswordError = "Las contraseñas no coinciden.") }
            return
        }
        when (val r = registerUseCase(s.name, s.email, s.password)) {
            is AuthResult.Success -> _uiState.update { it.copy(isSuccess = true) }
            is AuthResult.Error   -> _uiState.update { it.copy(emailError = r.message) }
        }
    }

    fun onSuccessConsumed() = _uiState.update { it.copy(isSuccess = false) }
}
