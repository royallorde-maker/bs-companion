package com.bscompanion.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.House
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bscompanion.app.ui.theme.DnBeige
import com.bscompanion.app.ui.theme.DnBg
import com.bscompanion.app.ui.theme.DnCardHigh
import com.bscompanion.app.ui.theme.DnSub

@Composable
fun SplashScreen() {
    Box(
        Modifier.fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF211D18),
                    0.55f to DnBg,
                    1f to DnBg
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(76.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(DnCardHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.House, null, tint = DnBeige, modifier = Modifier.size(36.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text("DesignNest", color = Color(0xFFF3EDE2), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(
                "I N T E R I O R S",
                color = DnSub,
                fontSize = 10.sp,
                letterSpacing = 3.sp
            )
        }
    }
}
