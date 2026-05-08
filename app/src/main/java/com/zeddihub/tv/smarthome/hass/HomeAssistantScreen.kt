package com.zeddihub.tv.smarthome.hass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

@Composable
fun HomeAssistantScreen(vm: HomeAssistantViewModel = hiltViewModel()) {
    val baseUrl by vm.baseUrl.collectAsState()
    val token by vm.token.collectAsState()
    val pinged by vm.lastPing.collectAsState()
    val pinned by vm.pinnedEntities.collectAsState()
    val states by vm.statesByEntity.collectAsState()
    val message by vm.message.collectAsState()

    var editingUrl by remember { mutableStateOf(baseUrl) }
    var editingToken by remember { mutableStateOf(token) }

    LaunchedEffect(baseUrl, token) {
        if (editingUrl.isBlank()) editingUrl = baseUrl
        if (editingToken.isBlank()) editingToken = token
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("Home Assistant", fontSize = 28.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)
        Text("Bearer-token integrace s HA. Připnuté entity = quick toggles z TV.",
            fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Config card
        Surface(
            shape = RoundedCornerShape(16.dp),
            colors = androidx.tv.material3.SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LabeledRow("Base URL", baseUrl.ifBlank { "(nenastaveno)" })
                LabeledRow("Token", if (token.isBlank()) "(nenastaveno)" else "••••••••${token.takeLast(4)}")
                LabeledRow("Status", pinged ?: "(nepinguto)")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { vm.useDefault() }) { Text("Default LAN URL") }
                    Button(onClick = { vm.ping() }) { Text("Ping HA") }
                    Button(onClick = { vm.refreshStates() }) { Text("Refresh stavy") }
                }
                message?.let {
                    Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        // Pinned entities
        Text("Připnuté entity (${pinned.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))
        if (pinned.isEmpty()) {
            Text("Žádné. Připni entity v karte níže (po prvním Refresh stavů).",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            pinned.forEach { eid ->
                EntityRow(
                    entityId = eid,
                    state = states[eid] ?: "?",
                    onToggle = { vm.toggle(eid) },
                    onUnpin = { vm.unpin(eid) },
                )
            }
        }

        // Quick scene tester
        Surface(
            shape = RoundedCornerShape(12.dp),
            colors = androidx.tv.material3.SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Spustit script / scene", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("Použij ID z HA (např. \"movie_mode\" pro script, \"scene.evening\" pro scenes).",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp))
                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.runScript("movie_mode") }) { Text("script.movie_mode") }
                    Button(onClick = { vm.activateScene("scene.evening") }) { Text("scene.evening") }
                }
            }
        }
    }
}

@Composable
private fun LabeledRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp))
        Text(value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun EntityRow(entityId: String, state: String, onToggle: () -> Unit, onUnpin: () -> Unit) {
    val isOn = state.equals("on", ignoreCase = true) || state.equals("home", ignoreCase = true)
    val accent = if (isOn) Color(0xFF22C55E) else Color(0xFF6B7280)
    Surface(
        onClick = onToggle,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = accent.copy(alpha = 0.20f),
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier
                .padding(end = 12.dp)) {
                Text(if (isOn) "●" else "○", color = accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(entityId, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                Text("state: $state", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onUnpin) { Text("Odepnout") }
        }
    }
}
