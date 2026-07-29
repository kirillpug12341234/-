package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TelegramBlueColorScheme = lightColorScheme(
    primary = TelegramBluePrimary,
    secondary = TelegramBlueSecondary,
    background = Color(0xFFF4F4F5),
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

private val TelegramDarkColorScheme = darkColorScheme(
    primary = Color(0xFF5288C1),
    secondary = Color(0xFF64B5F6),
    background = TelegramDarkBackground,
    surface = TelegramDarkSurface,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val AmoledColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    secondary = Color(0xFF90CAF9),
    background = AmoledBackground,
    surface = AmoledSurface,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val SunsetColorScheme = darkColorScheme(
    primary = SunsetPrimary,
    secondary = Color(0xFFD35400),
    background = SunsetBackground,
    surface = SunsetSurface,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val MatrixColorScheme = darkColorScheme(
    primary = MatrixPrimary,
    secondary = Color(0xFF00CC44),
    background = MatrixBackground,
    surface = MatrixSurface,
    onPrimary = Color.Black,
    onBackground = MatrixPrimary,
    onSurface = MatrixPrimary
)

@Composable
fun TelegramTheme(
    themeMode: String = "DARK",
    content: @Composable () -> Unit
) {
    val colors = when (themeMode.uppercase()) {
        "TELEGRAM_BLUE" -> TelegramBlueColorScheme
        "AMOLED" -> AmoledColorScheme
        "SUNSET" -> SunsetColorScheme
        "MATRIX" -> MatrixColorScheme
        else -> TelegramDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
