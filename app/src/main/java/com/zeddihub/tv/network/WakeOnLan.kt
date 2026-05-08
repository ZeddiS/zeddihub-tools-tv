package com.zeddihub.tv.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * Sends a Wake-on-LAN magic packet to the given MAC address. Broadcast to
 * the directed broadcast address of the local subnet. Works on Wi-Fi (router
 * must allow directed broadcasts; most home routers do).
 */
object WakeOnLan {

    suspend fun send(mac: String, broadcastAddr: String = "255.255.255.255", port: Int = 9): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val macBytes = parseMac(mac)
                val packet = ByteArray(6 + 16 * 6)
                for (i in 0..5) packet[i] = 0xFF.toByte()
                for (i in 1..16) System.arraycopy(macBytes, 0, packet, i * 6, 6)
                val addr = InetAddress.getByName(broadcastAddr)
                DatagramSocket().use { socket ->
                    socket.broadcast = true
                    socket.send(DatagramPacket(packet, packet.size, addr, port))
                }
                Unit
            }
        }

    private fun parseMac(mac: String): ByteArray {
        val cleaned = mac.replace("[:\\-\\.\\s]".toRegex(), "")
        require(cleaned.length == 12) { "MAC adresa musí mít 12 hex znaků" }
        return ByteArray(6) { i ->
            cleaned.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }
}
