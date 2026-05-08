package com.zeddihub.tv.timer.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zeddihub.tv.timer.TimerActions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fires when an exact AlarmManager alarm hits the schedule trigger time.
 * Starts the SleepTimerService with the configured duration, then re-arms
 * the next occurrence of this same schedule so the cycle continues.
 *
 * goAsync() keeps the receiver alive long enough to finish the rearm
 * (DataStore reads are async).
 */
@AndroidEntryPoint
class ScheduleAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var store: ScheduleStore
    @Inject lateinit var scheduler: ScheduleScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ScheduleScheduler.ACTION_FIRE) return
        val id = intent.getStringExtra(ScheduleScheduler.EXTRA_SCHEDULE_ID) ?: return
        val durationMin = intent.getIntExtra(ScheduleScheduler.EXTRA_DURATION_MIN, 30)

        // Start the sleep timer with the scheduled duration
        TimerActions.start(context.applicationContext, durationMin * 60_000L)

        // Re-arm the next occurrence (suspending DataStore read)
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val s = store.list().firstOrNull { it.id == id && it.enabled }
                if (s != null) scheduler.arm(s)
            } finally {
                pending.finish()
            }
        }
    }
}
