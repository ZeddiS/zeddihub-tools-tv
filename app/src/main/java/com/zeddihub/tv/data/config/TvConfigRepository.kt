package com.zeddihub.tv.data.config

import androidx.compose.ui.graphics.Color
import com.squareup.moshi.Moshi
import com.zeddihub.tv.BuildConfig
import com.zeddihub.tv.browser.Bookmark
import com.zeddihub.tv.browser.DefaultBookmarks
import com.zeddihub.tv.data.prefs.AppPrefs
import com.zeddihub.tv.media.LaunchableApp
import com.zeddihub.tv.media.StreamingApps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetches /api/tv-config.php at startup and exposes it as StateFlows.
 * Falls back to the in-app hardcoded defaults (StreamingApps.all,
 * DefaultBookmarks.all) when the network is unavailable so the app is
 * always usable offline.
 *
 * Cache strategy:
 *   • In-memory StateFlow for the current process.
 *   • DataStore for the last successful fetch — read on cold start so the
 *     UI can show admin's edits immediately, then a network refresh runs
 *     in the background and updates the flows.
 */
@Singleton
class TvConfigRepository @Inject constructor(
    private val client: OkHttpClient,
    private val moshi: Moshi,
    private val prefs: AppPrefs,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val endpoint = "${BuildConfig.API_BASE_URL}tv-config.php"

    private val _streamingApps = MutableStateFlow(StreamingApps.all)
    val streamingApps: StateFlow<List<LaunchableApp>> = _streamingApps.asStateFlow()

    private val _bookmarks = MutableStateFlow(DefaultBookmarks.all)
    val bookmarks: StateFlow<List<Bookmark>> = _bookmarks.asStateFlow()

    private val _dashboardTiles = MutableStateFlow<List<RemoteDashboardTile>>(emptyList())
    val dashboardTiles: StateFlow<List<RemoteDashboardTile>> = _dashboardTiles.asStateFlow()

    /**
     * Wires the repository at app start. Reads cached JSON from DataStore
     * (instant), then fires a network fetch (eventual). Both update the
     * StateFlows so the UI re-composes seamlessly.
     */
    fun start() {
        scope.launch {
            // Step 1: hydrate from DataStore cache (instant for cold launch)
            runCatching {
                val cached = prefs.tvRemoteConfigJson.first()
                if (cached.isNotBlank()) applyJson(cached)
            }
            // Step 2: refresh from network
            runCatching {
                val resp = client.newCall(Request.Builder().url(endpoint).build()).execute()
                val body = resp.body?.string().orEmpty()
                resp.close()
                if (body.isNotBlank()) {
                    applyJson(body)
                    prefs.setTvRemoteConfigJson(body)
                }
            }
        }
    }

    private fun applyJson(json: String) {
        val cfg = runCatching {
            moshi.adapter(TvRemoteConfig::class.java).fromJson(json)
        }.getOrNull() ?: return

        cfg.streaming_apps?.takeIf { it.isNotEmpty() }?.let { remote ->
            _streamingApps.value = remote
                .filter { it.visible != false }
                .sortedBy { it.order ?: 0 }
                .map {
                    LaunchableApp(
                        name = it.name,
                        pkg  = it.pkg,
                        tintColor = it.tint.toComposeColorOr(Color(0xFFFF8A1A)),
                    )
                }
        }

        cfg.bookmarks?.takeIf { it.isNotEmpty() }?.let { remote ->
            _bookmarks.value = remote
                .filter { it.visible != false }
                .sortedBy { it.order ?: 0 }
                .map { Bookmark(title = it.title, url = it.url) }
        }

        cfg.dashboard_tiles?.takeIf { it.isNotEmpty() }?.let { remote ->
            _dashboardTiles.value = remote
                .filter { it.visible != false }
                .sortedBy { it.order ?: 0 }
        }
    }
}
