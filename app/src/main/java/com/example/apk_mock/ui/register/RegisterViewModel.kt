package com.example.apk_mock.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apk_mock.domain.repository.AuthRepository
import com.example.apk_mock.domain.repository.AuthResult
import com.example.apk_mock.domain.repository.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val registeredUser: User? = null,
    val isSuccess: Boolean = false
)

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update {
        it.copy(name = value, nameError = null, generalError = null)
    }

    fun onEmailChange(value: String) = _uiState.update {
        it.copy(email = value, emailError = null, generalError = null)
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(password = value, passwordError = null, generalError = null)
    }

    fun onConfirmPasswordChange(value: String) = _uiState.update {
        it.copy(confirmPassword = value, confirmPasswordError = null, generalError = null)
    }

    fun onRegisterClick() {
        val state = _uiState.value

        validateFirstError(state)?.let { error ->
            _uiState.update {
                it.copy(
                    nameError = if (error.field == RegisterField.Name) error.message else null,
                    emailError = if (error.field == RegisterField.Email) error.message else null,
                    passwordError = if (error.field == RegisterField.Password) error.message else null,
                    confirmPasswordError = if (error.field == RegisterField.ConfirmPassword) error.message else null,
                    generalError = error.message
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            when (val result = repository.register(state.name.trim(), state.email.trim(), state.password)) {
                is AuthResult.Success -> _uiState.update {
                    it.copy(isLoading = false, registeredUser = result.user, isSuccess = true)
                }
                is AuthResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        nameError = null,
                        emailError = result.message,
                        passwordError = null,
                        confirmPasswordError = null,
                        generalError = result.message
                    )
                }
            }
        }
    }

    fun onSuccessConsumed() = _uiState.update { it.copy(isSuccess = false) }

    private fun validateFirstError(state: RegisterUiState): RegisterValidationError? {
        if (state.name.isBlank()) {
            return RegisterValidationError(RegisterField.Name, "Ingresá tu nombre completo.")
        }
        if (state.email.isBlank()) {
            return RegisterValidationError(RegisterField.Email, "Ingresá tu correo electrónico.")
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            return RegisterValidationError(RegisterField.Email, "Ingresá un correo válido.")
        }
        if (state.password.isBlank()) {
            return RegisterValidationError(RegisterField.Password, "Ingresá una contraseña.")
        }
        if (state.password.length < 6) {
            return RegisterValidationError(RegisterField.Password, "La contraseña debe tener al menos 6 caracteres.")
        }
        if (state.confirmPassword.isBlank()) {
            return RegisterValidationError(RegisterField.ConfirmPassword, "Confirmá tu contraseña.")
        }
        if (state.confirmPassword != state.password) {
            return RegisterValidationError(RegisterField.ConfirmPassword, "Las contraseñas no coinciden.")
        }
        return null
    }
}

private enum class RegisterField {
    Name,
    Email,
    Password,
    ConfirmPassword
}

private data class RegisterValidationError(
    val field: RegisterField,
    val message: String
)
