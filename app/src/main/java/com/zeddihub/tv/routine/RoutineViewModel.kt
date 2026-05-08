package com.zeddihub.tv.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val runner: RoutineRunner,
) : ViewModel() {
    private val _steps = MutableStateFlow<List<RoutineStep>>(emptyList())
    val steps: StateFlow<List<RoutineStep>> = _steps.asStateFlow()

    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running.asStateFlow()

    fun load() = viewModelScope.launch {
        _steps.value = runner.loadBedtime()
    }

    fun run() = viewModelScope.launch {
        _running.value = true
        runner.runBedtime()
        // Re-enable button after estimated runtime; we don't track real
        // completion since runner is fire-and-forget by design.
        delay(2_000)
        _running.value = false
    }

    fun resetToDefault() = update(defaultBedtimeRoutine())
    fun addVolumeFade() = update(_steps.value + RoutineStep(kind = RoutineKinds.VOLUME_FADE, durationSeconds = 30, targetVolumePct = 30))
    fun addStartTimer() = update(_steps.value + RoutineStep(kind = RoutineKinds.START_TIMER, timerMinutes = 30))
    fun addDelay() = update(_steps.value + RoutineStep(kind = RoutineKinds.DELAY, durationSeconds = 5))
    fun addWebhook() = update(_steps.value + RoutineStep(kind = RoutineKinds.WEBHOOK, webhookUrl = "", webhookMethod = "GET"))

    private fun update(newSteps: List<RoutineStep>) = viewModelScope.launch {
        _steps.value = newSteps
        runner.saveBedtime(newSteps)
    }
}
