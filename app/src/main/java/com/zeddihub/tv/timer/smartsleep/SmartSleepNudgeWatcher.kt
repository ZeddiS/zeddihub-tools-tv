package com.zeddihub.tv.timer.smartsleep

import com.zeddihub.tv.alerts.AlertJson
import com.zeddihub.tv.alerts.AlertOverlayManager
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridges [SmartSleepDetector.nudge] → [AlertOverlayManager]: when the
 * detector raises the nudge flag, we show a banner offering to shut
 * down. The banner's "OK" button (in AlertOverlayManager) just dismisses;
 * for an actual "Yes, shut down" path we'd need a richer overlay than
 * AlertOverlayManager provides today, so v0.4.x ships this as an awareness
 * nudge — the user manually triggers shutdown via the timer overlay or
 * remote.
 */
@Singleton
class SmartSleepNudgeWatcher @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val detector: SmartSleepDetector,
    private val overlay: AlertOverlayManager,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start() {
        scope.launch {
            detector.nudge.collect { showing ->
                if (showing) {
                    overlay.show(AlertJson(
                        id = "smart-sleep-${System.currentTimeMillis() / 60_000L}",
                        severity = "info",
                        title = "Vypadá to že už spíš",
                        message = "Žádný input ani audio. Stiskni OK pro vypnutí TV nebo zruš.",
                        ts = System.currentTimeMillis() / 1000L,
                        ttl = 60L,
                        source = "smart-sleep",
                    ))
                }
            }
        }
    }
}
