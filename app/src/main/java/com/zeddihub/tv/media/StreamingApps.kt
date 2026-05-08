package com.zeddihub.tv.media

import androidx.compose.ui.graphics.Color

data class LaunchableApp(val name: String, val pkg: String, val tintColor: Color)

object StreamingApps {
    val all: List<LaunchableApp> = listOf(
        LaunchableApp("Netflix", "com.netflix.ninja", Color(0xFFE50914)),
        LaunchableApp("YouTube", "com.google.android.youtube.tv", Color(0xFFFF0000)),
        LaunchableApp("Disney+", "com.disney.disneyplus", Color(0xFF113CCF)),
        LaunchableApp("HBO Max", "com.hbo.hbonow", Color(0xFFB535F6)),
        LaunchableApp("Spotify", "com.spotify.tv.android", Color(0xFF1DB954)),
        LaunchableApp("Plex", "com.plexapp.android", Color(0xFFE5A00D)),
        LaunchableApp("Kodi", "org.xbmc.kodi", Color(0xFF17B2E7)),
        LaunchableApp("Twitch", "tv.twitch.android.app", Color(0xFF9146FF)),
    )
}
