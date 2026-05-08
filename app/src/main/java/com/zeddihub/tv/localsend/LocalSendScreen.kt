package com.zeddihub.tv.localsend

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LocalSendScreen(vm: LocalSendViewModel = hiltViewModel()) {
    val running by vm.running.collectAsState()
    val received by vm.received.collectAsState()
    val addr by vm.address.collectAsState()
    val mdns by vm.mdnsRegistered.collectAsState()

    LaunchedEffect(Unit) { vm.refreshAddress() }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("LocalSend příjem", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("Přijímej soubory z mobilu / desktop přes lokální Wi-Fi.",
                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = { if (running) vm.stop() else vm.start() }) {
                Text(if (running) "Vypnout server" else "Zapnout server")
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            colors = androidx.tv.material3.SurfaceDefaults.colors(
                containerColor = if (running) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (running && addr != null) {
                    Text("Server běží — pošli soubor na:", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("http://$addr", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp))
                    Text("Otevři LocalSend appku a zadej tuto adresu (nebo použij \"Přidat zařízení ručně\").",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 8.dp))
                } else if (running) {
                    Text("Server běží na portu 53317, ale nelze určit IP. Zkontroluj Wi-Fi.",
                        color = MaterialTheme.colorScheme.error)
                } else {
                    Text("Server vypnutý.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Text("Přijaté soubory (${received.size})",
            fontSize = 16.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 32.dp, bottom = 12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (received.isEmpty()) {
                Text("Zatím nic. Pošli něco z telefonu.",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            received.reversed().forEach { f ->
                ReceivedRow(f)
            }
        }
    }
}

@Composable
private fun ReceivedRow(f: LocalSendServer.ReceivedFile) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        colors = androidx.tv.material3.SurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(f.name, color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("${formatBytes(f.size)} · ${formatTime(f.timestamp)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
            Text(f.path, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        }
    }
}

private fun formatBytes(b: Long): String = when {
    b < 1024 -> "$b B"
    b < 1024 * 1024 -> "%.1f KB".format(b / 1024.0)
    b < 1024 * 1024 * 1024 -> "%.1f MB".format(b / (1024.0 * 1024.0))
    else -> "%.2f GB".format(b / (1024.0 * 1024.0 * 1024.0))
}

private fun formatTime(ts: Long): String =
    SimpleDateFormat("HH:mm:ss", Locale("cs")).format(Date(ts))
