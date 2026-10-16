package com.bscompanion.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Krótki ekran startowy: ikona wyrasta, napisy wjeżdżają kaskadą, znika po ~1,1 s. */
@Composable
fun SplashScreen() {
    // Flaga startowa napędza kaskadę: ikona (scale+fade) → tytuł (slide+fade) → podpis (fade).
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    // Tweeny (nie sprężyny): wszystko na pewno zdąży przed zniknięciem splashy.
    val iconScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.6f,
        animationSpec = tween(450),
        label = "splash-scale"
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(350),
        label = "splash-icon-fade"
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(400, delayMillis = 120),
        label = "splash-title-fade"
    )
    val subAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(400, delayMillis = 240),
        label = "splash-sub-fade"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .graphicsLayer(scaleX = iconScale, scaleY = iconScale, alpha = iconAlpha)
                    .clip(RoundedCornerShape(36.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.MenuBook, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(64.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Internauta",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().graphicsLayer(alpha = titleAlpha)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Internat ZST Szczecinek",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().graphicsLayer(alpha = titleAlpha)
            )
            Spacer(Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.graphicsLayer(alpha = subAlpha)
            ) {
                Text(
                    "Plan dnia • Stołówka • Przypomnienia",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
        Text(
            "wersja ${com.bscompanion.app.BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.BottomCenter).graphicsLayer(alpha = subAlpha)
        )
    }
}
