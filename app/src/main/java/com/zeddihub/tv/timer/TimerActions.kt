package com.zeddihub.tv.timer

import android.content.Context
import android.content.Intent
import android.os.Build

object TimerActions {
    const val ACTION_START = "com.zeddihub.tv.timer.START"
    const val ACTION_PAUSE = "com.zeddihub.tv.timer.PAUSE"
    const val ACTION_RESUME = "com.zeddihub.tv.timer.RESUME"
    const val ACTION_STOP = "com.zeddihub.tv.timer.STOP"
    const val ACTION_SHUTDOWN_NOW = "com.zeddihub.tv.timer.SHUTDOWN_NOW"
    const val ACTION_SHOW_QUICK_ACTIONS = "com.zeddihub.tv.timer.QUICK_ACTIONS"
    const val ACTION_HIDE_QUICK_ACTIONS = "com.zeddihub.tv.timer.HIDE_QUICK_ACTIONS"

    const val EXTRA_DURATION_MS = "duration_ms"

    fun start(context: Context, durationMs: Long) {
        val i = Intent(context, SleepTimerService::class.java).apply {
            action = ACTION_START
            putExtra(EXTRA_DURATION_MS, durationMs)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(i)
        else context.startService(i)
    }

    fun send(context: Context, action: String) {
        val i = Intent(context, SleepTimerService::class.java).apply { this.action = action }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(i)
        else context.startService(i)
    }
}
