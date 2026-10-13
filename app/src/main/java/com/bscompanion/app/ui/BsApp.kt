package com.bscompanion.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bscompanion.app.ui.theme.DnBeige
import com.bscompanion.app.ui.theme.DnBg
import com.bscompanion.app.ui.theme.DnCard
import com.bscompanion.app.ui.theme.DnCardHigh
import com.bscompanion.app.ui.theme.DnFaint
import com.bscompanion.app.ui.theme.DnStroke
import com.bscompanion.app.ui.theme.DnSub
import com.bscompanion.app.ui.theme.DnText

// ---------------------------------------------------------------------------
// Root — Onboarding -> Home -> Living Room detail, exactly like the mockup.
// ---------------------------------------------------------------------------

@Composable
fun BsApp() {
    var route by rememberSaveable { mutableStateOf("onboarding") }
    Box(Modifier.fillMaxSize().background(DnBg)) {
        when (route) {
            "onboarding" -> OnboardingScreen(onStart = { route = "home" })
            "home" -> HomeScreen(onOpenRoom = { route = "detail" })
            else -> DetailScreen(onBack = { route = "home" })
        }
    }
}

// Back-compat overload (MainActivity previously passed theme params).
@Composable
fun BsApp(themeMode: Int, onThemeChange: (Int) -> Unit) {
    BsApp()
}

// ---------------------------------------------------------------------------
// Painted interior — gradient + lamp glow + sofa silhouette (no photo assets).
// Variants shift the mood: 0 living warm, 1 beige project, 2 bedroom, 3 kitchen,
// 4 tool chair, 5 tool 3d, 6-8 recents.
// ---------------------------------------------------------------------------

@Composable
fun InteriorArt(modifier: Modifier = Modifier, variant: Int = 0) {
    val top = when (variant) {
        1 -> Color(0xFF4E463C)
        2 -> Color(0xFF41382E)
        3 -> Color(0xFF3A3A36)
        4 -> Color(0xFF463C31)
        5 -> Color(0xFF3C3833)
        6 -> Color(0xFF453B30)
        7 -> Color(0xFF3B332B)
        else -> Color(0xFF4A4136)
    }
    val mid = when (variant) {
        1 -> Color(0xFF2C2620)
        else -> Color(0xFF221D18)
    }
    Canvas(modifier) {
        drawRect(Brush.verticalGradient(0f to top, 0.55f to mid, 1f to Color(0xFF141210)))
        val w = size.width
        val h = size.height
        // warm ceiling glow
        drawCircle(brush = Brush.radialGradient(
                0f to Color(0xFFD9C2A0).copy(alpha = 0.55f),
                1f to Color.Transparent,
                center = Offset(w * 0.5f, h * 0.16f),
                radius = w * 0.55f
            ),
            radius = w * 0.55f,
            center = Offset(w * 0.5f, h * 0.16f)
        )
        // pendant cord + shade
        val cx = w * (0.38f + (variant % 3) * 0.1f)
        drawLine(Color(0xFF0C0B0A), Offset(cx, 0f), Offset(cx, h * 0.22f), strokeWidth = 2.5f)
        drawCircle(color = Color(0xFF0E0D0C), radius = w * 0.13f, center = Offset(cx, h * 0.27f))
        drawCircle(color = Color(0xFFE8D3AE).copy(alpha = 0.9f), radius = w * 0.045f, center = Offset(cx, h * 0.30f))
        drawCircle(brush = Brush.radialGradient(
                0f to Color(0xFFFFE9C4).copy(alpha = 0.5f),
                1f to Color.Transparent,
                center = Offset(cx, h * 0.42f),
                radius = w * 0.4f
            ),
            radius = w * 0.4f,
            center = Offset(cx, h * 0.42f)
        )
        // back wall shelf line
        drawRoundRect(
            Color(0xFF38312A).copy(alpha = 0.9f),
            topLeft = Offset(w * 0.08f, h * 0.38f),
            size = androidx.compose.ui.geometry.Size(w * 0.84f, h * 0.012f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        // sofa body + arms + cushions
        val sofaTop = h * 0.62f
        val sofaH = h * 0.22f
        drawRoundRect(Color(0xFF39322A), Offset(w * 0.10f, sofaTop), androidx.compose.ui.geometry.Size(w * 0.80f, sofaH), CornerRadius(18f, 18f))
        drawRoundRect(Color(0xFF453C31), Offset(w * 0.14f, sofaTop - h * 0.055f), androidx.compose.ui.geometry.Size(w * 0.33f, h * 0.10f), CornerRadius(14f, 14f))
        drawRoundRect(Color(0xFF453C31), Offset(w * 0.53f, sofaTop - h * 0.055f), androidx.compose.ui.geometry.Size(w * 0.33f, h * 0.10f), CornerRadius(14f, 14f))
        drawRoundRect(Color(0xFF2B251F), Offset(w * 0.10f, sofaTop + sofaH - h * 0.02f), androidx.compose.ui.geometry.Size(w * 0.80f, h * 0.05f), CornerRadius(10f, 10f))
        // side table + bowl
        drawRoundRect(Color(0xFF2E2822), Offset(w * 0.06f, h * 0.78f), androidx.compose.ui.geometry.Size(w * 0.24f, h * 0.03f), CornerRadius(8f, 8f))
        drawCircle(color = Color(0xFF1D1A16), radius = w * 0.055f, center = Offset(w * 0.18f, h * 0.755f))
        // floor glow
        drawRect(
            Brush.verticalGradient(
                0f to Color.Transparent,
                1f to Color(0xFFD9C2A0).copy(alpha = 0.10f)
            ),
            topLeft = Offset(0f, h * 0.82f),
            size = androidx.compose.ui.geometry.Size(w, h * 0.18f)
        )
    }
}

// ---------------------------------------------------------------------------
// 1) ONBOARDING
// ---------------------------------------------------------------------------

@Composable
fun OnboardingScreen(onStart: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        InteriorArt(Modifier.fillMaxSize(), variant = 0)
        // darken edges for legibility
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.25f),
                    0.45f to Color.Transparent,
                    0.72f to Color(0xFF0E0D0C).copy(alpha = 0.72f),
                    1f to Color(0xFF0E0D0C).copy(alpha = 0.96f)
                )
            )
        )
        Column(
            Modifier.fillMaxSize().padding(horizontal = 22.dp).padding(top = 54.dp, bottom = 26.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Home, null, tint = DnText, modifier = Modifier.size(19.dp))
                }
                Spacer(Modifier.width(9.dp))
                Column {
                    Text("DesignNest", color = DnText, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.2.sp)
                    Text("INTERIORS", color = DnSub, fontSize = 8.sp, letterSpacing = 2.4.sp)
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Beautiful Spaces,\nThoughtfully Designed",
                color = DnText,
                fontSize = 29.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Transform your house into a home\nwith personalized interior design.",
                color = DnSub,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(DnBeige)
                    .clickable(onClick = onStart)
                    .padding(horizontal = 22.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Get Started", color = Color(0xFF1C1712), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Box(
                    Modifier.size(34.dp).clip(CircleShape).background(Color(0xFF1C1712)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ArrowForward, null, tint = DnBeige, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Box(Modifier.width(22.dp).height(4.dp).clip(CircleShape).background(DnBeige))
                Spacer(Modifier.width(6.dp))
                Box(Modifier.size(4.dp).clip(CircleShape).background(DnFaint))
                Spacer(Modifier.width(6.dp))
                Box(Modifier.size(4.dp).clip(CircleShape).background(DnFaint))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 2) HOME
// ---------------------------------------------------------------------------

@Composable
fun HomeScreen(onOpenRoom: () -> Unit) {
    Scaffold(
        containerColor = DnBg,
        bottomBar = { DnBottomBar() }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF8A6E52), Color(0xFF4A3B2C)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("E", color = DnText, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Welcome Back,", color = DnSub, fontSize = 11.sp)
                    Text("Emma Johnson", color = DnText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    Modifier.size(42.dp).clip(CircleShape).background(DnCard).clickable {},
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Notifications, null, tint = DnText, modifier = Modifier.size(20.dp))
                }
            }

            // My Project card (light)
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(DnBeige)
                    .clickable(onClick = onOpenRoom).padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Box(
                            Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF1C1712).copy(alpha = 0.08f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("My Project", color = Color(0xFF5A4A36), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Modern Living\nRoom", color = Color(0xFF1C1712), fontSize = 19.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row {
                                listOf(Color(0xFF6B5844), Color(0xFF3E352C), Color(0xFF8A7360)).forEachIndexed { i, c ->
                                    Box(
                                        Modifier.size(22.dp).clip(CircleShape).background(c)
                                    ) {
                                        if (i == 0) Box(Modifier.align(Alignment.Center).size(14.dp).clip(CircleShape).background(Color(0xFFD9BE96)))
                                    }
                                }
                            }
                            Spacer(Modifier.width(6.dp))
                            Text("+12 Members", color = Color(0xFF5A4A36), fontSize = 10.sp)
                            Spacer(Modifier.width(8.dp))
                            Box(
                                Modifier.size(34.dp).clip(CircleShape).background(Color(0xFF1C1712)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.ArrowForward, null, tint = DnBeige, modifier = Modifier.size(17.dp))
                            }
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(
                        Modifier.width(96.dp).clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE5D2B4)).padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                            Canvas(Modifier.fillMaxSize()) {
                                drawLine(Color(0xFF5A4A36), Offset(size.width / 2, 0f), Offset(size.width / 2, size.height * 0.4f), 2f)
                                drawArc(Color(0xFF5A4A36), 0f, 180f, true, topLeft = Offset(size.width * 0.18f, size.height * 0.25f), size = androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.5f))
                            }
                        }
                        Text("Warm Neutral", color = Color(0xFF1C1712), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Mood", color = Color(0xFF5A4A36), fontSize = 9.sp)
                        Spacer(Modifier.height(6.dp))
                        Row {
                            Box(Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF1C1712)))
                            Spacer(Modifier.width(4.dp))
                            Box(Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF1C1712).copy(alpha = 0.25f)))
                            Spacer(Modifier.width(4.dp))
                            Box(Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF1C1712).copy(alpha = 0.25f)))
                        }
                    }
                }
            }

            // My Spaces
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("My Spaces", color = DnText, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("See All", color = DnSub, fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SpaceTile(Icons.Filled.Weekend, "Living Room", selected = true, modifier = Modifier.weight(1f), onClick = onOpenRoom)
                SpaceTile(Icons.Filled.Bed, "Bedroom", selected = false, modifier = Modifier.weight(1f), onClick = {})
                SpaceTile(Icons.Filled.Kitchen, "Kitchen", selected = false, modifier = Modifier.weight(1f), onClick = {})
                SpaceTile(Icons.Filled.Add, "Add Space", selected = false, modifier = Modifier.weight(1f), onClick = {})
            }

            // Quick Tools
            Text("Quick Tools", color = DnText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier.weight(1f).height(148.dp).clip(RoundedCornerShape(18.dp)).background(DnCard)
                ) {
                    InteriorArt(Modifier.fillMaxSize(), variant = 4)
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.55f))))
                    Column(Modifier.fillMaxSize().padding(12.dp)) {
                        Text("Design Ideas", color = DnText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Inspiration for\nyour dream space", color = DnSub, fontSize = 10.sp, lineHeight = 13.sp)
                        Spacer(Modifier.weight(1f))
                        Box(Modifier.size(30.dp).clip(CircleShape).background(DnBeige), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Lightbulb, null, tint = Color(0xFF1C1712), modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Box(
                    Modifier.weight(1f).height(148.dp).clip(RoundedCornerShape(18.dp)).background(DnCard)
                ) {
                    InteriorArt(Modifier.fillMaxSize(), variant = 5)
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.55f))))
                    Column(Modifier.fillMaxSize().padding(12.dp)) {
                        Text("3D Visualizer", color = DnText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Bring your vision\ncome to life", color = DnSub, fontSize = 10.sp, lineHeight = 13.sp)
                        Spacer(Modifier.weight(1f))
                        Box(Modifier.size(30.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Widgets, null, tint = DnText, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Recent Projects
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Recent Projects", color = DnText, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("See All", color = DnSub, fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RecentCard("Modern Living Room", "In Progress", 6, Modifier.weight(1f), onOpenRoom)
                RecentCard("Luxury Bedroom", "Completed", 7, Modifier.weight(1f)) {}
                RecentCard("Minimalist Kitchen", "New Design", 3, Modifier.weight(1f)) {}
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
fun SpaceTile(icon: ImageVector, label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.fillMaxWidth().height(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (selected) DnBeige else DnCard)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = if (selected) Color(0xFF1C1712) else DnSub, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = if (selected) DnText else DnSub, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun RecentCard(title: String, status: String, variant: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.height(150.dp).clip(RoundedCornerShape(16.dp)).background(DnCard).clickable(onClick = onClick)
    ) {
        InteriorArt(Modifier.fillMaxSize(), variant = variant)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Transparent, 0.55f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.75f))))
        Column(Modifier.fillMaxSize().padding(9.dp)) {
            Spacer(Modifier.weight(1f))
            Text(title, color = DnText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 13.sp)
            Text(status, color = DnBeige, fontSize = 9.sp)
        }
    }
}

@Composable
fun DnBottomBar() {
    Box(Modifier.fillMaxWidth().background(DnBg).padding(horizontal = 18.dp, vertical = 10.dp)) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(DnCard).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Row(
                Modifier.clip(RoundedCornerShape(22.dp)).background(DnBeige).padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Home, null, tint = Color(0xFF1C1712), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Home", color = Color(0xFF1C1712), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Icon(Icons.Filled.Description, null, tint = DnSub, modifier = Modifier.size(22.dp))
            Icon(Icons.Filled.FavoriteBorder, null, tint = DnSub, modifier = Modifier.size(22.dp))
            Icon(Icons.Filled.PersonOutline, null, tint = DnSub, modifier = Modifier.size(22.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// 3) LIVING ROOM DETAIL
// ---------------------------------------------------------------------------

@Composable
fun DetailScreen(onBack: () -> Unit) {
    var brightness by rememberSaveable { mutableFloatStateOf(0.62f) }
    var tab by rememberSaveable { mutableStateOf("Lighting") }
    var mood by rememberSaveable { mutableStateOf("Warm") }
    var power by rememberSaveable { mutableStateOf(true) }

    Scaffold(containerColor = DnBg) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 22.dp)
        ) {
            // hero
            item {
                Box(Modifier.fillMaxWidth().height(430.dp)) {
                    InteriorArt(Modifier.fillMaxSize(), variant = 0)
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Black.copy(alpha = 0.35f), 0.4f to Color.Transparent, 1f to DnBg)))
                    // top bar
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RoundBtn(Icons.Filled.ArrowBack, onClick = onBack)
                        Spacer(Modifier.weight(1f))
                        RoundBtn(Icons.Filled.FavoriteBorder, onClick = {})
                        Spacer(Modifier.width(10.dp))
                        RoundBtn(Icons.Filled.MoreVert, onClick = {})
                    }
                    // bottom content over hero
                    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp).padding(top = 200.dp)) {
                        Spacer(Modifier.weight(1f))
                        Text("Living Room", color = DnText, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Text("Design the perfect ambiance\nfor your space.", color = DnSub, fontSize = 12.sp, lineHeight = 17.sp)
                        Spacer(Modifier.height(12.dp))
                        Box(
                            Modifier.size(46.dp).clip(CircleShape)
                                .background(if (power) DnBeige else DnCardHigh)
                                .clickable { power = !power },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.PowerSettingsNew, null,
                                tint = if (power) Color(0xFF1C1712) else DnSub,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.WbSunny, null, tint = DnSub, modifier = Modifier.size(16.dp))
                            Slider(
                                value = brightness,
                                onValueChange = { brightness = it },
                                modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = DnBeige,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.18f)
                                )
                            )
                            Icon(Icons.Filled.WbSunny, null, tint = DnText, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            // tabs
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Lighting", "Colors", "Furniture", "Decor").forEach { t ->
                        val sel = t == tab
                        Box(
                            Modifier.clip(RoundedCornerShape(20.dp))
                                .background(if (sel) DnBeige else DnCard)
                                .clickable { tab = t }
                                .padding(horizontal = 16.dp, vertical = 9.dp)
                        ) {
                            Text(t, color = if (sel) Color(0xFF1C1712) else DnSub, fontSize = 12.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
            // mood cards
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MoodCard("Warm", "Cozy & Relaxing", mood == "Warm", Modifier.weight(1f)) { mood = "Warm" }
                    MoodCard("Natural", "Balanced", mood == "Natural", Modifier.weight(1f)) { mood = "Natural" }
                    MoodCard("Bright White", "Focused", mood == "Bright White", Modifier.weight(1f)) { mood = "Bright White" }
                }
                Spacer(Modifier.height(14.dp))
            }
            // schedule
            item {
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp)
                        .clip(RoundedCornerShape(18.dp)).background(DnCard).padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Schedule", color = DnText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp))
                            Row {
                                Column(Modifier.weight(1f)) {
                                    Text("From", color = DnFaint, fontSize = 10.sp)
                                    Text("06:00 PM", color = DnText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column(Modifier.weight(1f)) {
                                    Text("Until", color = DnFaint, fontSize = 10.sp)
                                    Text("11:00 PM", color = DnText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Box(
                            Modifier.size(36.dp).clip(CircleShape).background(DnCardHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Edit, null, tint = DnText, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoundBtn(icon: ImageVector, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.42f)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val arrowBack = icon == Icons.Filled.ArrowBack
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(if (arrowBack) 20.dp else 19.dp))
    }
}

@Composable
fun MoodCard(title: String, desc: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val border = if (selected) DnBeige else DnStroke
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DnCard)
            .clickable(onClick = onClick)
            .padding(1.5.dp)
    ) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(DnCard)
                .padding(1.5.dp)
        ) {
            // fake border via outer tint
            Box(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) Color(0xFF2E2A24) else DnCard)
                    .padding(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Box(Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF171412))) {
                        Canvas(Modifier.fillMaxSize()) {
                            drawCircle(color = Color(0xFFD9C2A0).copy(alpha = 0.35f), radius = size.width * 0.30f, center = Offset(size.width / 2, size.height * 0.32f))
                            drawLine(Color(0xFF6E6252), Offset(size.width / 2, 0f), Offset(size.width / 2, size.height * 0.30f), 2f)
                            drawArc(
                                Color(if (selected) 0xFFD9C2A0 else 0xFF5A5348),
                                0f, 180f, true,
                                topLeft = Offset(size.width * 0.30f, size.height * 0.18f),
                                size = androidx.compose.ui.geometry.Size(size.width * 0.40f, size.height * 0.42f)
                            )
                            drawCircle(color = Color(0xFFFFE9C4).copy(alpha = if (selected) 0.95f else 0.35f), radius = 5f, center = Offset(size.width / 2, size.height * 0.44f))
                        }
                        if (selected) {
                            Box(
                                Modifier.align(Alignment.TopStart).padding(6.dp).size(20.dp).clip(CircleShape)
                                    .background(DnBeige),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✦", color = Color(0xFF1C1712), fontSize = 11.sp)
                            }
                        } else {
                            Box(
                                Modifier.align(Alignment.TopStart).padding(6.dp).size(20.dp).clip(CircleShape)
                                    .background(Color.Transparent)
                            ) {
                                Text("+", color = DnSub, fontSize = 13.sp, modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(title, color = DnText, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(desc, color = DnSub, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (selected) {
                        Spacer(Modifier.height(2.dp))
                        Box(Modifier.width(22.dp).height(2.dp).clip(CircleShape).background(DnBeige))
                    }
                }
            }
        }
    }
}

// keep unused-import guard happy
@Suppress("unused")
private val _keepDp: Dp = 1.dp
