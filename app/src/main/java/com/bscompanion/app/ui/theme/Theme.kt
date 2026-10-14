package com.bscompanion.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.bscompanion.app.R

// Space Grotesk w całej apce (OFL, w res/font); Montserrat tylko do zegara.
private val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_regular, FontWeight.Normal),
    Font(R.font.space_grotesk_medium, FontWeight.Medium),
    Font(R.font.space_grotesk_bold, FontWeight.Bold)
)

val Montserrat = FontFamily(Font(R.font.montserrat_bold, FontWeight.Bold))

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

/** Trzy motywy do wyboru w Ustawieniach. Zapisywane w prefs, aktywne od razu. */
enum class AppTheme(val label: String, val darkBars: Boolean, val windowBg: Long) {
    MONO("Mono", true, 0xFF000000),
    GRAFIT("Grafit", true, 0xFF131316),
    MLEKO("Mleko", false, 0xFFF1F1EC)
}

fun appThemeOf(name: String?): AppTheme = try {
    AppTheme.valueOf(name ?: "MONO")
} catch (_: Exception) {
    AppTheme.MONO
}

// Mono: czysta czerń + szarości + biel. Zero błękitu.
private val MonoScheme = darkColorScheme(
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

// Grafit: miękka ciemna szarość zamiast absolutnej czerni.
private val GrafitScheme = darkColorScheme(
    primary = Color(0xFFE8E8EA),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF2E2E34),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFA7A7B3),
    secondaryContainer = Color(0xFF26262C),
    onSecondaryContainer = Color(0xFFECECF1),
    tertiary = Color(0xFFBFC0C9),
    tertiaryContainer = Color(0xFF232329),
    onTertiaryContainer = Color(0xFFFFFFFF),
    background = Color(0xFF131316),
    onBackground = Color(0xFFECECF1),
    surface = Color(0xFF131316),
    onSurface = Color(0xFFECECF1),
    surfaceVariant = Color(0xFF1B1B1F),
    onSurfaceVariant = Color(0xFFA7A7B3),
    surfaceContainerLowest = Color(0xFF0C0C0E),
    surfaceContainerLow = Color(0xFF131316),
    surfaceContainer = Color(0xFF1B1B1F),
    surfaceContainerHigh = Color(0xFF232329),
    surfaceContainerHighest = Color(0xFF2C2C33),
    outline = Color(0xFF3A3A42),
    outlineVariant = Color(0xFF26262C),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFFE8E8EA),
    inverseSurface = Color(0xFFE8E8EA),
    inverseOnSurface = Color(0xFF131316),
    inversePrimary = Color(0xFF3A3A42),
    surfaceBright = Color(0xFF2C2C33),
    surfaceDim = Color(0xFF0C0C0E)
)

// Mleko: jasne odwrócenie mono — czarna pigułka na kości słoniowej.
private val MlekoScheme = lightColorScheme(
    primary = Color(0xFF141412),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE4E4DA),
    onPrimaryContainer = Color(0xFF141412),
    secondary = Color(0xFF6B6B66),
    secondaryContainer = Color(0xFFE9E9E1),
    onSecondaryContainer = Color(0xFF141412),
    tertiary = Color(0xFF4C4C48),
    tertiaryContainer = Color(0xFFDEDED6),
    onTertiaryContainer = Color(0xFF141412),
    background = Color(0xFFF1F1EC),
    onBackground = Color(0xFF141412),
    surface = Color(0xFFF1F1EC),
    onSurface = Color(0xFF141412),
    surfaceVariant = Color(0xFFE7E7DF),
    onSurfaceVariant = Color(0xFF6B6B66),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F7F2),
    surfaceContainer = Color(0xFFEFEFE8),
    surfaceContainerHigh = Color(0xFFE4E4DA),
    surfaceContainerHighest = Color(0xFFD8D8CE),
    outline = Color(0xFFB9B9AE),
    outlineVariant = Color(0xFFD8D8CE),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF141412),
    inverseSurface = Color(0xFF1A1A18),
    inverseOnSurface = Color(0xFFF1F1EC),
    inversePrimary = Color(0xFFE4E4DA),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFDCDCD2)
)

@Composable
fun BSCompanionTheme(
    theme: AppTheme = AppTheme.MONO,
    content: @Composable () -> Unit
) {
    val scheme = when (theme) {
        AppTheme.MONO -> MonoScheme
        AppTheme.GRAFIT -> GrafitScheme
        AppTheme.MLEKO -> MlekoScheme
    }
    MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
}
