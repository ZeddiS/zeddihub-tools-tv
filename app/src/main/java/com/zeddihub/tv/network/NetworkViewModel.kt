package com.zeddihub.tv.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.zeddihub.tv.data.prefs.AppPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@JsonClass(generateAdapter = true)
data class WolDevice(val name: String, val mac: String, val broadcast: String = "255.255.255.255")

data class NetworkUiState(
    val wolMessage: String? = null,
    val pinging: Boolean = false,
    val pingResults: List<PingResult> = emptyList(),
    val speedTesting: Boolean = false,
    val lastSpeed: SpeedResult? = null,
)

@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val prefs: AppPrefs,
    private val moshi: Moshi,
) : ViewModel() {
    private val _state = MutableStateFlow(NetworkUiState())
    val state: StateFlow<NetworkUiState> = _state.asStateFlow()

    private val devicesAdapter by lazy {
        moshi.adapter<List<WolDevice>>(Types.newParameterizedType(List::class.java, WolDevice::class.java))
    }

    fun wakeFirstSavedDevice() = viewModelScope.launch {
        val json = prefs.wolDevicesJson.first()
        val list = runCatching { devicesAdapter.fromJson(json) }.getOrNull().orEmpty()
        if (list.isEmpty()) {
            _state.update { copy(wolMessage = "Žádná uložená zařízení. Přidat lze v Nastavení.") }
            return@launch
        }
        val d = list.first()
        val r = WakeOnLan.send(d.mac, d.broadcast)
        _state.update { copy(wolMessage = if (r.isSuccess) "Magic packet odeslán → ${d.name}" else "Chyba: ${r.exceptionOrNull()?.message}") }
    }

    fun wakeQuick() = viewModelScope.launch {
        // Demo MAC for testing
        val r = WakeOnLan.send("00:11:22:33:44:55")
        _state.update { copy(wolMessage = if (r.isSuccess) "Test packet odeslán" else r.exceptionOrNull()?.message ?: "Chyba") }
    }

    fun runSpeedTest() = viewModelScope.launch {
        _state.update { copy(speedTesting = true) }
        val r = SpeedTest.run()
        _state.update { copy(speedTesting = false, lastSpeed = r) }
    }

    fun runPingBatch() = viewModelScope.launch {
        _state.update { copy(pinging = true, pingResults = emptyList()) }
        val targets = listOf(
            "1.1.1.1" to 53,
            "8.8.8.8" to 53,
            "google.com" to 80,
            "github.com" to 443,
            "zeddihub.eu" to 443,
        )
        val results = PingTool.batch(targets)
        _state.update { copy(pinging = false, pingResults = results) }
    }

    private fun MutableStateFlow<NetworkUiState>.update(block: NetworkUiState.() -> NetworkUiState) {
        value = value.block()
    }
}
