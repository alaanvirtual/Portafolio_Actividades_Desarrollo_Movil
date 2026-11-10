package com.example.redsocialnfc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Esquema de colores para "Local": Morados y Grises sobrios (No luminosos)
private val LocalColorScheme = darkColorScheme(
    primary = MutedPurple,
    onPrimary = Color.White,
    primaryContainer = SubtleGrayPurple,
    onPrimaryContainer = MutedPurpleLight,
    secondary = MutedGray,
    onSecondary = TextPrimary,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = MutedAccent,
    tertiary = MutedAccent,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkOutline,
    error = ErrorColor
)

@Composable
fun RedSocialNFCTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LocalColorScheme,
        typography = Typography,
        content = content
    )
}
