package com.zeddihub.tv.timer.schedule

import com.squareup.moshi.JsonClass

/**
 * A recurring sleep-timer schedule. Triggers SleepTimerService at the
 * specified local time on the given days-of-week.
 *
 * `daysOfWeek` is a bitmask: bit 0 = Monday … bit 6 = Sunday (matching
 * java.time.DayOfWeek.value - 1, so Mon=0..Sun=6). 0x7F = every day.
 */
@JsonClass(generateAdapter = true)
data class Schedule(
    val id: String,
    val name: String,
    val hour: Int,            // 0..23
    val minute: Int,          // 0..59
    val durationMinutes: Int, // sleep timer duration in minutes
    val daysOfWeek: Int,      // bitmask 0..0x7F (Mon-Sun)
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
