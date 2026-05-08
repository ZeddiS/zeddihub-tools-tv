package com.zeddihub.tv.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zeddihub.tv.timer.schedule.ScheduleScheduler
import com.zeddihub.tv.timer.schedule.ScheduleStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * On reboot, the AlarmManager forgets all our scheduled alarms — so this
 * receiver re-arms each enabled schedule. We do NOT auto-restart any
 * previously-running countdown (could surprise the user with a sudden
 * shutdown after reboot).
 */
@AndroidEntryPoint
class TimerBootReceiver : BroadcastReceiver() {

    @Inject lateinit var store: ScheduleStore
    @Inject lateinit var scheduler: ScheduleScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                scheduler.rearm(store.list())
            } finally {
                pending.finish()
            }
        }
    }
}
