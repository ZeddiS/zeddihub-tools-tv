package com.zeddihub.tv.diag

import com.zeddihub.tv.network.PingTool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pre-flight diagnostic for streaming. Runs a battery of probes in parallel
 * and reports a verdict + per-step status, so the user can spot the bad
 * actor at a glance ("DNS works, but throughput is 2 Mbps — your Wi-Fi is
 * congested").
 *
 * Probes:
 *   - DNS resolve of zeddihub.eu, google.com, cloudflare.com
 *   - TCP ping (53/443) to 1.1.1.1, 8.8.8.8 — measure latency + packet loss
 *   - Latency variance over 10 pings (jitter)
 *   - Throughput via 1 MB Cloudflare blob
 */
data class StepResult(
    val name: String,
    val ok: Boolean,
    val detail: String,
    val advice: String? = null, // shown when !ok
)

data class ConnectionTestReport(
    val verdict: Verdict,
    val steps: List<StepResult>,
) {
    enum class Verdict { GOOD, OK, BAD, OFFLINE }
}

@Singleton
class ConnectionTest @Inject constructor() {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun run(): ConnectionTestReport = coroutineScope {
        val dnsAsync = async { dnsStep() }
        val pingAsync = async { pingStep() }
        val jitterAsync = async { jitterStep() }
        val throughputAsync = async { throughputStep() }

        val steps = listOf(dnsAsync.await(), pingAsync.await(), jitterAsync.await(), throughputAsync.await())
        val verdict = when {
            steps.none { it.ok } -> ConnectionTestReport.Verdict.OFFLINE
            steps.count { !it.ok } >= 2 -> ConnectionTestReport.Verdict.BAD
            steps.any { !it.ok } -> ConnectionTestReport.Verdict.OK
            else -> ConnectionTestReport.Verdict.GOOD
        }
        ConnectionTestReport(verdict, steps)
    }

    private suspend fun dnsStep(): StepResult = withContext(Dispatchers.IO) {
        val hosts = listOf("zeddihub.eu", "google.com", "cloudflare.com")
        val results = hosts.map { host ->
            runCatching {
                val start = System.nanoTime()
                InetAddress.getByName(host)
                (System.nanoTime() - start) / 1_000_000L
            }.getOrNull()
        }
        val ok = results.count { it != null } >= 2
        val avg = results.filterNotNull().average().takeIf { !it.isNaN() }?.toLong() ?: -1
        StepResult(
            name = "DNS resolve",
            ok = ok,
            detail = if (ok) "průměr $avg ms (3 hosti)" else "DNS server nereaguje",
            advice = if (!ok) "Zkus přepnout na 1.1.1.1 nebo 8.8.8.8 v Settings → Network" else null,
        )
    }

    private suspend fun pingStep(): StepResult = coroutineScope {
        val targets = listOf("1.1.1.1" to 53, "8.8.8.8" to 53, "1.0.0.1" to 53)
        val results = targets.map { (h, p) -> async { PingTool.tcpPing(h, p, 1500) } }.map { it.await() }
        val okCount = results.count { it.ok }
        val avg = results.mapNotNull { it.latencyMs }.average().takeIf { !it.isNaN() }?.toLong() ?: -1
        val ok = okCount >= 2
        StepResult(
            name = "Latence",
            ok = ok,
            detail = if (ok) "$avg ms ($okCount/${targets.size} OK)" else "$okCount/${targets.size} nedosažitelné",
            advice = if (!ok) "Wi-Fi nebo router pravděpodobně padá. Zkus restart routeru." else null,
        )
    }

    private suspend fun jitterStep(): StepResult = coroutineScope {
        val pings = (1..10).map { async { PingTool.tcpPing("1.1.1.1", 53, 1000) } }.map { it.await() }
        val latencies = pings.mapNotNull { it.latencyMs }
        if (latencies.size < 5) {
            return@coroutineScope StepResult("Jitter", false, "Málo úspěšných pings",
                "Síť ztrácí pakety; restart Wi-Fi často pomůže.")
        }
        val avg = latencies.average()
        val variance = latencies.map { (it - avg) * (it - avg) }.average()
        val stdDev = kotlin.math.sqrt(variance)
        val ok = stdDev < 30
        StepResult(
            name = "Jitter",
            ok = ok,
            detail = "σ = ${stdDev.toLong()} ms (avg ${avg.toLong()})",
            advice = if (!ok) "Vysoký jitter → obraz bude trhat. Přejdi blíž k routeru nebo zapni 5 GHz." else null,
        )
    }

    private suspend fun throughputStep(): StepResult = withContext(Dispatchers.IO) {
        runCatching {
            val start = System.nanoTime()
            val resp = httpClient.newCall(
                Request.Builder().url("https://speed.cloudflare.com/__down?bytes=1000000").build()
            ).execute()
            val bytes = resp.body?.bytes()?.size ?: 0
            val sec = (System.nanoTime() - start) / 1e9
            if (bytes <= 0 || sec <= 0) return@runCatching null
            val mbps = (bytes * 8.0 / 1_000_000.0) / sec
            mbps
        }.getOrNull()?.let { mbps ->
            val ok = mbps >= 5.0
            StepResult(
                name = "Throughput",
                ok = ok,
                detail = "%.1f Mbps".format(mbps),
                advice = if (!ok) "Pod 5 Mbps = HD streaming bude buffrovat. Zkus speedtest." else null,
            )
        } ?: StepResult("Throughput", false, "Selhalo", "Speedtest server nereaguje, ale to není fatální.")
    }
}
