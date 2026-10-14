package com.bscompanion.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

// Tokeny ruchu w duchu Fluent: szybko i ease-out na wejściu, fade+scale
// zamiast sztywnych liniowych przesunięć, krótkie staggery, press = szybkie
// ściśnięcie (100–400 ms wg NN/g i Fluent 2; wyjścia krótsze niż wejścia).
object FluentMotion {
    const val PressScale = 0.96f
    const val PressMs = 120
    const val EnterMs = 250
    const val ExitMs = 167
    const val StaggerMs = 40
}

/** Skala wciśnięcia śledząca palec (0.96 w dół, sprężyście w górę). */
@Composable
fun rememberFluentPress(
    interaction: MutableInteractionSource,
    scale: Float = FluentMotion.PressScale
): Float {
    val pressed by interaction.collectIsPressedAsState()
    return animateFloatAsState(
        if (pressed) scale else 1f,
        tween(FluentMotion.PressMs),
        label = "press"
    ).value
}

/** Nakłada śledzoną skalę (środek elementu). */
fun Modifier.fluentScale(value: Float): Modifier =
    graphicsLayer(scaleX = value, scaleY = value)
