package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PokerColorScheme = darkColorScheme(
    primary = PokerGold,
    onPrimary = Color.Black,
    primaryContainer = PokerGoldDark,
    onPrimaryContainer = Color.White,
    secondary = PokerEmerald,
    onSecondary = Color.Black,
    secondaryContainer = PokerFeltEmerald,
    onSecondaryContainer = Color.White,
    tertiary = PokerCyan,
    onTertiary = Color.Black,
    background = PokerFeltDark,
    onBackground = TextPrimary,
    surface = PokerSurface,
    onSurface = TextPrimary,
    surfaceVariant = PokerSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = PokerBorder,
    error = PokerRuby,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent sleek dark casino aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PokerColorScheme,
        typography = Typography,
        content = content
    )
}
