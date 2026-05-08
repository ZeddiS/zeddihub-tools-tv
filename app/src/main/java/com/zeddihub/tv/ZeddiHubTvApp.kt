package com.zeddihub.tv

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ZeddiHubTvApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NotificationManager::class.java) ?: return

        nm.createNotificationChannel(
            NotificationChannel(
                CH_TIMER,
                getString(R.string.timer_notif_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Sleep Timer countdown"
                setShowBadge(false)
            }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CH_UPDATES,
                "Aktualizace aplikace",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CH_ALERTS,
                "Upozornění serverů",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    companion object {
        const val CH_TIMER = "timer"
        const val CH_UPDATES = "updates"
        const val CH_ALERTS = "alerts"
    }
}
