package com.zeddihub.tv.health

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

@Composable
fun HealthScreen(vm: HealthViewModel = hiltViewModel()) {
    val current by vm.current.collectAsState()
    val history by vm.tempHistory.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("Stav TV boxu", fontSize = 28.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)
        Text("Real-time monitoring CPU, RAM, úložiště a teploty.", fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        // KPI tiles
        Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            current?.let { s ->
                KpiTile(Icons.Outlined.Thermostat,
                    title = "Teplota",
                    value = s.cpuTempC?.let { "%.1f °C".format(it) } ?: "—",
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
                    KpiTile(Icons.Outlined.BatteryFull, "Baterie", "${s.batteryPct} %",
                        modifier = Modifier.weight(1f))
                }
            }
        }

        // Temperature chart
        Surface(
            shape = RoundedCornerShape(16.dp),
            colors = androidx.tv.material3.SurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Teplota za posledních ~5 minut", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                if (history.isEmpty()) {
                    Text("Sbírám data…", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp))
                } else {
                    TempChart(history,
                        modifier = Modifier.fillMaxWidth().height(180.dp).padding(top = 16.dp))
                }
            }
        }

        // Uptime + thresholds
        current?.let { s ->
            Text("Uptime: ${formatUptime(s.uptime)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
private fun KpiTile(icon: ImageVector, title: String, value: String,
                    tone: Color = MaterialTheme.colorScheme.primary,
                    modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        colors = androidx.tv.material3.SurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = tone, modifier = Modifier.size(20.dp))
                Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp))
            }
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = tone, modifier = Modifier.padding(top = 8.dp))
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
        // Background grid: 3 horizontal lines
        for (i in 0..3) {
            val y = h * i / 3f
            drawLine(border, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
        }
        // Path
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
    t == null -> Color(0xFF6B7280)
    t < 50 -> Color(0xFF22C55E)
    t < 65 -> Color(0xFFF59E0B)
    else -> Color(0xFFEF4444)
}

private fun loadTone(pct: Int?): Color = when {
    pct == null -> Color(0xFF6B7280)
    pct < 60 -> Color(0xFF22C55E)
    pct < 85 -> Color(0xFFF59E0B)
    else -> Color(0xFFEF4444)
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
