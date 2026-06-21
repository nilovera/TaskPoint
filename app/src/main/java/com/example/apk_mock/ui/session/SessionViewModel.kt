package com.example.apk_mock.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.apk_mock.data.sync.RemoteSyncReconciler
import com.example.apk_mock.data.sync.SyncScheduler
import com.example.apk_mock.domain.repository.AuthRepository
import com.example.apk_mock.domain.repository.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SessionUiState {
    data object Checking : SessionUiState
    data object Unauthenticated : SessionUiState
    data class Authenticated(val user: User) : SessionUiState
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val remoteSyncReconciler: RemoteSyncReconciler,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow<SessionUiState>(SessionUiState.Checking)
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        refreshSession()
    }

    fun refreshSession() {
        viewModelScope.launch {
            _uiState.value = SessionUiState.Checking
            val state = runCatching { authRepository.currentUser() }
                .getOrNull()
                ?.let(SessionUiState::Authenticated)
                ?: SessionUiState.Unauthenticated
            _uiState.value = state
            if (state is SessionUiState.Authenticated) {
                runCatching { remoteSyncReconciler.reconcileRemoteChanges() }
                syncScheduler.schedulePendingSync()
            }
        }
    }

    fun onAuthenticated(user: User) {
        _uiState.value = SessionUiState.Authenticated(user)
        viewModelScope.launch {
            runCatching { remoteSyncReconciler.reconcileRemoteChanges() }
            syncScheduler.schedulePendingSync()
        }
    }

    fun onSessionEnded() {
        _uiState.value = SessionUiState.Unauthenticated
    }
}
