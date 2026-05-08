package com.zeddihub.tv.timer.schedule

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
class ScheduleStore @Inject constructor(
    private val prefs: AppPrefs,
    moshi: Moshi,
) {
    private val adapter = moshi.adapter<List<Schedule>>(
        Types.newParameterizedType(List::class.java, Schedule::class.java)
    )

    val schedules: Flow<List<Schedule>> = prefs.schedulesJson.map { json ->
        runCatching { adapter.fromJson(json) ?: emptyList() }.getOrDefault(emptyList())
    }

    suspend fun list(): List<Schedule> =
        runCatching { adapter.fromJson(prefs.schedulesJson.first()) ?: emptyList() }
            .getOrDefault(emptyList())

    suspend fun upsert(s: Schedule) {
        val current = list().toMutableList()
        val idx = current.indexOfFirst { it.id == s.id }
        if (idx >= 0) current[idx] = s else current += s
        save(current)
    }

    suspend fun delete(id: String) {
        save(list().filter { it.id != id })
    }

    suspend fun toggleEnabled(id: String): Boolean {
        val current = list().toMutableList()
        val idx = current.indexOfFirst { it.id == id }
        if (idx < 0) return false
        current[idx] = current[idx].copy(enabled = !current[idx].enabled)
        save(current)
        return current[idx].enabled
    }

    private suspend fun save(list: List<Schedule>) {
        prefs.setSchedulesJson(adapter.toJson(list))
    }

    companion object {
        fun newId(): String = UUID.randomUUID().toString().take(8)
    }
}
