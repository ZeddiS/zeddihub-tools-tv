package com.zeddihub.tv.data.telemetry

import android.content.Context
import android.os.Build
import com.zeddihub.tv.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lightweight telemetry — POSTs anonymous events to /tools/telemetry.php
 * so Admin → Analytics → TV can show usage data. The X-Client-Kind: tv
 * header is added globally by the OkHttp interceptor in AppModule, so the
 * backend can filter TV events from mobile/desktop.
 *
 * No PII is sent: anon=true, no IP / device id, just version + os build +
 * event name. The endpoint stores at most 24h of events and rotates.
 */
@Singleton
class TelemetryRecorder @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val client: OkHttpClient,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val endpoint = "${BuildConfig.SITE_BASE_URL}tools/telemetry.php"

    fun recordLaunch() = record("launch", emptyMap())

    fun recordEvent(event: String, panel: String? = null) =
        record(event, if (panel != null) mapOf("panel" to panel) else emptyMap())

    private fun record(event: String, extra: Map<String, String>) {
        scope.launch {
            runCatching {
                val body = JSONObject().apply {
                    put("event", event)
                    put("version", BuildConfig.VERSION_NAME)
                    put("version_code", BuildConfig.VERSION_CODE)
                    put("os", "android_tv_${Build.VERSION.SDK_INT}")
                    put("anon", true)
                    extra.forEach { (k, v) -> put(k, v) }
                }
                val req = Request.Builder()
                    .url(endpoint)
                    .post(body.toString().toRequestBody("application/json".toMediaType()))
                    .build()
                client.newCall(req).execute().close()
            }
        }
    }
}
