package com.cryptoscope.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF0B0E14)
val DarkSurface = Color(0xFF151A24)
val DarkCard = Color(0xFF1E2433)
val CryptoGreen = Color(0xFF00C087)
val CryptoRed = Color(0xFFFF3B30)
val CryptoGold = Color(0xFFFFD700)
val TextPrimary = Color(0xFFF0F4F8)
val TextSecondary = Color(0xFF9BA3AF)

private val DarkColorScheme = darkColorScheme(
    primary = CryptoGreen,
    secondary = CryptoGold,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun CryptoScopeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
