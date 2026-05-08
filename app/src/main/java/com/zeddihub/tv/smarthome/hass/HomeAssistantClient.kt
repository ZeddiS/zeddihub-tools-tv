package com.zeddihub.tv.smarthome.hass

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@JsonClass(generateAdapter = true)
data class HassEntity(
    val entity_id: String,
    val state: String = "",
    val attributes: Map<String, Any?> = emptyMap(),
)

@JsonClass(generateAdapter = true)
data class HassServiceDomain(
    val domain: String,
    val services: Map<String, Any?> = emptyMap(),
)

data class HassConfig(val baseUrl: String, val token: String) {
    fun isValid(): Boolean = baseUrl.isNotBlank() && token.isNotBlank()
}

/**
 * Thin Home Assistant REST client. We deliberately don't use Retrofit here:
 * HA's payloads are deeply dynamic (services & attributes are typed per
 * integration), Moshi codegen with Map<String, Any?> works, and a hand-rolled
 * OkHttp client lets us swap baseUrl + token at runtime without rebuilding
 * Retrofit instances.
 *
 * Auth: long-lived access token from HA Profile → Long-lived access tokens.
 * baseUrl: typically http://homeassistant.local:8123 (LAN) or
 *          https://your-instance.duckdns.org (Nabu Casa / port-forward).
 */
@Singleton
class HomeAssistantClient @Inject constructor(
    private val client: OkHttpClient,
    moshi: Moshi,
) {
    private val entityListAdapter = moshi.adapter<List<HassEntity>>(
        Types.newParameterizedType(List::class.java, HassEntity::class.java)
    )
    private val serviceListAdapter = moshi.adapter<List<HassServiceDomain>>(
        Types.newParameterizedType(List::class.java, HassServiceDomain::class.java)
    )
    private val mapAdapter = moshi.adapter<Map<String, Any?>>(
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    )

    suspend fun ping(cfg: HassConfig): Result<String> = withContext(Dispatchers.IO) {
        if (!cfg.isValid()) return@withContext Result.failure(IllegalArgumentException("HA URL or token missing"))
        runCatching {
            val resp = client.newCall(authedReq(cfg, "/api/").build()).execute()
            if (!resp.isSuccessful) error("HTTP ${resp.code}")
            resp.body?.string().orEmpty()
        }
    }

    suspend fun states(cfg: HassConfig): Result<List<HassEntity>> = withContext(Dispatchers.IO) {
        if (!cfg.isValid()) return@withContext Result.success(emptyList())
        runCatching {
            val resp = client.newCall(authedReq(cfg, "/api/states").build()).execute()
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) error("HTTP ${resp.code}")
            entityListAdapter.fromJson(body) ?: emptyList()
        }
    }

    suspend fun services(cfg: HassConfig): Result<List<HassServiceDomain>> = withContext(Dispatchers.IO) {
        if (!cfg.isValid()) return@withContext Result.success(emptyList())
        runCatching {
            val resp = client.newCall(authedReq(cfg, "/api/services").build()).execute()
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) error("HTTP ${resp.code}")
            serviceListAdapter.fromJson(body) ?: emptyList()
        }
    }

    /** Calls a service like `light.turn_on` with optional payload (e.g. {"entity_id": "light.kitchen"}). */
    suspend fun callService(
        cfg: HassConfig,
        domain: String,
        service: String,
        payload: Map<String, Any?> = emptyMap(),
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!cfg.isValid()) return@withContext Result.failure(IllegalArgumentException("HA URL or token missing"))
        runCatching {
            val body = mapAdapter.toJson(payload)
                .toRequestBody("application/json".toMediaTypeOrNull())
            val resp = client.newCall(
                authedReq(cfg, "/api/services/$domain/$service").post(body).build()
            ).execute()
            if (!resp.isSuccessful) error("HTTP ${resp.code} — ${resp.body?.string().orEmpty()}")
            resp.close()
            Unit
        }
    }

    /** Trigger a script: shorthand for callService("script", scriptName). */
    suspend fun runScript(cfg: HassConfig, scriptName: String): Result<Unit> =
        callService(cfg, "script", scriptName)

    /** Activate a scene: shorthand for callService("scene", "turn_on", {entity_id: scene.x}). */
    suspend fun activateScene(cfg: HassConfig, sceneEntityId: String): Result<Unit> =
        callService(cfg, "scene", "turn_on", mapOf("entity_id" to sceneEntityId))

    private fun authedReq(cfg: HassConfig, path: String): Request.Builder =
        Request.Builder()
            .url(cfg.baseUrl.trimEnd('/') + path)
            .header("Authorization", "Bearer ${cfg.token}")
            .header("Content-Type", "application/json")
}
