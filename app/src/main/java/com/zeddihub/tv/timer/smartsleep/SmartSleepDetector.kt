package com.zeddihub.tv.timer.smartsleep

import android.content.Context
import android.media.AudioManager
import android.os.SystemClock
import com.zeddihub.tv.data.prefs.AppPrefs
import com.zeddihub.tv.timer.TimerAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Watches user input via the accessibility-service hook + audio activity,
 * and once both have been quiet for `prefs.smartSleepIdleMinutes`, raises
 * a nudge that the UI can show ("looks like you're asleep — shut down?").
 *
 * Why audio matters: a long silent movie shouldn't trigger auto-sleep
 * just because no D-pad input arrived for 30 minutes. We only suggest
 * shutdown if STREAM_MUSIC has been quiet for the same window.
 */
@Singleton
class SmartSleepDetector @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val prefs: AppPrefs,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val am: AudioManager =
        ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    @Volatile private var lastInputAt: Long = SystemClock.elapsedRealtime()
    @Volatile private var lastAudioAt: Long = SystemClock.elapsedRealtime()

    private val _nudge = MutableStateFlow(false)
    val nudge: StateFlow<Boolean> = _nudge.asStateFlow()

    fun start() {
        scope.launch {
            while (true) {
                val thresholdMin = runCatching { prefs.smartSleepIdleMinutes.first() }.getOrDefault(0)
                if (thresholdMin > 0) tick(thresholdMin)
                // Audio sample regardless — we want a fresh "last audio" if
                // playback resumes during a silent stretch.
                if (am.isMusicActive) lastAudioAt = SystemClock.elapsedRealtime()
                delay(15_000) // 15s cadence is enough; thresholds are minutes
            }
        }
    }

    fun notifyInput() {
        lastInputAt = SystemClock.elapsedRealtime()
        // Resetting on input also dismisses any pending nudge — once the
        // user touches the remote we know they're awake.
        if (_nudge.value) _nudge.value = false
    }

    fun dismissNudge() { _nudge.value = false }

    fun acceptShutdown() {
        _nudge.value = false
        TimerAccessibilityService.requestLockScreen(ctx)
    }

    private fun tick(thresholdMinutes: Int) {
        val thresholdMs = thresholdMinutes * 60_000L
        val now = SystemClock.elapsedRealtime()
        val idleInput = now - lastInputAt >= thresholdMs
        val idleAudio = now - lastAudioAt >= thresholdMs
        if (idleInput && idleAudio && !_nudge.value) {
            _nudge.value = true
        }
    }
}
