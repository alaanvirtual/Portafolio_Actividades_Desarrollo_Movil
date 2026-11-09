package com.example.redsocialnfc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DigitalAlchemyColorScheme = darkColorScheme(
    primary = NeonMagenta,
    onPrimary = DarkBackground,
    primaryContainer = SubtleViolet,
    onPrimaryContainer = NeonMagentaLight,
    secondary = ElectricViolet,
    onSecondary = TextPrimary,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = NeonCyan,
    tertiary = NeonCyan,
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
        colorScheme = DigitalAlchemyColorScheme,
        typography = Typography,
        content = content
    )
}
