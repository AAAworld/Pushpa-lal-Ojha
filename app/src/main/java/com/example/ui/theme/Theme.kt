package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGold,
    secondary = SecondaryNebula,
    tertiary = TertiaryTeal,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = OnBackgroundDark,
    onSurface = OnBackgroundDark,
    onPrimary = OnPrimaryDark,
    onSecondary = OnPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGoldVariant,
    secondary = SecondaryNebula,
    tertiary = TertiaryTeal,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = OnBackgroundLight,
    onSurface = OnBackgroundLight,
    onPrimary = OnPrimaryLight,
    onSecondary = OnPrimaryLight
)

@Composable
fun CosmicAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We prioritize our highly customized spiritual dark/light color schemes
    // to preserve the spiritual, magical aura of the application.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
