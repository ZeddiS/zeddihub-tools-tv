package com.zeddihub.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

/**
 * ZeddiHub TV theme — orange brand on black, matching the official logo
 * (the orange "Hub" tile on dark background).
 *
 * Color choices:
 *   primary       = brand orange (#F39200) — used for focused state, active
 *                   tiles, primary actions
 *   secondary     = warm yellow accent for highlights (subtle counterpoint
 *                   to primary, useful for status badges)
 *   surface       = #14141F — slightly lifted from background for cards
 *   border        = #252535 — gives shape to surfaces without halos
 *
 * Why not a `lightColorScheme()`: TV is universally consumed in dim rooms
 * and on dark panels; a light theme would compete with content. Settings
 * still expose a "system / dark / amoled" toggle for future expansion,
 * today they all map to this dark palette.
 */
private val DarkColors = darkColorScheme(
    primary = Color(0xFFF39200),
    onPrimary = Color(0xFF1A0F00),
    primaryContainer = Color(0xFF4A2E00),
    onPrimaryContainer = Color(0xFFFFE0BE),
    secondary = Color(0xFFFFC078),
    onSecondary = Color(0xFF2A1A00),
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
