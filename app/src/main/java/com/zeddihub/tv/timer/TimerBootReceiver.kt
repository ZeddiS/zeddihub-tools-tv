package com.zeddihub.tv.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * On boot we don't auto-restart the timer (could surprise the user with a
 * sudden countdown after reboot). Instead we just no-op — the receiver
 * exists so the manifest declaration is valid and so we have a hook for
 * future "remember last timer" feature.
 */
class TimerBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) { /* no-op for now */ }
}
