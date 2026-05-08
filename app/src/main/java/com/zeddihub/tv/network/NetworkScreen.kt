package com.zeddihub.tv.network

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

@Composable
fun NetworkScreen(vm: NetworkViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Síťové nástroje", fontSize = 28.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)

        // Wake-on-LAN
        Card("Wake-on-LAN") {
            Text("Probudit PC nebo NAS magic packetem.", fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 12.dp)) {
                Button(onClick = { vm.wakeFirstSavedDevice() }) { Text("Probudit uložené") }
                Button(onClick = { vm.wakeQuick() }) { Text("Quick MAC test") }
            }
            state.wolMessage?.let {
                Text(it, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp))
            }
        }

        // Speed test
        Card("Speed test") {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 8.dp)) {
                Button(onClick = { vm.runSpeedTest() }, enabled = !state.speedTesting) {
                    Text(if (state.speedTesting) "Měřím…" else "Spustit test")
                }
                state.lastSpeed?.let {
                    Text("⬇ %.1f Mbps · %d ms".format(it.downloadMbps ?: 0.0, it.pingMs ?: 0L),
                        fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Ping batch
        Card("Ping běžných služeb") {
            Button(onClick = { vm.runPingBatch() }, enabled = !state.pinging) {
                Text(if (state.pinging) "Pinguji…" else "Spustit ping batch")
            }
            state.pingResults.forEach { r ->
                Text("${r.host}:${r.port}  ${if (r.ok) "${r.latencyMs} ms" else "× nedosažitelné"}",
                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun Card(title: String, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        colors = androidx.tv.material3.SurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp))
            content()
        }
    }
}
