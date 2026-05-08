package com.zeddihub.tv.parental

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParentalViewModel @Inject constructor(
    private val store: ParentalStore,
) : ViewModel() {

    val rules: StateFlow<List<ParentalRule>> =
        store.rules.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val pinIsSet: StateFlow<Boolean> = store.rules.map {
        // The store doesn't expose a flow for the PIN by itself; we cheaply
        // re-check on each rules emission since the user typically changes
        // both around the same time.
        store.pinIsSet()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun setPin(pin: String) = viewModelScope.launch {
        store.setPin(pin)
        _message.value = if (pin.isBlank()) "PIN zrušen." else "PIN uložen."
    }

    fun addRule(pkg: String) = viewModelScope.launch {
        store.upsert(ParentalRule(packageName = pkg, pinRequired = true))
        _message.value = "Pravidlo pro $pkg přidáno (PIN required)."
    }

    fun togglePinRequired(rule: ParentalRule) = viewModelScope.launch {
        store.upsert(rule.copy(pinRequired = !rule.pinRequired))
    }

    fun setBedtime(rule: ParentalRule, startMin: Int, endMin: Int) = viewModelScope.launch {
        store.upsert(rule.copy(bedtimeStartMinute = startMin, bedtimeEndMinute = endMin))
        _message.value = "Bedtime ${rule.packageName}: ${formatMin(startMin)}–${formatMin(endMin)}"
    }

    fun removeRule(pkg: String) = viewModelScope.launch {
        store.remove(pkg)
        _message.value = "$pkg odstraněno."
    }

    private fun formatMin(m: Int): String =
        "%02d:%02d".format((m / 60) % 24, m % 60)
}
