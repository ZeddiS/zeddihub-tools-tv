package com.zeddihub.tv.health

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.zeddihub.tv.ui.components.KpiTile
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.SectionTitle
import com.zeddihub.tv.ui.components.Tone
import com.zeddihub.tv.ui.components.ZhCard
import com.zeddihub.tv.ui.components.ZhPageScaffold

@Composable
fun HealthScreen(vm: HealthViewModel = hiltViewModel()) {
    val current by vm.current.collectAsState()
    val history by vm.tempHistory.collectAsState()

    ZhPageScaffold {
        PageHeader(
            title = "Stav TV boxu",
            subtitle = "Real-time monitoring CPU, RAM, úložiště a teploty.",
            icon = Icons.Outlined.MonitorHeart,
        )

        // KPI tiles
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            current?.let { s ->
                KpiTile(Icons.Outlined.Thermostat, "Teplota",
                    s.cpuTempC?.let { "%.1f°".format(it) } ?: "—",
                    tone = tempTone(s.cpuTempC),
                    modifier = Modifier.weight(1f))
                KpiTile(Icons.Outlined.Speed, "CPU",
                    s.cpuLoadPct?.let { "$it %" } ?: "—",
                    tone = loadTone(s.cpuLoadPct),
                    modifier = Modifier.weight(1f))
                KpiTile(Icons.Outlined.Memory, "RAM",
                    "${s.ramUsedMb} / ${s.ramTotalMb} MB",
                    modifier = Modifier.weight(1f))
                KpiTile(Icons.Outlined.Storage, "Úložiště",
                    "${s.storageUsedGb} / ${s.storageTotalGb} GB",
                    modifier = Modifier.weight(1f))
                if (s.batteryPct != null) {
                    KpiTile(Icons.Outlined.BatteryFull, "Baterie",
                        "${s.batteryPct} %",
                        modifier = Modifier.weight(1f))
                }
            }
        }

        // Temperature graph
        SectionTitle("Teplota za posledních ~5 minut")
        ZhCard {
            if (history.isEmpty()) {
                Text("Sbírám data…",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                TempChart(history,
                    modifier = Modifier.fillMaxWidth().height(180.dp))
            }
        }

        // Uptime
        current?.let { s ->
            ZhCard {
                Row {
                    Text("Uptime",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f))
                    Text(formatUptime(s.uptime),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}

@Composable
private fun TempChart(samples: List<Float>, modifier: Modifier) {
    val accent = MaterialTheme.colorScheme.primary
    val border = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier = modifier) {
        if (samples.size < 2) return@Canvas
        val minT = samples.min().coerceAtMost(30f)
        val maxT = samples.max().coerceAtLeast(60f)
        val range = (maxT - minT).coerceAtLeast(1f)
        val w = size.width
        val h = size.height
        // Background grid
        for (i in 0..3) {
            val y = h * i / 3f
            drawLine(border, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
        }
        // Data path
        val path = Path()
        samples.forEachIndexed { idx, t ->
            val x = w * idx / (samples.size - 1)
            val y = h - (t - minT) / range * h
            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, accent, style = Stroke(width = 3f))
    }
}

private fun tempTone(t: Float?): Color = when {
    t == null -> Tone.Muted
    t < 50 -> Tone.Success
    t < 65 -> Tone.Warning
    else -> Tone.Error
}

private fun loadTone(pct: Int?): Color = when {
    pct == null -> Tone.Muted
    pct < 60 -> Tone.Success
    pct < 85 -> Tone.Warning
    else -> Tone.Error
}

private fun formatUptime(seconds: Long): String {
    val d = seconds / 86_400
    val h = (seconds % 86_400) / 3600
    val m = (seconds % 3600) / 60
    return when {
        d > 0 -> "${d}d ${h}h ${m}m"
        h > 0 -> "${h}h ${m}m"
        else -> "${m}m"
    }
}
