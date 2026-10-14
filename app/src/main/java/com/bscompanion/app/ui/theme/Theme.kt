package com.bscompanion.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// AMOLED: prawdziwa czerń + grafitowe tafle + spokojny błękit. Jedyny motyw.
private val AmoledScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF001233),
    primaryContainer = Color(0xFF16283F),
    onPrimaryContainer = Color(0xFFC9DFFA),
    secondary = Color(0xFF9AA0A6),
    secondaryContainer = Color(0xFF1C1C1E),
    onSecondaryContainer = Color(0xFFF2F2F2),
    tertiary = Color(0xFF7FA8D4),
    tertiaryContainer = Color(0xFF12233A),
    onTertiaryContainer = Color(0xFFC9DFFA),
    background = Color(0xFF000000),
    onBackground = Color(0xFFF2F2F2),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFF2F2F2),
    surfaceVariant = Color(0xFF0D0D0D),
    onSurfaceVariant = Color(0xFF9A9A9A),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF050505),
    surfaceContainer = Color(0xFF101010),
    surfaceContainerHigh = Color(0xFF161616),
    surfaceContainerHighest = Color(0xFF1E1E1E),
    outline = Color(0xFF2A2A2A),
    outlineVariant = Color(0xFF1A1A1A),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF8AB4F8),
    inverseSurface = Color(0xFFE8EAED),
    inverseOnSurface = Color(0xFF000000),
    inversePrimary = Color(0xFF12325E),
    surfaceBright = Color(0xFF1E1E1E),
    surfaceDim = Color(0xFF000000)
)

@Composable
fun BSCompanionTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AmoledScheme, content = content)
}
