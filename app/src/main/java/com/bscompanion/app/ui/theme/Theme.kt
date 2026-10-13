package com.bscompanion.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Nexora: chłodne mroźne szkło — jasny błękit + granatowy atrament.
private val LightScheme = lightColorScheme(
    primary = Color(0xFF12325E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD9E6F6),
    onPrimaryContainer = Color(0xFF0E2444),
    secondary = Color(0xFF5A6B87),
    secondaryContainer = Color(0xFFDCE8F5),
    onSecondaryContainer = Color(0xFF0E2444),
    tertiary = Color(0xFF3E6FA3),
    tertiaryContainer = Color(0xFFCDE0F3),
    onTertiaryContainer = Color(0xFF0E2444),
    surface = Color(0xFFD7E5F4),
    onSurface = Color(0xFF0E1E3A),
    surfaceVariant = Color(0xFFE3ECF7),
    onSurfaceVariant = Color(0xFF54678A),
    background = Color(0xFFCBDDF1),
    onBackground = Color(0xFF0E1E3A),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF4F8FD),
    surfaceContainer = Color(0xFFEAF1FA),
    surfaceContainerHigh = Color(0xFFDFE9F5),
    surfaceContainerHighest = Color(0xFFD2DFEF),
    outline = Color(0xFF9FB4CE),
    outlineVariant = Color(0xFFD3E0F0),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF12325E),
    inverseSurface = Color(0xFF12233D),
    inverseOnSurface = Color(0xFFEAF1FA),
    inversePrimary = Color(0xFF9CC3EE),
    surfaceBright = Color(0xFFEAF1FA),
    surfaceDim = Color(0xFFB9CFE6)
)

// Nocny odpowiednik tego samego języka: głęboki granat + jasne szkło.
private val DarkScheme = darkColorScheme(
    primary = Color(0xFF9CC3EE),
    onPrimary = Color(0xFF0A1B33),
    primaryContainer = Color(0xFF1E3A63),
    onPrimaryContainer = Color(0xFFDCE9F8),
    secondary = Color(0xFF93A5C0),
    secondaryContainer = Color(0xFF1A2C49),
    onSecondaryContainer = Color(0xFFEAF1FA),
    tertiary = Color(0xFF7FA8D4),
    tertiaryContainer = Color(0xFF16304F),
    onTertiaryContainer = Color(0xFFDCE9F8),
    background = Color(0xFF0A1728),
    onBackground = Color(0xFFEAF1FA),
    surface = Color(0xFF0A1728),
    onSurface = Color(0xFFEAF1FA),
    surfaceVariant = Color(0xFF12233D),
    onSurfaceVariant = Color(0xFF93A5C0),
    surfaceContainerLowest = Color(0xFF060D18),
    surfaceContainerLow = Color(0xFF0A1728),
    surfaceContainer = Color(0xFF12233D),
    surfaceContainerHigh = Color(0xFF18294A),
    surfaceContainerHighest = Color(0xFF20345A),
    outline = Color(0xFF3A4F70),
    outlineVariant = Color(0xFF22344F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF9CC3EE),
    inverseSurface = Color(0xFFEAF1FA),
    inverseOnSurface = Color(0xFF0E1E3A),
    inversePrimary = Color(0xFF12325E),
    surfaceBright = Color(0xFF20345A),
    surfaceDim = Color(0xFF060F1E)
)

@Composable
fun BSCompanionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    MaterialTheme(colorScheme = scheme, content = content)
}
