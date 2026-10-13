package com.bscompanion.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = Color(0xFF4658A9),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDEE0FF),
    onPrimaryContainer = Color(0xFF00125C),
    secondary = Color(0xFF5B5D72),
    secondaryContainer = Color(0xFFE1E0F9),
    tertiary = Color(0xFF77536D),
    tertiaryContainer = Color(0xFFFFD7F2),
    surface = Color(0xFFFBF8FF),
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFFE3E1EC),
    onSurfaceVariant = Color(0xFF49454F),
    background = Color(0xFFFBF8FF),
    onBackground = Color(0xFF1D1B20),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F2FA),
    surfaceContainer = Color(0xFFF3EDF7),
    surfaceContainerHigh = Color(0xFFECE6F0),
    surfaceContainerHighest = Color(0xFFE6E0E9),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF4658A9),
    inverseSurface = Color(0xFF2F3033),
    inverseOnSurface = Color(0xFFF2F0F4),
    inversePrimary = Color(0xFFBDC2FF),
    surfaceBright = Color(0xFFFBF8FF),
    surfaceDim = Color(0xFFE0D9E3)
)

// Premium "ink" dark: prawdziwa czerń + węglowe karty + mocna typografia.
private val InkBg = Color(0xFF0A0B0E)
private val InkCard = Color(0xFF15171C)
private val InkHigh = Color(0xFF1C1F26)
private val InkText = Color(0xFFF4F5F7)
private val InkSub = Color(0xFFA2A7B4)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFB9C6FF),
    onPrimary = Color(0xFF0A1030),
    primaryContainer = Color(0xFF2B3763),
    onPrimaryContainer = Color(0xFFDEE3FF),
    secondary = InkSub,
    secondaryContainer = Color(0xFF2F3749),
    onSecondaryContainer = InkText,
    tertiary = Color(0xFFE3BFD6),
    tertiaryContainer = Color(0xFF4A3040),
    onTertiaryContainer = Color(0xFFFFD9EC),
    background = InkBg,
    onBackground = InkText,
    surface = InkBg,
    onSurface = InkText,
    surfaceVariant = InkCard,
    onSurfaceVariant = InkSub,
    surfaceContainerLowest = InkBg,
    surfaceContainerLow = Color(0xFF0E1013),
    surfaceContainer = InkCard,
    surfaceContainerHigh = Color(0xFF20242E),
    surfaceContainerHighest = Color(0xFF2A2F3B),
    outline = Color(0xFF4A4E57),
    outlineVariant = Color(0xFF2E323B),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFFB9C6FF),
    inverseSurface = Color(0xFFE6E0E9),
    inverseOnSurface = Color(0xFF1D1B20),
    inversePrimary = Color(0xFF4658A9),
    surfaceBright = Color(0xFF2A2F3B),
    surfaceDim = Color(0xFF0A0B0E)
)

@Composable
fun BSCompanionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    MaterialTheme(colorScheme = scheme, content = content)
}
