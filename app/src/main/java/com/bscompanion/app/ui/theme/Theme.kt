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

/** Dwanaście motywów do wyboru w Ustawieniach. Zapisywane w prefs, aktywne od razu. */
enum class AppTheme(val label: String, val darkBars: Boolean, val windowBg: Long) {
    MONO("Mono", true, 0xFF000000),
    GRAFIT("Grafit", true, 0xFF131316),
    MLEKO("Mleko", false, 0xFFF1F1EC),
    CANDY("Cukierek", true, 0xFF1A0E14),
    BURSZTYN("Bursztyn", true, 0xFF171208),
    LAS("Las", true, 0xFF0C1510),
    LAWENDA("Lawenda", true, 0xFF14101C),
    OCEAN("Ocean", true, 0xFF08131F),
    TURKUS("Turkus", true, 0xFF06171A),
    ZACHOD("Zachód", true, 0xFF1B0F0C),
    WINO("Wino", true, 0xFF170C0F),
    LOD("Lód", false, 0xFFEFF4F7)
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

// Cukierek: ciemne cukierkowe tło + różowy akcent. Ciemne bary jak w Mono.
private val CandyScheme = darkColorScheme(
    primary = Color(0xFFFFB1C8),
    onPrimary = Color(0xFF4A0E20),
    primaryContainer = Color(0xFF5C1A30),
    onPrimaryContainer = Color(0xFFFFD9E3),
    secondary = Color(0xFFE08AA5),
    secondaryContainer = Color(0xFF3A1421),
    onSecondaryContainer = Color(0xFFFFD9E3),
    tertiary = Color(0xFFDD9DF2),
    tertiaryContainer = Color(0xFF38163F),
    onTertiaryContainer = Color(0xFFF5D9FF),
    background = Color(0xFF1A0E14),
    onBackground = Color(0xFFFFE9EF),
    surface = Color(0xFF1A0E14),
    onSurface = Color(0xFFFFE9EF),
    surfaceVariant = Color(0xFF241219),
    onSurfaceVariant = Color(0xFFE0A9BB),
    surfaceContainerLowest = Color(0xFF12070C),
    surfaceContainerLow = Color(0xFF1A0E14),
    surfaceContainer = Color(0xFF241219),
    surfaceContainerHigh = Color(0xFF2F1620),
    surfaceContainerHighest = Color(0xFF3D1C2A),
    outline = Color(0xFF5C2E40),
    outlineVariant = Color(0xFF3A1B28),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFFFFB1C8),
    inverseSurface = Color(0xFFFFE9EF),
    inverseOnSurface = Color(0xFF1A0E14),
    inversePrimary = Color(0xFF7A2A45),
    surfaceBright = Color(0xFF3D1C2A),
    surfaceDim = Color(0xFF12070C)
)

// Bursztyn: miodowy akcent na prawie czarnej kawie. Ciemne bary.
private val BursztynScheme = darkColorScheme(
    primary = Color(0xFFFFC15C),
    onPrimary = Color(0xFF3A2200),
    primaryContainer = Color(0xFF4A2E0A),
    onPrimaryContainer = Color(0xFFFFE3B3),
    secondary = Color(0xFFE0A44A),
    secondaryContainer = Color(0xFF2E1F0C),
    onSecondaryContainer = Color(0xFFFFE3B3),
    tertiary = Color(0xFFD8B36A),
    tertiaryContainer = Color(0xFF33230F),
    onTertiaryContainer = Color(0xFFFFEFD4),
    background = Color(0xFF171208),
    onBackground = Color(0xFFFFEFD4),
    surface = Color(0xFF171208),
    onSurface = Color(0xFFFFEFD4),
    surfaceVariant = Color(0xFF201812),
    onSurfaceVariant = Color(0xFFC9AE7E),
    surfaceContainerLowest = Color(0xFF0F0B05),
    surfaceContainerLow = Color(0xFF171208),
    surfaceContainer = Color(0xFF201812),
    surfaceContainerHigh = Color(0xFF2A2013),
    surfaceContainerHighest = Color(0xFF362917),
    outline = Color(0xFF5C4A2E),
    outlineVariant = Color(0xFF33270F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFFFFC15C),
    inverseSurface = Color(0xFFFFEFD4),
    inverseOnSurface = Color(0xFF171208),
    inversePrimary = Color(0xFF7A5210),
    surfaceBright = Color(0xFF362917),
    surfaceDim = Color(0xFF0F0B05)
)

// Las: leśna zieleń na ciemnym mchu. Ciemne bary.
private val LasScheme = darkColorScheme(
    primary = Color(0xFF9BE2A8),
    onPrimary = Color(0xFF0B3D1F),
    primaryContainer = Color(0xFF1D4A2A),
    onPrimaryContainer = Color(0xFFD2F2D6),
    secondary = Color(0xFF8CC49A),
    secondaryContainer = Color(0xFF122019),
    onSecondaryContainer = Color(0xFFD2F2D6),
    tertiary = Color(0xFFA9C7AE),
    tertiaryContainer = Color(0xFF1A2B20),
    onTertiaryContainer = Color(0xFFE4F2E6),
    background = Color(0xFF0C1510),
    onBackground = Color(0xFFE4F2E6),
    surface = Color(0xFF0C1510),
    onSurface = Color(0xFFE4F2E6),
    surfaceVariant = Color(0xFF122019),
    onSurfaceVariant = Color(0xFFA9C7AE),
    surfaceContainerLowest = Color(0xFF070D09),
    surfaceContainerLow = Color(0xFF0C1510),
    surfaceContainer = Color(0xFF122019),
    surfaceContainerHigh = Color(0xFF182720),
    surfaceContainerHighest = Color(0xFF1F3129),
    outline = Color(0xFF33473A),
    outlineVariant = Color(0xFF1B2A20),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF9BE2A8),
    inverseSurface = Color(0xFFE4F2E6),
    inverseOnSurface = Color(0xFF0C1510),
    inversePrimary = Color(0xFF2E6B3C),
    surfaceBright = Color(0xFF1F3129),
    surfaceDim = Color(0xFF070D09)
)

// Lawenda: fiolet na ciemnej śliwce. Ciemne bary.
private val LawendaScheme = darkColorScheme(
    primary = Color(0xFFD3B8FF),
    onPrimary = Color(0xFF35145E),
    primaryContainer = Color(0xFF472A76),
    onPrimaryContainer = Color(0xFFEBDDFF),
    secondary = Color(0xFFBFA3EC),
    secondaryContainer = Color(0xFF221A33),
    onSecondaryContainer = Color(0xFFEBDDFF),
    tertiary = Color(0xFFE0A6D5),
    tertiaryContainer = Color(0xFF3A1E38),
    onTertiaryContainer = Color(0xFFF5D9FF),
    background = Color(0xFF14101C),
    onBackground = Color(0xFFF0E9FB),
    surface = Color(0xFF14101C),
    onSurface = Color(0xFFF0E9FB),
    surfaceVariant = Color(0xFF1B1626),
    onSurfaceVariant = Color(0xFFC2B3D9),
    surfaceContainerLowest = Color(0xFF0D0A13),
    surfaceContainerLow = Color(0xFF14101C),
    surfaceContainer = Color(0xFF1B1626),
    surfaceContainerHigh = Color(0xFF241D33),
    surfaceContainerHighest = Color(0xFF2F2642),
    outline = Color(0xFF4A3D63),
    outlineVariant = Color(0xFF2A2138),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFFD3B8FF),
    inverseSurface = Color(0xFFF0E9FB),
    inverseOnSurface = Color(0xFF14101C),
    inversePrimary = Color(0xFF5F3FA3),
    surfaceBright = Color(0xFF2F2642),
    surfaceDim = Color(0xFF0D0A13)
)

// Ocean: głęboki granat z lodowym błękitem. Ciemne bary.
private val OceanScheme = darkColorScheme(
    primary = Color(0xFF9CD8FF),
    onPrimary = Color(0xFF002B45),
    primaryContainer = Color(0xFF0B3B5C),
    onPrimaryContainer = Color(0xFFD6EEFF),
    secondary = Color(0xFF7FB8D9),
    secondaryContainer = Color(0xFF0C2434),
    onSecondaryContainer = Color(0xFFD6EEFF),
    tertiary = Color(0xFFA8C8E8),
    tertiaryContainer = Color(0xFF12293A),
    onTertiaryContainer = Color(0xFFE1F0FF),
    background = Color(0xFF08131F),
    onBackground = Color(0xFFE1F0FF),
    surface = Color(0xFF08131F),
    onSurface = Color(0xFFE1F0FF),
    surfaceVariant = Color(0xFF0E1D2B),
    onSurfaceVariant = Color(0xFFA8C4D8),
    surfaceContainerLowest = Color(0xFF050D15),
    surfaceContainerLow = Color(0xFF08131F),
    surfaceContainer = Color(0xFF0E1D2B),
    surfaceContainerHigh = Color(0xFF142838),
    surfaceContainerHighest = Color(0xFF1B3446),
    outline = Color(0xFF2C4A60),
    outlineVariant = Color(0xFF18303F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF9CD8FF),
    inverseSurface = Color(0xFFE1F0FF),
    inverseOnSurface = Color(0xFF08131F),
    inversePrimary = Color(0xFF2F6690),
    surfaceBright = Color(0xFF1B3446),
    surfaceDim = Color(0xFF050D15)
)

// Turkus: morska zieleń z akcentem mięty. Ciemne bary.
private val TurkusScheme = darkColorScheme(
    primary = Color(0xFF7FE3D4),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF0B4A42),
    onPrimaryContainer = Color(0xFFC8F7EE),
    secondary = Color(0xFF6FBDB2),
    secondaryContainer = Color(0xFF0A2C28),
    onSecondaryContainer = Color(0xFFC8F7EE),
    tertiary = Color(0xFF9BCFC8),
    tertiaryContainer = Color(0xFF123732),
    onTertiaryContainer = Color(0xFFDBF5F0),
    background = Color(0xFF06171A),
    onBackground = Color(0xFFDBF5F0),
    surface = Color(0xFF06171A),
    onSurface = Color(0xFFDBF5F0),
    surfaceVariant = Color(0xFF0B2226),
    onSurfaceVariant = Color(0xFF9BCABF),
    surfaceContainerLowest = Color(0xFF030F11),
    surfaceContainerLow = Color(0xFF06171A),
    surfaceContainer = Color(0xFF0B2226),
    surfaceContainerHigh = Color(0xFF102D32),
    surfaceContainerHighest = Color(0xFF16393F),
    outline = Color(0xFF2A4E52),
    outlineVariant = Color(0xFF183236),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF7FE3D4),
    inverseSurface = Color(0xFFDBF5F0),
    inverseOnSurface = Color(0xFF06171A),
    inversePrimary = Color(0xFF1F6D63),
    surfaceBright = Color(0xFF16393F),
    surfaceDim = Color(0xFF030F11)
)

// Zachód: zachodzące słońce — koral na ciemnej kawie. Ciemne bary.
private val ZachodScheme = darkColorScheme(
    primary = Color(0xFFFFA98A),
    onPrimary = Color(0xFF4A1B0A),
    primaryContainer = Color(0xFF5C2410),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFFE0906F),
    secondaryContainer = Color(0xFF332014),
    onSecondaryContainer = Color(0xFFFFDBCF),
    tertiary = Color(0xFFE8C08A),
    tertiaryContainer = Color(0xFF3A2A14),
    onTertiaryContainer = Color(0xFFFFEED4),
    background = Color(0xFF1B0F0C),
    onBackground = Color(0xFFFFEEDD),
    surface = Color(0xFF1B0F0C),
    onSurface = Color(0xFFFFEEDD),
    surfaceVariant = Color(0xFF261610),
    onSurfaceVariant = Color(0xFFD8AE97),
    surfaceContainerLowest = Color(0xFF120A07),
    surfaceContainerLow = Color(0xFF1B0F0C),
    surfaceContainer = Color(0xFF261610),
    surfaceContainerHigh = Color(0xFF311C15),
    surfaceContainerHighest = Color(0xFF3E241B),
    outline = Color(0xFF5C3A2B),
    outlineVariant = Color(0xFF3A2318),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFFFFA98A),
    inverseSurface = Color(0xFFFFEEDD),
    inverseOnSurface = Color(0xFF1B0F0C),
    inversePrimary = Color(0xFF8F4326),
    surfaceBright = Color(0xFF3E241B),
    surfaceDim = Color(0xFF120A07)
)

// Wino: wytrawna czerwień na ciemnym bordo. Ciemne bary.
private val WinoScheme = darkColorScheme(
    primary = Color(0xFFFFB3BC),
    onPrimary = Color(0xFF4C0E1B),
    primaryContainer = Color(0xFF5C1A27),
    onPrimaryContainer = Color(0xFFFFD9DE),
    secondary = Color(0xFFD999A2),
    secondaryContainer = Color(0xFF351014),
    onSecondaryContainer = Color(0xFFFFD9DE),
    tertiary = Color(0xFFE5B9AC),
    tertiaryContainer = Color(0xFF3A2019),
    onTertiaryContainer = Color(0xFFFFEDE6),
    background = Color(0xFF170C0F),
    onBackground = Color(0xFFFFEBEE),
    surface = Color(0xFF170C0F),
    onSurface = Color(0xFFFFEBEE),
    surfaceVariant = Color(0xFF221216),
    onSurfaceVariant = Color(0xFFD2A6AE),
    surfaceContainerLowest = Color(0xFF0F0709),
    surfaceContainerLow = Color(0xFF170C0F),
    surfaceContainer = Color(0xFF221216),
    surfaceContainerHigh = Color(0xFF2C171C),
    surfaceContainerHighest = Color(0xFF391E24),
    outline = Color(0xFF553038),
    outlineVariant = Color(0xFF331A20),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFFFFB3BC),
    inverseSurface = Color(0xFFFFEBEE),
    inverseOnSurface = Color(0xFF170C0F),
    inversePrimary = Color(0xFF7E2836),
    surfaceBright = Color(0xFF391E24),
    surfaceDim = Color(0xFF0F0709)
)

// Lód: jasny, chłodny błękit — dzienne światło na szybie. Jasne bary.
private val LodScheme = lightColorScheme(
    primary = Color(0xFF0F3B4A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD5E7EE),
    onPrimaryContainer = Color(0xFF0F3B4A),
    secondary = Color(0xFF4F6B75),
    secondaryContainer = Color(0xFFE2EDF1),
    onSecondaryContainer = Color(0xFF0F2A33),
    tertiary = Color(0xFF3F5B63),
    tertiaryContainer = Color(0xFFD8E6EA),
    onTertiaryContainer = Color(0xFF0F2A33),
    background = Color(0xFFEFF4F7),
    onBackground = Color(0xFF10262E),
    surface = Color(0xFFEFF4F7),
    onSurface = Color(0xFF10262E),
    surfaceVariant = Color(0xFFE0E9ED),
    onSurfaceVariant = Color(0xFF4F6B75),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF6FAFB),
    surfaceContainer = Color(0xFFECF2F5),
    surfaceContainerHigh = Color(0xFFDCE8ED),
    surfaceContainerHighest = Color(0xFFCFDFE6),
    outline = Color(0xFFA9BCC4),
    outlineVariant = Color(0xFFCFDFE6),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF0F3B4A),
    inverseSurface = Color(0xFF10262E),
    inverseOnSurface = Color(0xFFEFF4F7),
    inversePrimary = Color(0xFFC4DDE6),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFD3E0E5)
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
        AppTheme.CANDY -> CandyScheme
        AppTheme.BURSZTYN -> BursztynScheme
        AppTheme.LAS -> LasScheme
        AppTheme.LAWENDA -> LawendaScheme
        AppTheme.OCEAN -> OceanScheme
        AppTheme.TURKUS -> TurkusScheme
        AppTheme.ZACHOD -> ZachodScheme
        AppTheme.WINO -> WinoScheme
        AppTheme.LOD -> LodScheme
    }
    MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
}
