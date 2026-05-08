package com.zeddihub.tv.timer.wakeup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.PowerManager
import com.zeddihub.tv.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WakeUpAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var store: WakeUpStore
    @Inject lateinit var scheduler: WakeUpScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != WakeUpScheduler.ACTION_FIRE) return
        val id = intent.getStringExtra(WakeUpScheduler.EXTRA_ID) ?: return
        val launchPkg = intent.getStringExtra(WakeUpScheduler.EXTRA_LAUNCH_PKG)
        val volumePct = intent.getIntExtra(WakeUpScheduler.EXTRA_VOLUME_PCT, 50)

        // 1) Wake the screen
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        val wl = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "ZeddiHubTV:WakeUp"
        )
        wl.acquire(60_000L)

        // 2) Set music volume
        runCatching {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxV = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val target = (maxV * volumePct.coerceIn(0, 100) / 100)
            am.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
        }

        // 3) Launch target app, falling back to our MainActivity if unset / not installed
        val intentToLaunch = if (!launchPkg.isNullOrBlank()) {
            context.packageManager.getLaunchIntentForPackage(launchPkg)
        } else null
        val final = (intentToLaunch ?: Intent(context, MainActivity::class.java))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        runCatching { context.startActivity(final) }

        // 4) Re-arm next occurrence
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val w = store.list().firstOrNull { it.id == id && it.enabled }
                if (w != null) scheduler.arm(w)
            } finally {
                pending.finish()
                if (wl.isHeld) runCatching { wl.release() }
            }
        }
    }
}
