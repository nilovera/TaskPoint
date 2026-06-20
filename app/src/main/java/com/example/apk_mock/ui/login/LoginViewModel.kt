package com.example.apk_mock.ui.login

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

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val loggedInUser: User? = null
)

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, errorMessage = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, errorMessage = null) }

    fun onLoginClick() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Completá todos los campos.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val r = repository.login(state.email.trim(), state.password)) {
                is AuthResult.Success -> _uiState.update {
                    it.copy(isLoading = false, isSuccess = true, loggedInUser = r.user)
                }
                is AuthResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = r.message) }
            }
        }
    }

    fun onSuccessConsumed() = _uiState.update { it.copy(isSuccess = false) }
}
