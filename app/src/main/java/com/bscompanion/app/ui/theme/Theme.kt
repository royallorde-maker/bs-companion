package com.bscompanion.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.bscompanion.app.R

// Space Grotesk w całej apce (OFL, w res/font); Clocker tylko do zegara.
private val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_regular, FontWeight.Normal),
    Font(R.font.space_grotesk_medium, FontWeight.Medium),
    Font(R.font.space_grotesk_bold, FontWeight.Bold)
)

val Clocker = FontFamily(Font(R.font.clocker, FontWeight.Normal))

private val AppTypography = Typography().let { t ->
    Typography(
        displayLarge = t.displayLarge.copy(fontFamily = SpaceGrotesk),
        displayMedium = t.displayMedium.copy(fontFamily = SpaceGrotesk),
        displaySmall = t.displaySmall.copy(fontFamily = SpaceGrotesk),
        headlineLarge = t.headlineLarge.copy(fontFamily = SpaceGrotesk),
        headlineMedium = t.headlineMedium.copy(fontFamily = SpaceGrotesk),
        headlineSmall = t.headlineSmall.copy(fontFamily = SpaceGrotesk),
        titleLarge = t.titleLarge.copy(fontFamily = SpaceGrotesk),
        titleMedium = t.titleMedium.copy(fontFamily = SpaceGrotesk),
        titleSmall = t.titleSmall.copy(fontFamily = SpaceGrotesk),
        bodyLarge = t.bodyLarge.copy(fontFamily = SpaceGrotesk),
        bodyMedium = t.bodyMedium.copy(fontFamily = SpaceGrotesk),
        bodySmall = t.bodySmall.copy(fontFamily = SpaceGrotesk),
        labelLarge = t.labelLarge.copy(fontFamily = SpaceGrotesk),
        labelMedium = t.labelMedium.copy(fontFamily = SpaceGrotesk),
        labelSmall = t.labelSmall.copy(fontFamily = SpaceGrotesk)
    )
}

// AMOLED mono: czysta czerń + szarości + biel. Zero błękitu. Jedyny motyw.
private val AmoledScheme = darkColorScheme(
    primary = Color(0xFFF2F2F2),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF232323),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF9A9A9A),
    secondaryContainer = Color(0xFF1C1C1C),
    onSecondaryContainer = Color(0xFFF2F2F2),
    tertiary = Color(0xFFB5B5B5),
    tertiaryContainer = Color(0xFF1A1A1A),
    onTertiaryContainer = Color(0xFFFFFFFF),
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
    surfaceTint = Color(0xFFF2F2F2),
    inverseSurface = Color(0xFFE8E8E8),
    inverseOnSurface = Color(0xFF000000),
    inversePrimary = Color(0xFF2A2A2A),
    surfaceBright = Color(0xFF1E1E1E),
    surfaceDim = Color(0xFF000000)
)

@Composable
fun BSCompanionTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AmoledScheme, typography = AppTypography, content = content)
}
