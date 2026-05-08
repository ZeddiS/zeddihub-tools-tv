package com.zeddihub.tv.alerts

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.zeddihub.tv.data.prefs.AppPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Polls /api/alerts.php every 60 s. New alerts (not in seen-set) are
 * shown via AlertOverlayManager. Seen IDs persist in DataStore so the
 * same alert isn't re-shown after Activity recreation or process restart.
 *
 * This is a minimal alternative to FCM that works without google-services
 * (which we don't ship in v0.4.0). When FCM lands in v0.5+, the poller
 * stays as a fallback for devices without Play Services.
 */
@Singleton
class AlertsPoller @Inject constructor(
    @ApplicationContext private val ctx: Context,
    retrofit: Retrofit,
    private val overlay: AlertOverlayManager,
    private val prefs: AppPrefs,
    moshi: Moshi,
) {
    private val api: AlertsApi = retrofit.create()
    private val seenAdapter = moshi.adapter<List<String>>(
        Types.newParameterizedType(List::class.java, String::class.java)
    )
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var job: Job? = null

    private val _latest = MutableStateFlow<AlertJson?>(null)
    val latest: StateFlow<AlertJson?> = _latest.asStateFlow()

    fun start(intervalSec: Long = 60) {
        if (job?.isActive == true) return
        job = scope.launch {
            // Stagger by 5 s after app start so we don't double-pound the
            // server when many devices launch in close succession.
            delay(5_000)
            while (true) {
                runCatching { tick() }
                delay(intervalSec * 1000L)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private suspend fun tick() {
        val resp = runCatching { api.list("tv") }.getOrNull() ?: return
        if (resp.alerts.isEmpty()) return
        val seen = loadSeen().toMutableSet()
        // Show the highest-severity unseen alert. Tied severities → newest.
        val unseen = resp.alerts.filter { it.id !in seen }.sortedWith(
            compareByDescending<AlertJson> { severityRank(it.severity) }
                .thenByDescending { it.ts }
        )
        val pick = unseen.firstOrNull() ?: return
        overlay.show(pick)
        _latest.value = pick
        seen += pick.id
        saveSeen(seen.toList().takeLast(200)) // bound the seen-set
    }

    private fun severityRank(s: String): Int = when (s.lowercase()) {
        "error" -> 3; "warn" -> 2; "info" -> 1; else -> 0
    }

    private suspend fun loadSeen(): List<String> =
        runCatching { seenAdapter.fromJson(prefs.alertsSeenJson.first()) ?: emptyList() }
            .getOrDefault(emptyList())

    private suspend fun saveSeen(list: List<String>) {
        prefs.setAlertsSeenJson(seenAdapter.toJson(list))
    }
}
