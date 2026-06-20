package com.example.apk_mock.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apk_mock.domain.repository.AuthRepository
import com.example.apk_mock.domain.repository.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SessionUiState {
    data object Checking : SessionUiState
    data object Unauthenticated : SessionUiState
    data class Authenticated(val user: User) : SessionUiState
}

class SessionViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SessionUiState>(SessionUiState.Checking)
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        refreshSession()
    }

    fun refreshSession() {
        viewModelScope.launch {
            _uiState.value = SessionUiState.Checking
            _uiState.value = runCatching { authRepository.currentUser() }
                .getOrNull()
                ?.let(SessionUiState::Authenticated)
                ?: SessionUiState.Unauthenticated
        }
    }

    fun onAuthenticated(user: User) {
        _uiState.value = SessionUiState.Authenticated(user)
    }

    fun onSessionEnded() {
        _uiState.value = SessionUiState.Unauthenticated
    }
}
