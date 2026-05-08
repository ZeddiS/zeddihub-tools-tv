package com.zeddihub.tv.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val HISTORY_MAX = 60   // ~5 minutes at 5s sample interval
private const val SAMPLE_INTERVAL_MS = 5_000L

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val sampler: HealthSampler,
) : ViewModel() {
    private val _current = MutableStateFlow<HealthSample?>(null)
    val current: StateFlow<HealthSample?> = _current.asStateFlow()

    private val _tempHistory = MutableStateFlow<List<Float>>(emptyList())
    val tempHistory: StateFlow<List<Float>> = _tempHistory.asStateFlow()

    init {
        viewModelScope.launch {
            // Prime once for instantaneous CPU load (needs 2 samples to compute delta)
            sampler.sample()
            while (true) {
                val s = sampler.sample()
                _current.value = s
                s.cpuTempC?.let { temp ->
                    _tempHistory.value = (_tempHistory.value + temp).takeLast(HISTORY_MAX)
                }
                delay(SAMPLE_INTERVAL_MS)
            }
        }
    }
}
