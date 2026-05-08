package com.zeddihub.tv.timer.wakeup

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

@Singleton
class WakeUpScheduler @Inject constructor(
    @ApplicationContext private val ctx: Context,
) {
    private val am: AlarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun rearm(list: List<WakeUp>) {
        list.forEach { cancel(it.id) }
        list.filter { it.enabled }.forEach { arm(it) }
    }

    fun arm(w: WakeUp) {
        val triggerMs = nextTriggerEpochMs(w) ?: return
        val pi = pendingIntent(w.id, w.launchPackage, w.volumePct)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
        }
    }

    fun cancel(id: String) {
        am.cancel(pendingIntent(id, null, 0, mutable = false))
    }

    private fun pendingIntent(id: String, launchPackage: String?, volumePct: Int, mutable: Boolean = false): PendingIntent {
        val i = Intent(ctx, WakeUpAlarmReceiver::class.java).apply {
            action = ACTION_FIRE
            putExtra(EXTRA_ID, id)
            putExtra(EXTRA_LAUNCH_PKG, launchPackage)
            putExtra(EXTRA_VOLUME_PCT, volumePct)
        }
        val flags = (if (mutable) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_IMMUTABLE) or
                PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getBroadcast(ctx, ("wake-" + id).hashCode(), i, flags)
    }

    fun nextTriggerEpochMs(w: WakeUp, from: LocalDateTime = LocalDateTime.now()): Long? {
        if (w.daysOfWeek == 0) return null
        for (offset in 0..6) {
            val candidate = from.plusDays(offset.toLong())
                .withHour(w.hour).withMinute(w.minute).withSecond(0).withNano(0)
            if (offset == 0 && !candidate.isAfter(from)) continue
            val bit = candidate.dayOfWeek.value - 1
            if ((w.daysOfWeek shr bit) and 1 == 1) {
                return candidate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
        }
        return null
    }

    companion object {
        const val ACTION_FIRE = "com.zeddihub.tv.WAKEUP_FIRE"
        const val EXTRA_ID = "id"
        const val EXTRA_LAUNCH_PKG = "launch_pkg"
        const val EXTRA_VOLUME_PCT = "volume_pct"
    }
}
