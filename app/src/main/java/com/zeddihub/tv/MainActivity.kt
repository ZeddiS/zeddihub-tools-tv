package com.zeddihub.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.tv.material3.Surface
import com.zeddihub.tv.alerts.AlertsPoller
import com.zeddihub.tv.data.prefs.AppPrefs
import com.zeddihub.tv.data.update.UpdateChecker
import com.zeddihub.tv.nav.AppScaffold
import com.zeddihub.tv.setup.SetupWizard
import com.zeddihub.tv.timer.smartsleep.SmartSleepNudgeWatcher
import com.zeddihub.tv.ui.theme.ZeddiHubTvTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var updateChecker: UpdateChecker
    @Inject lateinit var alertsPoller: AlertsPoller
    @Inject lateinit var smartSleepNudge: SmartSleepNudgeWatcher
    @Inject lateinit var prefs: AppPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Start polling for server alerts in the background. The poller
        // is a @Singleton so calling start() twice is a no-op — safe across
        // configuration changes.
        alertsPoller.start()
        // SmartSleepNudgeWatcher bridges the idle detector → alert overlay,
        // so the user gets the same banner UX as for server-down alerts.
        smartSleepNudge.start()
        setContent {
            ZeddiHubTvTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val wizardDone by prefs.wizardCompleted.collectAsState(initial = true)
                    // Tracks user "I'm done" press in addition to the persisted
                    // flag so the transition is instant (no flicker waiting on
                    // the DataStore write to round-trip).
                    var localDone by remember { mutableStateOf(false) }
                    if (wizardDone || localDone) {
                        AppScaffold()
                    } else {
                        SetupWizard(onFinished = { localDone = true })
                    }
                }
                LaunchedEffect(Unit) {
                    updateChecker.checkOnStartup(this@MainActivity)
                }
            }
        }
    }
}
