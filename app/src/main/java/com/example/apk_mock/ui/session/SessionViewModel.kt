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
    data class Authenticated(
        val user: User,
        val isInitialDataSyncInProgress: Boolean = false
    ) : SessionUiState
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
            val user = runCatching { authRepository.currentUser() }.getOrNull()
            if (user == null) {
                _uiState.value = SessionUiState.Unauthenticated
                return@launch
            }
            synchronizeInitialData(user)
        }
    }

    fun onAuthenticated(user: User) {
        // Se publica antes de navegar a Home para que nunca se interprete una
        // base local aun vacia como el estado definitivo del usuario.
        _uiState.value = SessionUiState.Authenticated(
            user = user,
            isInitialDataSyncInProgress = true
        )
        viewModelScope.launch {
            synchronizeInitialData(user)
        }
    }

    fun onSessionEnded() {
        _uiState.value = SessionUiState.Unauthenticated
    }

    private suspend fun synchronizeInitialData(user: User) {
        _uiState.value = SessionUiState.Authenticated(
            user = user,
            isInitialDataSyncInProgress = true
        )
        try {
            runCatching { remoteSyncReconciler.reconcileRemoteChanges() }
            runCatching { syncScheduler.schedulePendingSync() }
        } finally {
            val currentState = _uiState.value as? SessionUiState.Authenticated
            if (currentState?.user?.id == user.id) {
                _uiState.value = SessionUiState.Authenticated(user = user)
            }
        }
    }
}
