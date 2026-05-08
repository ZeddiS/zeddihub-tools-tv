package com.zeddihub.tv.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

data class PingResult(val host: String, val port: Int, val latencyMs: Long?, val ok: Boolean)

object PingTool {
    suspend fun tcpPing(host: String, port: Int = 80, timeoutMs: Int = 1500): PingResult =
        withContext(Dispatchers.IO) {
            val start = System.nanoTime()
            runCatching {
                Socket().use { s ->
                    s.connect(InetSocketAddress(host, port), timeoutMs)
                }
                PingResult(host, port, (System.nanoTime() - start) / 1_000_000L, true)
            }.getOrElse { PingResult(host, port, null, false) }
        }

    suspend fun batch(targets: List<Pair<String, Int>>): List<PingResult> = coroutineScope {
        targets.map { (h, p) -> async { tcpPing(h, p) } }.map { it.await() }
    }
}
