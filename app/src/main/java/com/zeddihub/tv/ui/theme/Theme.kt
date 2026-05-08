package com.zeddihub.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

private val DarkColors = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBE9FF),
    secondary = Color(0xFFF0A500),
    onSecondary = Color.Black,
    background = Color(0xFF080810),
    onBackground = Color(0xFFE6E6F0),
    surface = Color(0xFF14141F),
    onSurface = Color(0xFFE6E6F0),
    surfaceVariant = Color(0xFF1C1C2A),
    onSurfaceVariant = Color(0xFFA0A0B8),
    border = Color(0xFF252535),
    borderVariant = Color(0xFF1A1A28),
    error = Color(0xFFEF4444),
    onError = Color.White,
)

@Composable
fun ZeddiHubTvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = androidx.tv.material3.Typography(),
        content = content,
    )
}
