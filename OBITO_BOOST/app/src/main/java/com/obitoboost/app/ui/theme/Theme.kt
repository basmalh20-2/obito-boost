package com.obitoboost.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ObitoDarkScheme = darkColorScheme(
    primary = CyanGlow,
    onPrimary = NavyDeep,
    secondary = Gold,
    onSecondary = NavyDeep,
    background = NavyDeep,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavyCard,
    onSurfaceVariant = TextSecondary,
    outline = NavyCardBorder,
    error = StatusDanger,
)

@Composable
fun ObitoBoostTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ObitoDarkScheme,
        typography = ObitoTypography,
        content = content
    )
}

/** The signature deep-navy background with a subtle cyan radial glow, used behind every screen. */
fun Modifier.obitoBackground(): Modifier = this.background(
    Brush.radialGradient(
        colors = listOf(NavySurface, NavyDeep),
        radius = 1400f
    )
)

/** Card surface with the brand's soft cyan border used across dashboards. */
fun Modifier.obitoCard(): Modifier = this
    .clip(RoundedCornerShape(18.dp))
    .background(NavyCard)

val ObitoCardBorderColor: Color get() = NavyCardBorder
