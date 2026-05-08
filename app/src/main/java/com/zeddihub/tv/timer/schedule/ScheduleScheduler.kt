package com.zeddihub.tv.timer.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridges [Schedule] entries to AlarmManager. For each enabled schedule,
 * we register a single exact alarm at the next matching local time —
 * when it fires, the receiver re-arms the next occurrence (so the loop
 * keeps going across days). This avoids the JobScheduler/WorkManager
 * limitation of "approximately" timing for sleep-bedtime UX where seconds
 * matter.
 */
@Singleton
class ScheduleScheduler @Inject constructor(
    @ApplicationContext private val ctx: Context,
) {
    private val am: AlarmManager =
        ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun rearm(schedules: List<Schedule>) {
        // Cancel any previously-armed alarms by their request codes (id hash).
        schedules.forEach { cancel(it.id) }
        schedules.filter { it.enabled }.forEach { arm(it) }
    }

    fun arm(s: Schedule) {
        val triggerMs = nextTriggerEpochMs(s) ?: return
        val pi = pendingIntent(s.id, s.durationMinutes)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // canScheduleExactAlarms() may return false on Android 12+;
            // fall back to inexact rather than crashing. The user is
            // prompted in Settings to grant SCHEDULE_EXACT_ALARM if they
            // want second-accurate triggering.
            val canExact = am.canScheduleExactAlarms()
            if (canExact) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
            }
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
        }
    }

    fun cancel(id: String) {
        am.cancel(pendingIntent(id, 0, mutable = false))
    }

    private fun pendingIntent(id: String, durationMinutes: Int, mutable: Boolean = false): PendingIntent {
        val i = Intent(ctx, ScheduleAlarmReceiver::class.java).apply {
            action = ACTION_FIRE
            putExtra(EXTRA_SCHEDULE_ID, id)
            putExtra(EXTRA_DURATION_MIN, durationMinutes)
        }
        val flags = (if (mutable) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_IMMUTABLE) or
                PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getBroadcast(ctx, id.hashCode(), i, flags)
    }

    /** Returns the next epoch-millis when this schedule should fire,
     *  or null if no day is selected. */
    fun nextTriggerEpochMs(s: Schedule, from: LocalDateTime = LocalDateTime.now()): Long? {
        if (s.daysOfWeek == 0) return null
        // DayOfWeek: Monday=1..Sunday=7; bit index = value-1
        for (offset in 0..6) {
            val candidate = from.plusDays(offset.toLong())
                .withHour(s.hour).withMinute(s.minute).withSecond(0).withNano(0)
            if (offset == 0 && !candidate.isAfter(from)) continue
            val bit = candidate.dayOfWeek.value - 1
            if ((s.daysOfWeek shr bit) and 1 == 1) {
                return candidate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
        }
        return null
    }

    companion object {
        const val ACTION_FIRE = "com.zeddihub.tv.SCHEDULE_FIRE"
        const val EXTRA_SCHEDULE_ID = "schedule_id"
        const val EXTRA_DURATION_MIN = "duration_min"
    }
}
