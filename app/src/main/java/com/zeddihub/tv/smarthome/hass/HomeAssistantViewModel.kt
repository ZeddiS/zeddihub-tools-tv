package com.zeddihub.tv.smarthome.hass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.zeddihub.tv.data.prefs.AppPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Owns Home Assistant config + cached entity state. The cache is in-memory
 * only — re-fetched on Refresh; HA's REST API is fast enough that we don't
 * need a persistent state store. Pinned entity IDs persist in DataStore so
 * the user's quick-toggle list survives app restarts.
 */
@HiltViewModel
class HomeAssistantViewModel @Inject constructor(
    private val prefs: AppPrefs,
    private val client: HomeAssistantClient,
    moshi: Moshi,
) : ViewModel() {

    private val pinnedAdapter = moshi.adapter<List<String>>(
        Types.newParameterizedType(List::class.java, String::class.java)
    )

    val baseUrl: StateFlow<String> = prefs.hassBaseUrl.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val token: StateFlow<String> = prefs.hassToken.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val _pinned = MutableStateFlow<List<String>>(emptyList())
    val pinnedEntities: StateFlow<List<String>> = _pinned.asStateFlow()

    private val _states = MutableStateFlow<Map<String, String>>(emptyMap())
    val statesByEntity: StateFlow<Map<String, String>> = _states.asStateFlow()

    private val _lastPing = MutableStateFlow<String?>(null)
    val lastPing: StateFlow<String?> = _lastPing.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            _pinned.value = loadPinned()
        }
    }

    fun useDefault() = viewModelScope.launch {
        val current = prefs.hassBaseUrl.first()
        val tok = prefs.hassToken.first()
        prefs.setHass(current.ifBlank { "http://homeassistant.local:8123" }, tok)
        _message.value = "Default LAN URL nastavena. Vlož token a ping."
    }

    fun ping() = viewModelScope.launch {
        val cfg = HassConfig(prefs.hassBaseUrl.first(), prefs.hassToken.first())
        val r = client.ping(cfg)
        _lastPing.value = r.fold(
            onSuccess = { "✓ ${if (it.length > 60) it.take(60) + "…" else it}" },
            onFailure = { "× ${it.message}" }
        )
    }

    fun refreshStates() = viewModelScope.launch {
        val cfg = HassConfig(prefs.hassBaseUrl.first(), prefs.hassToken.first())
        val r = client.states(cfg)
        r.onSuccess { list ->
            _states.value = list.associate { it.entity_id to it.state }
            _message.value = "Načteno ${list.size} entit"
        }.onFailure {
            _message.value = "Refresh selhal: ${it.message}"
        }
    }

    fun pin(entityId: String) = viewModelScope.launch {
        val now = (loadPinned() + entityId).distinct()
        savePinned(now); _pinned.value = now
    }

    fun unpin(entityId: String) = viewModelScope.launch {
        val now = loadPinned().filter { it != entityId }
        savePinned(now); _pinned.value = now
    }

    fun toggle(entityId: String) = viewModelScope.launch {
        val cfg = HassConfig(prefs.hassBaseUrl.first(), prefs.hassToken.first())
        val cur = _states.value[entityId]?.lowercase() ?: ""
        // Best-effort: we don't know each entity's domain semantics, but
        // most actionable HA entities (light/switch/input_boolean/fan/etc.)
        // share the toggle service under their own domain.
        val domain = entityId.substringBefore('.', missingDelimiterValue = "homeassistant")
        val service = if (cur == "on" || cur == "playing" || cur == "open") "turn_off" else "turn_on"
        client.callService(cfg, domain, service, mapOf("entity_id" to entityId)).onSuccess {
            _message.value = "$entityId → $service"
            // Optimistic UI update
            _states.value = _states.value.toMutableMap().apply {
                put(entityId, if (service == "turn_on") "on" else "off")
            }
        }.onFailure {
            _message.value = "Toggle selhal: ${it.message}"
        }
    }

    fun runScript(scriptName: String) = viewModelScope.launch {
        val cfg = HassConfig(prefs.hassBaseUrl.first(), prefs.hassToken.first())
        client.runScript(cfg, scriptName).onSuccess {
            _message.value = "✓ script.$scriptName spuštěn"
        }.onFailure {
            _message.value = "Script selhal: ${it.message}"
        }
    }

    fun activateScene(sceneEntityId: String) = viewModelScope.launch {
        val cfg = HassConfig(prefs.hassBaseUrl.first(), prefs.hassToken.first())
        client.activateScene(cfg, sceneEntityId).onSuccess {
            _message.value = "✓ $sceneEntityId aktivována"
        }.onFailure {
            _message.value = "Scene selhala: ${it.message}"
        }
    }

    private suspend fun loadPinned(): List<String> =
        runCatching { pinnedAdapter.fromJson(prefs.hassPinnedJson.first()) ?: emptyList() }
            .getOrDefault(emptyList())

    private suspend fun savePinned(list: List<String>) {
        prefs.setHassPinnedJson(pinnedAdapter.toJson(list))
    }
}
