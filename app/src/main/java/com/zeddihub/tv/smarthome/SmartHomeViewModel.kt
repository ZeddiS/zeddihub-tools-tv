package com.zeddihub.tv.smarthome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmartHomeViewModel @Inject constructor(
    private val controller: SmartHomeController,
) : ViewModel() {
    val devices: StateFlow<List<SmartDevice>> =
        controller.devices.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _lastMessage = MutableStateFlow<String?>(null)
    val lastMessage: StateFlow<String?> = _lastMessage.asStateFlow()

    fun setPower(d: SmartDevice, on: Boolean) = viewModelScope.launch {
        val r = controller.setPower(d, on)
        _lastMessage.value = if (r.isSuccess) "${d.name} → ${if (on) "ON" else "OFF"}"
        else "Chyba ${d.name}: ${r.exceptionOrNull()?.message ?: "unknown"}"
    }

    fun upsert(d: SmartDevice) = viewModelScope.launch { controller.upsert(d) }
    fun delete(id: String) = viewModelScope.launch { controller.delete(id) }
}
