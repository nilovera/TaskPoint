package com.example.apk_mock.ui.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apk_mock.data.preferences.AppPreferencesRepository
import com.example.apk_mock.domain.model.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppPreferencesUiState(
    val isLoading: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val themePreference: ThemePreference = ThemePreference.SYSTEM
)

@HiltViewModel
class AppPreferencesViewModel @Inject constructor(
    private val repository: AppPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<AppPreferencesUiState> = repository.preferences
        .map { preferences ->
            AppPreferencesUiState(
                isLoading = false,
                onboardingCompleted = preferences.onboardingCompleted,
                themePreference = preferences.themePreference
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppPreferencesUiState()
        )

    fun completeOnboarding() {
        viewModelScope.launch { repository.completeOnboarding() }
    }

    fun setThemePreference(themePreference: ThemePreference) {
        viewModelScope.launch { repository.setThemePreference(themePreference) }
    }
}
