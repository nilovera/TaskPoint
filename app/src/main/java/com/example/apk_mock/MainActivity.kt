package com.example.apk_mock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.apk_mock.ui.navigation.AppNavigation
import com.example.apk_mock.ui.preferences.AppPreferencesViewModel
import com.example.apk_mock.ui.theme.APKMockTheme
import androidx.compose.material3.Surface
import com.example.apk_mock.ui.theme.TaskPointTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferencesViewModel: AppPreferencesViewModel = hiltViewModel()
            val preferencesState by preferencesViewModel.uiState.collectAsStateWithLifecycle()

            APKMockTheme(themePreference = preferencesState.themePreference) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (preferencesState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(TaskPointTheme.colors.background),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = TaskPointTheme.colors.primary)
                        }
                    } else {
                        AppNavigation(
                            onboardingCompleted = preferencesState.onboardingCompleted,
                            onOnboardingCompleted = preferencesViewModel::completeOnboarding,
                            themePreference = preferencesState.themePreference,
                            onThemePreferenceChange = preferencesViewModel::setThemePreference
                        )
                    }
                }
            }
        }
    }
}
