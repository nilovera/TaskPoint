package com.example.apk_mock.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.apk_mock.domain.model.ThemePreference

@Immutable
data class TaskPointColors(
    val background: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val routineCard: Color,
    val taskCard: Color,
    val subTaskCard: Color,
    val border: Color,
    val fieldBackground: Color,
    val fieldBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val label: Color,
    val placeholder: Color,
    val primary: Color,
    val success: Color,
    val destructive: Color,
    val warningBackground: Color,
    val warningText: Color,
    val errorBackground: Color,
    val avatarContainer: Color,
    val avatarIcon: Color,
    val bottomNavBackground: Color,
    val bottomNavInactive: Color
)

private val DarkTaskPointColors = TaskPointColors(
    background = Color(0xFF0D0D17),
    surface = Color(0xFF1E2130),
    surfaceAlt = Color(0xFF171A2B),
    routineCard = Color(0xFF171A2B),
    taskCard = Color(0xFF161929),
    subTaskCard = Color(0xFF111420),
    border = Color(0xFF252B44),
    fieldBackground = Color(0xFF1E2130),
    fieldBorder = Color(0xFF2A2D3E),
    textPrimary = Color.White,
    textSecondary = Color(0xFF9EA3B0),
    label = Color(0xFFB0B5C9),
    placeholder = Color(0xFF4A4F6A),
    primary = Color(0xFF3D6FE8),
    success = Color(0xFF3DAA7C),
    destructive = Color(0xFFE03847),
    warningBackground = Color(0xFF381217),
    warningText = Color(0xFFF26B75),
    errorBackground = Color(0xFF3A1A1A),
    avatarContainer = Color(0xFFEADDFF),
    avatarIcon = Color(0xFF1C2550),
    bottomNavBackground = Color(0xFF161929),
    bottomNavInactive = Color(0xFF9EA3B0)
)

private val LightTaskPointColors = TaskPointColors(
    background = Color(0xFFF7F7F7),
    surface = Color.White,
    surfaceAlt = Color.White,
    routineCard = Color.White,
    taskCard = Color.White,
    subTaskCard = Color.White,
    border = Color(0xFFD1D6EB),
    fieldBackground = Color.White,
    fieldBorder = Color(0xFFD1D6EB),
    textPrimary = Color.Black,
    textSecondary = Color(0x80000000),
    label = Color.Black,
    placeholder = Color(0x80000000),
    primary = Color(0xFF3D6FE8),
    success = Color(0xFF3DAA7C),
    destructive = Color(0xFFC93050),
    warningBackground = Color(0x80E05B6A),
    warningText = Color(0xFFC93050),
    errorBackground = Color(0x80E05B6A),
    avatarContainer = Color(0xFFEADDFF),
    avatarIcon = Color(0xFF1C2550),
    bottomNavBackground = Color.White,
    bottomNavInactive = Color(0xFF6B7280)
)

val LocalTaskPointColors = staticCompositionLocalOf { DarkTaskPointColors }

object TaskPointTheme {
    val colors: TaskPointColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTaskPointColors.current
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkTaskPointColors.primary,
    background = DarkTaskPointColors.background,
    surface = DarkTaskPointColors.surface,
    onPrimary = Color.White,
    onBackground = DarkTaskPointColors.textPrimary,
    onSurface = DarkTaskPointColors.textPrimary,
    error = DarkTaskPointColors.destructive,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightTaskPointColors.primary,
    background = LightTaskPointColors.background,
    surface = LightTaskPointColors.surface,
    surfaceVariant = LightTaskPointColors.surfaceAlt,
    outline = LightTaskPointColors.border,
    onPrimary = Color.White,
    onBackground = LightTaskPointColors.textPrimary,
    onSurface = LightTaskPointColors.textPrimary,
    onSurfaceVariant = LightTaskPointColors.textSecondary,
    error = LightTaskPointColors.destructive,
    onError = Color.White
)

@Composable
fun APKMockTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }
    val fallbackColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val fallbackTaskPointColors = if (darkTheme) DarkTaskPointColors else LightTaskPointColors
    val useDynamicColor =
        themePreference == ThemePreference.SYSTEM && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current
    val colorScheme = when {
        useDynamicColor && darkTheme -> dynamicDarkColorScheme(context)
        useDynamicColor -> dynamicLightColorScheme(context)
        else -> fallbackColorScheme
    }
    val taskPointColors = if (useDynamicColor) {
        colorScheme.toTaskPointColors(fallbackTaskPointColors)
    } else {
        fallbackTaskPointColors
    }

    CompositionLocalProvider(LocalTaskPointColors provides taskPointColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

private fun ColorScheme.toTaskPointColors(fallback: TaskPointColors): TaskPointColors = fallback.copy(
    background = background,
    surface = surface,
    surfaceAlt = surfaceVariant,
    routineCard = surface,
    taskCard = surface,
    subTaskCard = surfaceVariant,
    border = outlineVariant,
    fieldBackground = surface,
    fieldBorder = outline,
    textPrimary = onBackground,
    textSecondary = onSurfaceVariant,
    label = onSurface,
    placeholder = onSurfaceVariant.copy(alpha = 0.72f),
    primary = primary,
    destructive = error,
    warningBackground = errorContainer,
    warningText = onErrorContainer,
    errorBackground = errorContainer,
    avatarContainer = primaryContainer,
    avatarIcon = onPrimaryContainer,
    bottomNavBackground = surface,
    bottomNavInactive = onSurfaceVariant
)
