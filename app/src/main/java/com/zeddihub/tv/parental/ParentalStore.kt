package com.zeddihub.tv.parental

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.zeddihub.tv.data.prefs.AppPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParentalStore @Inject constructor(
    private val prefs: AppPrefs,
    moshi: Moshi,
) {
    private val adapter = moshi.adapter<List<ParentalRule>>(
        Types.newParameterizedType(List::class.java, ParentalRule::class.java)
    )

    val rules: Flow<List<ParentalRule>> = prefs.parentalQuotasJson.map {
        runCatching { adapter.fromJson(it) ?: emptyList() }.getOrDefault(emptyList())
    }

    suspend fun list(): List<ParentalRule> =
        runCatching { adapter.fromJson(prefs.parentalQuotasJson.first()) ?: emptyList() }
            .getOrDefault(emptyList())

    suspend fun upsert(rule: ParentalRule) {
        val cur = list().toMutableList()
        val idx = cur.indexOfFirst { it.packageName == rule.packageName }
        if (idx >= 0) cur[idx] = rule else cur += rule
        save(cur)
    }

    suspend fun remove(packageName: String) {
        save(list().filter { it.packageName != packageName })
    }

    suspend fun ruleFor(packageName: String): ParentalRule? =
        list().firstOrNull { it.packageName == packageName }

    private suspend fun save(list: List<ParentalRule>) {
        prefs.setParentalQuotasJson(adapter.toJson(list))
    }

    /** PIN handling — stored as plain text in DataStore; not strong-secret material. */
    suspend fun pin(): String = prefs.parentalPin.first()
    suspend fun setPin(pin: String) { prefs.setParentalPin(pin) }
    suspend fun pinIsSet(): Boolean = prefs.parentalPin.first().isNotBlank()
    suspend fun verifyPin(input: String): Boolean = prefs.parentalPin.first() == input
}
