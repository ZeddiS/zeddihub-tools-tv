package com.zeddihub.tv.alerts

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val poller: AlertsPoller,
    private val overlay: AlertOverlayManager,
) : ViewModel() {
    val latest: StateFlow<AlertJson?> = poller.latest

    fun test() {
        overlay.show(AlertJson(
            id = "test-${System.currentTimeMillis()}",
            severity = "info",
            title = "Test upozornění",
            message = "Tohle je test alert overlay. Skryje se za 10 sekund.",
            ts = System.currentTimeMillis() / 1000L,
            ttl = 10L,
            source = "test",
        ))
    }
}
