package com.zeddihub.tv.network

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
fun NetworkScreen(vm: NetworkViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    ZhPageScaffold {
        PageHeader(
            title = "Síťové nástroje",
            subtitle = "Wake-on-LAN, ping, speed test.",
            icon = Icons.Outlined.NetworkCheck,
        )

        // Wake-on-LAN
        Card("Wake-on-LAN") {
            Text("Probudit PC nebo NAS magic packetem.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 12.dp)) {
                Button(onClick = { vm.wakeFirstSavedDevice() }) { Text("Probudit uložené") }
                Button(onClick = { vm.wakeQuick() }) { Text("Quick MAC test") }
            }
            state.wolMessage?.let {
                Text(it,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp))
            }
        }

        // Speed test
        Card("Speed test") {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Button(onClick = { vm.runSpeedTest() }, enabled = !state.speedTesting) {
                    Text(if (state.speedTesting) "Měřím…" else "Spustit test")
                }
                state.lastSpeed?.let {
                    Text("⬇ %.1f Mbps · %d ms".format(it.downloadMbps ?: 0.0, it.pingMs ?: 0L),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Ping batch
        Card("Ping běžných služeb") {
            Button(onClick = { vm.runPingBatch() }, enabled = !state.pinging) {
                Text(if (state.pinging) "Pinguji…" else "Spustit ping batch")
            }
            Column(modifier = Modifier.padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                state.pingResults.forEach { r ->
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${r.host}:${r.port}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground)
                        Text(if (r.ok) "${r.latencyMs} ms" else "× nedosažitelné",
                            fontSize = 13.sp,
                            color = if (r.ok) Tone.Success else Tone.Error,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun Card(title: String, content: @Composable () -> Unit) {
    ZhCard {
        Column {
            Text(title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp))
            content()
        }
    }
}
