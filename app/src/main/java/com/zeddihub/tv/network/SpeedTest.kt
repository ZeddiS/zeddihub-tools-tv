package com.zeddihub.tv.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class SpeedResult(val downloadMbps: Double?, val pingMs: Long?)

/**
 * Lightweight one-shot speed test. Downloads a 10 MB blob from Cloudflare's
 * speedtest backend and measures wall-clock throughput. Not as precise as
 * Ookla but fine for a "is internet OK" indicator.
 */
object SpeedTest {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun run(): SpeedResult = withContext(Dispatchers.IO) {
        val ping = PingTool.tcpPing("speed.cloudflare.com", 443).latencyMs
        val download = runCatching {
            val start = System.nanoTime()
            val resp = client.newCall(
                Request.Builder().url("https://speed.cloudflare.com/__down?bytes=10000000").build()
            ).execute()
            val bytes = resp.body?.bytes()?.size ?: 0
            val durSec = (System.nanoTime() - start) / 1e9
            if (bytes > 0 && durSec > 0) (bytes * 8.0 / 1_000_000.0) / durSec else null
        }.getOrNull()
        SpeedResult(download, ping)
    }
}
