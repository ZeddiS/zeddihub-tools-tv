package com.zeddihub.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.tv.material3.Surface
import com.zeddihub.tv.alerts.AlertsPoller
import com.zeddihub.tv.data.update.UpdateChecker
import com.zeddihub.tv.nav.AppScaffold
import com.zeddihub.tv.ui.theme.ZeddiHubTvTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var updateChecker: UpdateChecker
    @Inject lateinit var alertsPoller: AlertsPoller

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Start polling for server alerts in the background. The poller
        // is a @Singleton so calling start() twice is a no-op — safe across
        // configuration changes.
        alertsPoller.start()
        setContent {
            ZeddiHubTvTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppScaffold()
                }
                LaunchedEffect(Unit) {
                    updateChecker.checkOnStartup(this@MainActivity)
                }
            }
        }
    }
}
