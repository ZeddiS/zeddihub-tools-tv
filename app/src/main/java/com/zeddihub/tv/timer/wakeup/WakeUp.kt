package com.zeddihub.tv.timer.wakeup

import com.squareup.moshi.JsonClass

/**
 * A scheduled wake-up — at the configured time on selected weekdays
 * the TV turns on (acquires a partial wake lock + brings MainActivity
 * to foreground) and optionally launches a target app package
 * (e.g. YouTube for morning news, Spotify for music alarm).
 *
 * Same daysOfWeek bitmask convention as [com.zeddihub.tv.timer.schedule.Schedule].
 */
@JsonClass(generateAdapter = true)
data class WakeUp(
    val id: String,
    val name: String,
    val hour: Int,
    val minute: Int,
    val daysOfWeek: Int,
    val launchPackage: String? = null,   // package to launch on wake
    val volumePct: Int = 50,             // STREAM_MUSIC volume to set on wake (0..100)
    val enabled: Boolean = true,
) {
    fun timeStr(): String = "%02d:%02d".format(hour, minute)
    fun daysText(): String {
        if (daysOfWeek == 0x7F) return "Každý den"
        if (daysOfWeek == 0x1F) return "Po-Pá"
        if (daysOfWeek == 0x60) return "So-Ne"
        val labels = listOf("Po","Út","St","Čt","Pá","So","Ne")
        return labels.filterIndexed { i, _ -> (daysOfWeek shr i) and 1 == 1 }.joinToString(", ")
    }
}
