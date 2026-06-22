package com.example.apk_mock.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.apk_mock.domain.model.ThemePreference
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.appPreferencesDataStore by preferencesDataStore(name = "app_preferences")

data class AppPreferences(
    val onboardingCompleted: Boolean = false,
    val themePreference: ThemePreference = ThemePreference.SYSTEM
)

@Singleton
class AppPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    val preferences: Flow<AppPreferences> = context.appPreferencesDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map(::toAppPreferences)

    suspend fun completeOnboarding() {
        context.appPreferencesDataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun setThemePreference(themePreference: ThemePreference) {
        context.appPreferencesDataStore.edit { preferences ->
            preferences[THEME_PREFERENCE] = themePreference.name
        }
    }

    private fun toAppPreferences(preferences: Preferences): AppPreferences {
        val storedTheme = preferences[THEME_PREFERENCE]
            ?.let { value -> ThemePreference.entries.firstOrNull { it.name == value } }
            ?: ThemePreference.SYSTEM

        return AppPreferences(
            onboardingCompleted = preferences[ONBOARDING_COMPLETED] ?: false,
            themePreference = storedTheme
        )
    }

    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
    }
}
