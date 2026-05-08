package com.zeddihub.tv.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.JsonClass
import com.zeddihub.tv.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import javax.inject.Inject

data class ServerStatus(
    val name: String,
    val address: String,
    val online: Boolean,
    val players: Int,
    val maxPlayers: Int,
)

data class ServersUiState(
    val loading: Boolean = false,
    val servers: List<ServerStatus> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class ServersViewModel @Inject constructor(
    private val retrofit: Retrofit,
) : ViewModel() {

    private val _state = MutableStateFlow(ServersUiState())
    val state: StateFlow<ServersUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        runCatching {
            val api = retrofit.create<ServersApi>()
            api.list().servers
        }.onSuccess { list ->
            _state.value = ServersUiState(loading = false, servers = list.map {
                ServerStatus(it.name, "${it.host}:${it.port}", it.online, it.players, it.max_players)
            })
        }.onFailure { e ->
            _state.value = ServersUiState(loading = false, error = "Backend nedostupný (${e.message ?: "?"}).")
        }
    }
}

interface ServersApi {
    @GET("servers.php?format=json")
    suspend fun list(): ServersResp
}

@JsonClass(generateAdapter = true)
data class ServersResp(val servers: List<ServerJson> = emptyList())

@JsonClass(generateAdapter = true)
data class ServerJson(
    val name: String,
    val host: String,
    val port: Int,
    val online: Boolean = false,
    val players: Int = 0,
    val max_players: Int = 0,
)
