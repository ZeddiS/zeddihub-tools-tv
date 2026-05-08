package com.zeddihub.tv.timer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.zeddihub.tv.MainActivity
import com.zeddihub.tv.R
import com.zeddihub.tv.ZeddiHubTvApp
import com.zeddihub.tv.data.prefs.AppPrefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

/**
 * Foreground service that runs the sleep countdown.
 *
 * Lifecycle:
 *   START(durationMs)  → IDLE/* → RUNNING (acquires partial wake lock, ticks every 1 s)
 *   PAUSE              → RUNNING → PAUSED (preserves remaining)
 *   RESUME             → PAUSED  → RUNNING
 *   STOP               → */RUNNING/PAUSED → IDLE (kills overlay, releases lock, stops self)
 *   SHUTDOWN_NOW       → calls overlay manager to dispatch GLOBAL_ACTION_LOCK_SCREEN (via a11y) and stops
 *
 * On reaching 0 ms: optional 10 s audio fade-out (if enabled), then GLOBAL_ACTION_LOCK_SCREEN.
 */
@AndroidEntryPoint
class SleepTimerService : Service() {

    @Inject lateinit var timerState: TimerState
    @Inject lateinit var overlayManager: TimerOverlayManager
    @Inject lateinit var prefs: AppPrefs

    private val scope = CoroutineScope(Dispatchers.Default)
    private var tickJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var endRealtimeMs: Long = 0L
    private var pausedRemainingMs: Long = 0L
    private var totalMs: Long = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            TimerActions.ACTION_START -> {
                val dur = intent.getLongExtra(TimerActions.EXTRA_DURATION_MS, 0L).coerceAtLeast(1_000L)
                start(dur)
            }
            TimerActions.ACTION_PAUSE -> pause()
            TimerActions.ACTION_RESUME -> resume()
            TimerActions.ACTION_STOP -> { stop(); return START_NOT_STICKY }
            TimerActions.ACTION_SHUTDOWN_NOW -> { shutdownNow(); return START_NOT_STICKY }
            TimerActions.ACTION_SHOW_QUICK_ACTIONS -> overlayManager.showQuickActions()
            TimerActions.ACTION_HIDE_QUICK_ACTIONS -> overlayManager.hideQuickActions()
        }
        return START_STICKY
    }

    private fun start(durationMs: Long) {
        totalMs = durationMs
        endRealtimeMs = SystemClock.elapsedRealtime() + durationMs
        pausedRemainingMs = 0L
        acquireWakeLock()
        promote(durationMs)
        timerState.update(TimerSnapshot(TimerStatus.RUNNING, totalMs, durationMs))
        overlayManager.show()
        runTicker()
    }

    private fun pause() {
        if (timerState.current().status != TimerStatus.RUNNING) return
        tickJob?.cancel()
        pausedRemainingMs = max(0L, endRealtimeMs - SystemClock.elapsedRealtime())
        timerState.update(timerState.current().copy(status = TimerStatus.PAUSED, remainingMs = pausedRemainingMs))
        overlayManager.refresh()
        promote(pausedRemainingMs, paused = true)
    }

    private fun resume() {
        if (timerState.current().status != TimerStatus.PAUSED) return
        endRealtimeMs = SystemClock.elapsedRealtime() + pausedRemainingMs
        timerState.update(timerState.current().copy(status = TimerStatus.RUNNING))
        promote(pausedRemainingMs)
        runTicker()
    }

    private fun stop() {
        tickJob?.cancel()
        releaseWakeLock()
        overlayManager.hide()
        timerState.update(TimerSnapshot())
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun shutdownNow() {
        timerState.update(timerState.current().copy(status = TimerStatus.EXPIRED, remainingMs = 0L))
        overlayManager.hide()
        TimerAccessibilityService.requestLockScreen(applicationContext)
        // give accessibility 1 s to dispatch, then clean up
        scope.launch { delay(1_500); stop() }
    }

    private fun runTicker() {
        tickJob?.cancel()
        tickJob = scope.launch {
            val fadeEnabled = runCatching { prefs.timerFadeAudio.first() }.getOrDefault(true)
            var fadeApplied = false
            while (true) {
                val now = SystemClock.elapsedRealtime()
                val remaining = max(0L, endRealtimeMs - now)
                timerState.update(TimerSnapshot(TimerStatus.RUNNING, totalMs, remaining))
                overlayManager.refresh()
                updateNotif(remaining, paused = false)

                // Audio fade-out in last 10 seconds
                if (fadeEnabled && !fadeApplied && remaining in 1L..10_000L) {
                    fadeApplied = true
                    fadeAudioOver(remaining)
                }

                if (remaining <= 0L) break
                delay(min(1_000L, remaining))
            }
            shutdownNow()
        }
    }

    private fun fadeAudioOver(durationMs: Long) {
        runCatching {
            val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val startVol = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            scope.launch {
                val steps = 10
                val stepMs = durationMs / steps
                for (i in 1..steps) {
                    val target = (startVol * (steps - i) / steps).coerceAtLeast(0)
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
                    delay(stepMs)
                }
                // restore after stop so user isn't left with muted TV next session
                am.setStreamVolume(AudioManager.STREAM_MUSIC, startVol, 0)
            }
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ZeddiHubTV:SleepTimer").apply {
            setReferenceCounted(false)
            acquire(24 * 60 * 60 * 1000L)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    private fun promote(remainingMs: Long, paused: Boolean = false) {
        val notif = buildNotif(remainingMs, paused)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID, notif)
        }
    }

    private fun updateNotif(remainingMs: Long, paused: Boolean) {
        val nm = androidx.core.app.NotificationManagerCompat.from(this)
        nm.notify(NOTIF_ID, buildNotif(remainingMs, paused))
    }

    private fun buildNotif(remainingMs: Long, paused: Boolean): Notification {
        val title = if (paused)
            getString(R.string.timer_notif_paused, formatRemaining(remainingMs))
        else
            getString(R.string.timer_notif_running, formatRemaining(remainingMs))

        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, SleepTimerService::class.java).setAction(TimerActions.ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val pauseResumeAction = if (paused) TimerActions.ACTION_RESUME else TimerActions.ACTION_PAUSE
        val pauseResumeIntent = PendingIntent.getService(
            this, 2,
            Intent(this, SleepTimerService::class.java).setAction(pauseResumeAction),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val pauseLabel = getString(if (paused) R.string.timer_resume else R.string.timer_pause)

        return NotificationCompat.Builder(this, ZeddiHubTvApp.CH_TIMER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .addAction(0, pauseLabel, pauseResumeIntent)
            .addAction(0, getString(R.string.timer_stop), stopIntent)
            .build()
    }

    override fun onDestroy() {
        scope.cancel()
        releaseWakeLock()
        overlayManager.hide()
        super.onDestroy()
    }

    companion object {
        private const val NOTIF_ID = 0xC4F3
    }
}

internal fun formatRemaining(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
