package com.zeddihub.tv.diag

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NetworkPing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.Tone
import com.zeddihub.tv.ui.components.ZhCard
import com.zeddihub.tv.ui.components.ZhPageScaffold

@Composable
fun ConnectionTestScreen(vm: ConnectionTestViewModel = hiltViewModel()) {
    val running by vm.running.collectAsState()
    val report by vm.report.collectAsState()

    ZhPageScaffold {
        PageHeader(
            title = "Test připojení",
            subtitle = "DNS · latence · jitter · throughput.",
            icon = Icons.Outlined.NetworkPing,
            trailing = {
                Button(onClick = { vm.run() }, enabled = !running) {
                    Text(if (running) "Měřím…" else "Spustit test")
                }
            },
        )

        report?.let { r ->
            VerdictBanner(r.verdict)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                r.steps.forEach { StepRow(it) }
            }
        }
    }
}

@Composable
private fun VerdictBanner(v: ConnectionTestReport.Verdict) {
    val (color, label, sub) = when (v) {
        ConnectionTestReport.Verdict.GOOD -> Triple(Tone.Success, "✓ Vše OK", "Streaming poběží hladce.")
        ConnectionTestReport.Verdict.OK -> Triple(Tone.Warning, "⚠ Drobné problémy", "Streamovat se dá, občas může buffrovat.")
        ConnectionTestReport.Verdict.BAD -> Triple(Tone.Error, "× Špatné spojení", "Stream bude trhat. Viz rady níže.")
        ConnectionTestReport.Verdict.OFFLINE -> Triple(Tone.Muted, "× Offline", "Síť nefunguje vůbec.")
    }
    ZhCard(container = color.copy(alpha = 0.15f)) {
        Column {
            Text(label, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(sub,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun StepRow(s: StepResult) {
    ZhCard {
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(if (s.ok) "✓" else "×",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (s.ok) Tone.Success else Tone.Error,
                modifier = Modifier.padding(end = 14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(s.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text(s.detail,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                s.advice?.let {
                    Text("→ $it",
                        fontSize = 12.sp,
                        color = Tone.Warning,
                        modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}
