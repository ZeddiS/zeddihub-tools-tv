package com.zeddihub.tv.data.config

import androidx.compose.ui.graphics.Color
import com.squareup.moshi.JsonClass

/**
 * Shape of the JSON returned by /api/tv-config.php. The admin can edit
 * each list independently; we treat any missing list as "use defaults".
 */
@JsonClass(generateAdapter = true)
data class TvRemoteConfig(
    val ok: Boolean? = null,
    val version: Int? = null,
    val streaming_apps: List<RemoteStreamingApp>? = null,
    val dashboard_tiles: List<RemoteDashboardTile>? = null,
    val bookmarks: List<RemoteBookmark>? = null,
    val updated_at: Long? = null,
)

@JsonClass(generateAdapter = true)
data class RemoteStreamingApp(
    val id: String,
    val name: String,
    val pkg: String,
    val tint: String? = null,        // "#RRGGBB"
    val visible: Boolean? = null,
    val order: Int? = null,
)

@JsonClass(generateAdapter = true)
data class RemoteDashboardTile(
    val id: String,
    val label: String,
    val route: String,
    val icon: String? = null,
    val tint: String? = null,
    val visible: Boolean? = null,
    val order: Int? = null,
)

@JsonClass(generateAdapter = true)
data class RemoteBookmark(
    val id: String,
    val title: String,
    val url: String,
    val visible: Boolean? = null,
    val order: Int? = null,
)

/**
 * Parse "#RRGGBB" → Color, falling back to a sensible orange when the
 * string is missing or malformed. Compose Color throws on bad hex; this
 * keeps the app robust against admin typos.
 */
fun String?.toComposeColorOr(fallback: Color): Color {
    if (this.isNullOrBlank()) return fallback
    return runCatching {
        val hex = if (startsWith('#')) substring(1) else this
        when (hex.length) {
            6 -> Color(android.graphics.Color.parseColor("#$hex"))
            8 -> Color(android.graphics.Color.parseColor("#$hex"))
            else -> fallback
        }
    }.getOrDefault(fallback)
}
