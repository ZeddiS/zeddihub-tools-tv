package com.zeddihub.tv.smarthome

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.zeddihub.tv.data.prefs.AppPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads/writes [SmartDevice] entries and dispatches on/off/scene commands
 * to each one. Network calls go through the shared [OkHttpClient] so
 * they share connection pool, timeouts, and the X-App-Secret header.
 */
@Singleton
class SmartHomeController @Inject constructor(
    private val prefs: AppPrefs,
    private val client: OkHttpClient,
    moshi: Moshi,
) {
    private val adapter = moshi.adapter<List<SmartDevice>>(
        Types.newParameterizedType(List::class.java, SmartDevice::class.java)
    )
    private val emptyJson = "{}".toRequestBody("application/json".toMediaTypeOrNull())

    val devices: Flow<List<SmartDevice>> = prefs.smartDevicesJson.map { json ->
        runCatching { adapter.fromJson(json) ?: emptyList() }.getOrDefault(emptyList())
    }

    suspend fun list(): List<SmartDevice> =
        runCatching { adapter.fromJson(prefs.smartDevicesJson.first()) ?: emptyList() }
            .getOrDefault(emptyList())

    suspend fun upsert(d: SmartDevice) {
        val cur = list().toMutableList()
        val idx = cur.indexOfFirst { it.id == d.id }
        if (idx >= 0) cur[idx] = d else cur += d
        prefs.setSmartDevicesJson(adapter.toJson(cur))
    }

    suspend fun delete(id: String) {
        prefs.setSmartDevicesJson(adapter.toJson(list().filter { it.id != id }))
    }

    /** Toggle: ON → OFF if currently on (we don't track state, just send opposite of [turnOn]). */
    suspend fun setPower(d: SmartDevice, on: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            when (d.kind) {
                SmartKinds.HUE_LIGHT -> {
                    // PUT http://<bridge>/api/<user>/lights/<id>/state {"on":true}
                    val url = "http://${d.host}/api/${d.token}/lights/${d.target}/state"
                    val body = """{"on":$on}""".toRequestBody("application/json".toMediaTypeOrNull())
                    client.newCall(Request.Builder().url(url).put(body).build()).execute().close()
                }
                SmartKinds.HUE_GROUP -> {
                    val url = "http://${d.host}/api/${d.token}/groups/${d.target}/action"
                    val body = """{"on":$on}""".toRequestBody("application/json".toMediaTypeOrNull())
                    client.newCall(Request.Builder().url(url).put(body).build()).execute().close()
                }
                SmartKinds.TASMOTA -> {
                    // GET http://<host>/cm?cmnd=Power%20ON  (or OFF)
                    val cmnd = if (on) "Power ON" else "Power OFF"
                    val url = "http://${d.host}/cm?cmnd=" + java.net.URLEncoder.encode(cmnd, "UTF-8") +
                            (if (d.token.isNotBlank())
                                "&user=admin&password=" + java.net.URLEncoder.encode(d.token, "UTF-8")
                             else "")
                    client.newCall(Request.Builder().url(url).get().build()).execute().close()
                }
                SmartKinds.TUYA_LOCAL -> {
                    // Tuya local-key requires AES-encrypted device protocol — out of scope for v0.3.0.
                    // For now we delegate to webhook so the user can route through Home Assistant or a custom bridge.
                    if (d.webhookUrl.isNotBlank()) callWebhook(d, on) else error("Tuya local není ještě podporováno; nastavte webhook na HA.")
                }
                SmartKinds.WEBHOOK -> callWebhook(d, on)
                else -> error("Neznámý typ zařízení: ${d.kind}")
            }
            Unit
        }
    }

    private fun callWebhook(d: SmartDevice, on: Boolean) {
        val url = d.webhookUrl
        val onSuffix = if (on) "?on=true" else "?on=false"
        val finalUrl = if (url.contains("?")) "$url&on=$on" else url + onSuffix
        val req = Request.Builder().url(finalUrl).apply {
            if (d.webhookMethod.equals("POST", ignoreCase = true)) post(emptyJson) else get()
        }.build()
        client.newCall(req).execute().close()
    }

    companion object {
        fun newId(): String = UUID.randomUUID().toString().take(8)
    }
}
