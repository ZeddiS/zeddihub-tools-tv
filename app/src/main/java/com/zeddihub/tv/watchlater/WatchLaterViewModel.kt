package com.zeddihub.tv.watchlater

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.zeddihub.tv.data.prefs.AppPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Inject

@HiltViewModel
class WatchLaterViewModel @Inject constructor(
    private val retrofit: Retrofit,
    private val prefs: AppPrefs,
    moshi: Moshi,
) : ViewModel() {

    private val api = retrofit.create<WatchLaterApi>()
    private val cacheAdapter = moshi.adapter<List<WatchLaterItem>>(
        Types.newParameterizedType(List::class.java, WatchLaterItem::class.java)
    )

    private val _items = MutableStateFlow<List<WatchLaterItem>>(emptyList())
    val items: StateFlow<List<WatchLaterItem>> = _items.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Hydrate from cache so the UI isn't empty on cold start.
        viewModelScope.launch {
            val cached = runCatching { cacheAdapter.fromJson(prefs.watchLaterJson.first()) }
                .getOrNull().orEmpty()
            if (cached.isNotEmpty()) _items.value = cached
        }
    }

    fun refresh() = viewModelScope.launch {
        _loading.value = true; _error.value = null
        runCatching { api.list().items }
            .onSuccess { fresh ->
                _items.value = fresh
                prefs.setWatchLaterJson(cacheAdapter.toJson(fresh))
            }
            .onFailure { e ->
                _error.value = "Backend nedostupný (${e.message ?: "?"}). Zobrazuji lokální cache."
            }
        _loading.value = false
    }

    fun markWatched(id: String) = viewModelScope.launch {
        _items.value = _items.value.map { if (it.id == id) it.copy(watched = true) else it }
        runCatching { api.markWatched(id) }
    }
}
