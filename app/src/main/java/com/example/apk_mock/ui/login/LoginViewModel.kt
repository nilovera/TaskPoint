package com.example.apk_mock.ui.login

import androidx.lifecycle.ViewModel
import com.example.apk_mock.domain.AuthRepository
import com.example.apk_mock.domain.AuthResult
import com.example.apk_mock.domain.useCase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val loggedInName: String = ""
)

class LoginViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, errorMessage = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, errorMessage = null) }

    fun onLoginClick() {
        when (val r = loginUseCase(_uiState.value.email, _uiState.value.password)) {
            is AuthResult.Success -> _uiState.update { it.copy(isSuccess = true, loggedInName = r.user.name) }
            is AuthResult.Error   -> _uiState.update { it.copy(errorMessage = r.message) }
        }
    }

    fun onSuccessConsumed() = _uiState.update { it.copy(isSuccess = false) }
}