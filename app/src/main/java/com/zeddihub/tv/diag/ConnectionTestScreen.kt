package com.zeddihub.tv.diag

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

@Composable
fun ConnectionTestScreen(vm: ConnectionTestViewModel = hiltViewModel()) {
    val running by vm.running.collectAsState()
    val report by vm.report.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Test připojení", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("Pre-flight diagnostika před streamem. DNS, latence, jitter, throughput.",
                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = { vm.run() }, enabled = !running) {
                Text(if (running) "Měřím…" else "Spustit test")
            }
        }

        report?.let { r ->
            VerdictBanner(r.verdict)
            Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                r.steps.forEach { StepRow(it) }
            }
        }
    }
}

@Composable
private fun VerdictBanner(v: ConnectionTestReport.Verdict) {
    val (color, label, sub) = when (v) {
        ConnectionTestReport.Verdict.GOOD -> Triple(Color(0xFF22C55E), "✓ Vše OK", "Streaming poběží hladce.")
        ConnectionTestReport.Verdict.OK -> Triple(Color(0xFFF59E0B), "⚠ Drobné problémy", "Streamovat se dá, ale občas může buffrovat.")
        ConnectionTestReport.Verdict.BAD -> Triple(Color(0xFFEF4444), "× Špatné spojení", "Stream bude trhat. Viz rady níže.")
        ConnectionTestReport.Verdict.OFFLINE -> Triple(Color(0xFF6B7280), "× Offline", "Síť nefunguje vůbec.")
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        colors = androidx.tv.material3.SurfaceDefaults.colors(containerColor = color.copy(alpha = 0.18f)),
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(label, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(sub, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun StepRow(s: StepResult) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        colors = androidx.tv.material3.SurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Text(if (s.ok) "✓" else "×",
                fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = if (s.ok) Color(0xFF22C55E) else Color(0xFFEF4444),
                modifier = Modifier.padding(end = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(s.name, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text(s.detail, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                s.advice?.let {
                    Text("→ $it", fontSize = 12.sp, color = Color(0xFFF59E0B),
                        modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}
