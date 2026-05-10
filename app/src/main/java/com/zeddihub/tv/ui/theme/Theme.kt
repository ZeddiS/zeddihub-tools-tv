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
    secondaryContainer = Color(0xFF3A2700),
    onSecondaryContainer = Color(0xFFFFD8A0),
    // Tertiary — purple complement to brand orange. Used in update dialog
    // gradient + dashboard hero. Without an explicit value the TV M3 default
    // is a pinkish lilac that fights with the orange.
    tertiary = Color(0xFFA78BFA),
    onTertiary = Color(0xFF1A0F36),
    tertiaryContainer = Color(0xFF3F2E72),
    onTertiaryContainer = Color(0xFFE9DDFF),
    // v0.1.12 — true-black background pro OLED panely + agresivnější
    // kontrast s gradient hero kartami. Surface zůstává mírně zvýšený
    // aby karty plavaly nad pozadím (depth perception).
    background = Color(0xFF000000),
    onBackground = Color(0xFFEEEEF5),
    surface = Color(0xFF0E0E18),
    onSurface = Color(0xFFEEEEF5),
    surfaceVariant = Color(0xFF14141F),
    onSurfaceVariant = Color(0xFFB8B8CC),
    border = Color(0xFF1F1F2E),
    borderVariant = Color(0xFF12121C),
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
