package com.example.apk_mock.ui.profile

import androidx.lifecycle.ViewModel
import com.example.apk_mock.domain.repository.AuthRepository
import com.example.apk_mock.domain.repository.ProfileResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProfileUiState(
    val name: String = "Nicolas Perez",
    val email: String = "nico@ejemplo.com",
    val passwordMask: String = "••••••••",
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val showPasswordSavedMessage: Boolean = false,
    val navigateToProfileAfterPasswordSave: Boolean = false,
    val sessionEnded: Boolean = false
)

class ProfileViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun refreshUser(fallbackName: String = "") {
        val user = repository.currentUser()
        if (user != null) {
            _uiState.update {
                it.copy(
                    name = user.name,
                    email = user.email,
                    passwordMask = "•".repeat(8)
                )
            }
        } else if (fallbackName.isNotBlank()) {
            _uiState.update { it.copy(name = fallbackName) }
        }
    }

    fun onCurrentPasswordChange(value: String) {
        _uiState.update {
            it.copy(currentPassword = value, currentPasswordError = null, generalError = null)
        }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update {
            it.copy(newPassword = value, newPasswordError = null, confirmPasswordError = null, generalError = null)
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update {
            it.copy(confirmPassword = value, confirmPasswordError = null, generalError = null)
        }
    }

    fun onOpenChangePassword() {
        _uiState.update {
            it.copy(
                showPasswordSavedMessage = false,
                currentPassword = "",
                newPassword = "",
                confirmPassword = "",
                currentPasswordError = null,
                newPasswordError = null,
                confirmPasswordError = null,
                generalError = null
            )
        }
    }

    fun onSavePasswordClick() {
        val state = _uiState.value
        if (state.currentPassword.isBlank()) {
            showPasswordError("Ingresa tu contraseña actual.")
            return
        }
        if (state.newPassword.length < 6) {
            showPasswordError("Tu contraseña debe tener mas de 6 caracteres.")
            return
        }
        if (state.newPassword != state.confirmPassword) {
            showPasswordError("Las contraseñas ingresadas no coinciden entre si.")
            return
        }

        when (val result = repository.changeCurrentPassword(state.currentPassword, state.newPassword)) {
            is ProfileResult.Success -> {
                refreshUser()
                _uiState.update {
                    it.copy(
                        currentPassword = "",
                        newPassword = "",
                        confirmPassword = "",
                        currentPasswordError = null,
                        newPasswordError = null,
                        confirmPasswordError = null,
                        generalError = null,
                        showPasswordSavedMessage = true,
                        navigateToProfileAfterPasswordSave = true
                    )
                }
            }

            is ProfileResult.Error -> showPasswordError(result.message)
        }
    }

    fun onPasswordNavigationConsumed() {
        _uiState.update { it.copy(navigateToProfileAfterPasswordSave = false) }
    }

    fun onLogoutConfirmed() {
        repository.logout()
        _uiState.update { it.copy(sessionEnded = true) }
    }

    fun onDeleteAccountConfirmed() {
        when (val result = repository.deleteCurrentUser()) {
            is ProfileResult.Success -> _uiState.update { it.copy(sessionEnded = true) }
            is ProfileResult.Error -> _uiState.update { it.copy(generalError = result.message) }
        }
    }

    fun onSessionEndedConsumed() {
        _uiState.update { it.copy(sessionEnded = false) }
    }

    private fun showPasswordError(message: String) {
        _uiState.update {
            when {
                message.contains("coinciden", ignoreCase = true) ->
                    it.copy(confirmPasswordError = message, generalError = null)

                message.contains("actual", ignoreCase = true) ||
                    message.contains("ingresada", ignoreCase = true) ->
                    it.copy(currentPasswordError = message, generalError = null)

                message.contains("nueva", ignoreCase = true) ||
                    message.contains("6 caracteres", ignoreCase = true) ->
                    it.copy(newPasswordError = message, generalError = null)

                else -> it.copy(generalError = message)
            }
        }
    }
}
