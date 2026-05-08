package com.zeddihub.tv.timer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class TimerStatus { IDLE, RUNNING, PAUSED, EXPIRED }

data class TimerSnapshot(
    val status: TimerStatus = TimerStatus.IDLE,
    val totalMs: Long = 0L,
    val remainingMs: Long = 0L,
)

/**
 * Process-wide singleton for timer state. Updated by [SleepTimerService],
 * observed by overlay, in-app screen, and notification.
 */
@Singleton
class TimerState @Inject constructor() {
    private val _state = MutableStateFlow(TimerSnapshot())
    val state: StateFlow<TimerSnapshot> = _state.asStateFlow()

    fun update(s: TimerSnapshot) { _state.value = s }
    fun current() = _state.value
}
