package com.zeddihub.tv.timer

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.zeddihub.tv.data.prefs.AppPrefs
import com.zeddihub.tv.timer.smartsleep.SmartSleepDetector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AccessibilityService — receives global key events even outside our app,
 * so the user can long-press the configured trigger key while watching
 * Netflix and pop up the timer's quick-actions overlay.
 *
 * Long-press window: 800 ms.
 *
 * Also exposes a static helper to dispatch GLOBAL_ACTION_LOCK_SCREEN
 * (API 28+) for the "shutdown" action — locks the device, on Android TV
 * boxes this typically puts the box (and the connected TV via HDMI-CEC)
 * into standby.
 */
@AndroidEntryPoint
class TimerAccessibilityService : AccessibilityService() {

    @Inject lateinit var prefs: AppPrefs
    @Inject lateinit var timerState: TimerState
    @Inject lateinit var smartSleep: SmartSleepDetector

    private val handler = Handler(Looper.getMainLooper())
    private var keyDownAt: Long = 0L
    private var triggerKey: Int = KeyEvent.KEYCODE_DPAD_CENTER
    private var pendingLongPress: Runnable? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        CoroutineScope(Dispatchers.Default).launch {
            triggerKey = runCatching { prefs.timerTriggerKey.first() }.getOrDefault(KeyEvent.KEYCODE_DPAD_CENTER)
        }
        // Smart-sleep detector starts watching for idleness as soon as the
        // accessibility service is connected — it's a no-op if the user
        // hasn't enabled the feature in Settings.
        smartSleep.start()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) { /* not needed */ }

    override fun onInterrupt() { /* not needed */ }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        // Every key press counts as "user is awake" — feed the smart-sleep
        // detector regardless of whether it's our trigger key.
        smartSleep.notifyInput()
        if (event.keyCode != triggerKey) return false
        // Only intercept while a timer is active — otherwise pass through
        // so we don't break OK/Back/Home behavior in other apps.
        if (!isTimerActive()) return false

        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                if (event.repeatCount == 0) {
                    keyDownAt = event.eventTime
                    pendingLongPress = Runnable {
                        // Trigger overlay
                        val i = Intent(this, SleepTimerService::class.java)
                            .setAction(TimerActions.ACTION_SHOW_QUICK_ACTIONS)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            startForegroundService(i)
                        else startService(i)
                    }
                    handler.postDelayed(pendingLongPress!!, LONG_PRESS_MS)
                }
                // Don't consume — let the system also see it (so e.g. OK still selects)
                return false
            }
            KeyEvent.ACTION_UP -> {
                pendingLongPress?.let { handler.removeCallbacks(it) }
                pendingLongPress = null
                return false
            }
        }
        return false
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    private fun isTimerActive(): Boolean {
        val s = timerState.current().status
        return s == TimerStatus.RUNNING || s == TimerStatus.PAUSED
    }

    companion object {
        private const val LONG_PRESS_MS = 800L
        @Volatile private var instance: TimerAccessibilityService? = null

        /** Dispatches GLOBAL_ACTION_LOCK_SCREEN if the service is running. */
        fun requestLockScreen(ctx: Context) {
            val svc = instance
            if (svc != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                svc.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            } else {
                // Fallback: launch home + power-off intent. Works partially on
                // some Android TV vendor builds (Xiaomi Mi Box S 3rd Gen has
                // limited power intent reach without root).
                val home = Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                runCatching { ctx.startActivity(home) }
            }
        }

        fun isEnabled(): Boolean = instance != null
    }
}
