package com.zeddihub.tv.timer.wakeup

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.zeddihub.tv.data.prefs.AppPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

@Singleton
class WakeUpStore @Inject constructor(
    private val prefs: AppPrefs,
    moshi: Moshi,
) {
    private val adapter = moshi.adapter<List<WakeUp>>(
        Types.newParameterizedType(List::class.java, WakeUp::class.java)
    )

    val wakeups: Flow<List<WakeUp>> = prefs.wakeupsJson.map { json ->
        runCatching { adapter.fromJson(json) ?: emptyList() }.getOrDefault(emptyList())
    }

    suspend fun list(): List<WakeUp> =
        runCatching { adapter.fromJson(prefs.wakeupsJson.first()) ?: emptyList() }
            .getOrDefault(emptyList())

    suspend fun upsert(w: WakeUp) {
        val current = list().toMutableList()
        val idx = current.indexOfFirst { it.id == w.id }
        if (idx >= 0) current[idx] = w else current += w
        save(current)
    }

    suspend fun delete(id: String) = save(list().filter { it.id != id })

    suspend fun toggleEnabled(id: String): Boolean {
        val current = list().toMutableList()
        val idx = current.indexOfFirst { it.id == id }
        if (idx < 0) return false
        current[idx] = current[idx].copy(enabled = !current[idx].enabled)
        save(current)
        return current[idx].enabled
    }

    private suspend fun save(list: List<WakeUp>) = prefs.setWakeupsJson(adapter.toJson(list))

    companion object {
        fun newId(): String = UUID.randomUUID().toString().take(8)
    }
}
