package com.bscompanion.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

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

/**
 * Bębenek jak w flip/slot-clock: każdy znak roluje osobno (5→4 rusza tylko
 * ostatni, 40→39 rusza oba), reszta stoi. Pozycyjnie po indeksie — długość
 * "HH:mm" jest stała, więc znaki nie mylą ról.
 */
@Composable
fun RollingText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        text.forEachIndexed { idx, ch ->
            key(idx) {
                AnimatedContent(
                    targetState = ch,
                    transitionSpec = {
                        (slideInVertically(
                            tween(FluentMotion.EnterMs, easing = LinearOutSlowInEasing)
                        ) { it / 2 } + fadeIn(tween(FluentMotion.EnterMs))) togetherWith
                            (slideOutVertically(
                                tween(FluentMotion.ExitMs, easing = FastOutLinearInEasing)
                            ) { -it / 2 } + fadeOut(tween(FluentMotion.ExitMs)))
                    },
                    label = "rolling"
                ) { c ->
                    Text(
                        c.toString(),
                        style = style,
                        fontWeight = fontWeight,
                        color = color,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Visible
                    )
                }
            }
        }
    }
}
