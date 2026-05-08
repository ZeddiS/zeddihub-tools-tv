package com.zeddihub.tv.timer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val appCtx: Context,
    private val state: TimerState,
    private val overlayManager: TimerOverlayManager,
) : ViewModel() {

    val snapshot: StateFlow<TimerSnapshot> = state.state

    private val _canDrawOverlays = MutableStateFlow(overlayManager.canDrawOverlays())
    val canDrawOverlays: StateFlow<Boolean> = _canDrawOverlays.asStateFlow()

    private val _a11yEnabled = MutableStateFlow(TimerAccessibilityService.isEnabled())
    val a11yEnabled: StateFlow<Boolean> = _a11yEnabled.asStateFlow()

    fun refreshPermissions() {
        _canDrawOverlays.value = overlayManager.canDrawOverlays()
        _a11yEnabled.value = TimerAccessibilityService.isEnabled()
    }

    fun start(ctx: Context, durationMs: Long) {
        refreshPermissions()
        TimerActions.start(ctx, durationMs)
    }
    fun pause(ctx: Context) = TimerActions.send(ctx, TimerActions.ACTION_PAUSE)
    fun resume(ctx: Context) = TimerActions.send(ctx, TimerActions.ACTION_RESUME)
    fun stop(ctx: Context) = TimerActions.send(ctx, TimerActions.ACTION_STOP)
    fun shutdownNow(ctx: Context) = TimerActions.send(ctx, TimerActions.ACTION_SHUTDOWN_NOW)
}
