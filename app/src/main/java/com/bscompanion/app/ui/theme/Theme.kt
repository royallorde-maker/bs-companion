package com.bscompanion.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// DesignNest — warm dark interiors palette sampled from the mockup.
val DnBg = Color(0xFF131210)
val DnSurface = Color(0xFF1D1C1A)
val DnCard = Color(0xFF232120)
val DnCardHigh = Color(0xFF2B2926)
val DnStroke = Color(0xFF35322D)
val DnBeige = Color(0xFFD9C2A0)
val DnBeigeDeep = Color(0xFFC4A87F)
val DnText = Color(0xFFF3EDE2)
val DnSub = Color(0xFFA39E93)
val DnFaint = Color(0xFF6E6A62)

private val DesignNestScheme = darkColorScheme(
    primary = DnBeige,
    onPrimary = Color(0xFF1C1712),
    primaryContainer = DnCardHigh,
    onPrimaryContainer = DnText,
    secondary = DnSub,
    secondaryContainer = DnCard,
    onSecondaryContainer = DnText,
    tertiary = DnBeigeDeep,
    background = DnBg,
    onBackground = DnText,
    surface = DnBg,
    onSurface = DnText,
    surfaceVariant = DnCard,
    onSurfaceVariant = DnSub,
    surfaceContainerLowest = Color(0xFF0E0D0C),
    surfaceContainerLow = Color(0xFF171614),
    surfaceContainer = DnCard,
    surfaceContainerHigh = DnCardHigh,
    surfaceContainerHighest = Color(0xFF35322C),
    outline = DnStroke,
    outlineVariant = Color(0xFF2A2825),
    scrim = Color(0xFF000000),
    surfaceTint = DnBeige,
    inverseSurface = DnText,
    inverseOnSurface = DnBg,
    inversePrimary = Color(0xFF5A4A36)
)

private val DnTypography = Typography(
    displaySmall = Typography().displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineSmall = Typography().headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLarge = Typography().titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = Typography().titleMedium.copy(fontWeight = FontWeight.SemiBold),
    bodyMedium = Typography().bodyMedium.copy(lineHeight = 20.sp),
    bodySmall = Typography().bodySmall.copy(lineHeight = 18.sp),
    labelSmall = Typography().labelSmall.copy(letterSpacing = 0.4.sp)
)

@Composable
fun BSCompanionTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    // DesignNest is dark-only — ignore system/light to match the mockup exactly.
    MaterialTheme(colorScheme = DesignNestScheme, typography = DnTypography, content = content)
}
