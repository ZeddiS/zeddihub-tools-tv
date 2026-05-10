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
import com.zeddihub.tv.data.config.TvConfigRepository
import com.zeddihub.tv.data.prefs.AppPrefs
import com.zeddihub.tv.data.telemetry.TelemetryRecorder
import com.zeddihub.tv.data.update.StartupUpdateDialog
import com.zeddihub.tv.data.update.UpdateCheckResult
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
    @Inject lateinit var telemetry: TelemetryRecorder
    @Inject lateinit var tvConfig: TvConfigRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Singletons; calling start() twice is a no-op safe across rotation.
        alertsPoller.start()
        smartSleepNudge.start()
        // Pull /api/tv-config.php in the background so streaming app order +
        // bookmarks reflect any admin edits without an app rebuild.
        tvConfig.start()

        // Cold start only — savedInstanceState != null means we restored from
        // a config change (e.g. rotation), not a fresh launch. We don't want
        // to fire a launch event or update prompt on every rotation.
        if (savedInstanceState == null) {
            telemetry.recordLaunch()
        }

        setContent {
            ZeddiHubTvTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // v0.1.11 — initial=false so we always show wizard first
                    // on cold start and only flip to AppScaffold once DataStore
                    // confirms the flag is set. Previous initial=true caused
                    // a brief flash of AppScaffold for first-run users before
                    // the wizard appeared.
                    //
                    // localDone is in-process only (lost on process death) —
                    // this is intentional. If user clicks "Přeskočit", the
                    // wizard hides FOR THIS SESSION but reshows on next cold
                    // start until they click "Dokončit" at the end.
                    val wizardDone by prefs.wizardCompleted.collectAsState(initial = false)
                    var localDone by remember { mutableStateOf(false) }

                    if (wizardDone || localDone) {
                        AppScaffold()
                    } else {
                        SetupWizard(onFinished = { localDone = true })
                    }

                    // Update prompt — driven by a state that's only populated
                    // on cold start. The dialog is composed at the very top
                    // of the tree so it overlays both wizard and main scaffold.
                    var pending by remember { mutableStateOf<UpdateCheckResult?>(null) }
                    var checkedThisLaunch by remember { mutableStateOf(false) }

                    LaunchedEffect(savedInstanceState) {
                        if (savedInstanceState != null || checkedThisLaunch) return@LaunchedEffect
                        checkedThisLaunch = true
                        // Small delay so wizard / nav can render first; the
                        // dialog appears on top once the network call resolves.
                        kotlinx.coroutines.delay(2_500)
                        val r = updateChecker.checkNow()
                        if (r != null && r.isUpdateAvailable) {
                            pending = r
                        }
                    }

                    pending?.let { r ->
                        StartupUpdateDialog(
                            result = r,
                            onInstall = {
                                updateChecker.startInstall(this@MainActivity, r)
                                pending = null
                            },
                            onDismiss = { pending = null },
                        )
                    }
                }
            }
        }
    }
}
