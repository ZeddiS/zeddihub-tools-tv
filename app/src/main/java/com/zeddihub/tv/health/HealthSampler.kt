package com.zeddihub.tv.health

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class HealthSample(
    val timestamp: Long,
    val cpuTempC: Float?,        // null if unreadable
    val cpuLoadPct: Int?,        // 0..100 over last sample interval
    val ramUsedMb: Long,
    val ramTotalMb: Long,
    val storageUsedGb: Long,
    val storageTotalGb: Long,
    val batteryPct: Int?,        // null on most TV boxes (no battery)
    val uptime: Long,            // seconds
)

/**
 * One-shot probe of system metrics. No background loop here — the caller
 * polls on whatever cadence makes sense (Dashboard refresh, dedicated
 * Health screen 1-Hz tick, alarm trigger).
 *
 * Reads from /proc and /sys directly because that's universally available
 * on rooted-or-not Android TV firmware. Falls back to ActivityManager and
 * StatFs for the parts that need a system service.
 */
@Singleton
class HealthSampler @Inject constructor(
    @ApplicationContext private val ctx: Context,
) {
    // CPU load tracking — diff between samples
    private var lastCpuTotal: Long = 0
    private var lastCpuIdle: Long = 0

    fun sample(): HealthSample {
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)

        val ramUsedMb = (mi.totalMem - mi.availMem) / (1024L * 1024L)
        val ramTotalMb = mi.totalMem / (1024L * 1024L)

        val stat = StatFs(Environment.getDataDirectory().path)
        val storageTotalGb = (stat.blockSizeLong * stat.blockCountLong) / (1024L * 1024L * 1024L)
        val storageAvailGb = (stat.blockSizeLong * stat.availableBlocksLong) / (1024L * 1024L * 1024L)
        val storageUsedGb = storageTotalGb - storageAvailGb

        return HealthSample(
            timestamp = System.currentTimeMillis(),
            cpuTempC = readCpuTemp(),
            cpuLoadPct = readCpuLoad(),
            ramUsedMb = ramUsedMb,
            ramTotalMb = ramTotalMb,
            storageUsedGb = storageUsedGb,
            storageTotalGb = storageTotalGb,
            batteryPct = readBatteryPct(),
            uptime = android.os.SystemClock.elapsedRealtime() / 1000L,
        )
    }

    private fun readCpuTemp(): Float? {
        // Try thermal zones in order; the SoC zone is usually 0 or labeled.
        val candidates = listOf(0, 1, 2, 3, 4, 5)
        for (i in candidates) {
            val typeFile = File("/sys/class/thermal/thermal_zone$i/type")
            val tempFile = File("/sys/class/thermal/thermal_zone$i/temp")
            if (!tempFile.exists()) continue
            val type = runCatching { typeFile.readText().trim().lowercase() }.getOrDefault("")
            // Skip non-CPU zones (battery temp, charger, etc.)
            if (type.contains("battery") || type.contains("charger") || type.contains("usb")) continue
            val raw = runCatching { tempFile.readText().trim().toLongOrNull() }.getOrNull() ?: continue
            // /sys exposes temp in milli-degrees Celsius (e.g. 42500 → 42.5°C)
            val celsius = raw / 1000f
            if (celsius in 1f..150f) return celsius
        }
        return null
    }

    private fun readCpuLoad(): Int? {
        // /proc/stat first line: cpu  user nice system idle iowait irq softirq steal guest guest_nice
        val parts = runCatching {
            File("/proc/stat").bufferedReader().use { it.readLine() }
        }.getOrNull()?.trim()?.split(Regex("\\s+")) ?: return null
        if (parts.size < 5 || parts[0] != "cpu") return null
        val nums = parts.drop(1).mapNotNull { it.toLongOrNull() }
        if (nums.size < 4) return null
        val idle = nums[3] + (nums.getOrNull(4) ?: 0L)
        val total = nums.sum()
        val deltaTotal = total - lastCpuTotal
        val deltaIdle = idle - lastCpuIdle
        lastCpuTotal = total
        lastCpuIdle = idle
        if (deltaTotal <= 0) return null
        val busy = deltaTotal - deltaIdle
        return ((busy * 100) / deltaTotal).toInt().coerceIn(0, 100)
    }

    private fun readBatteryPct(): Int? {
        // TV boxes usually have no battery. /sys/class/power_supply/battery/capacity
        // returns 0..100 if present; null if absent.
        val f = File("/sys/class/power_supply/battery/capacity")
        if (!f.exists()) return null
        return runCatching { f.readText().trim().toIntOrNull() }.getOrNull()
    }
}
