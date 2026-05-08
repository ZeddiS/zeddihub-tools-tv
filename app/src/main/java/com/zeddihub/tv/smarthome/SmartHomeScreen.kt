package com.zeddihub.tv.smarthome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun SmartHomeScreen(vm: SmartHomeViewModel = hiltViewModel()) {
    val devices by vm.devices.collectAsState()
    val message by vm.lastMessage.collectAsState()
    var editing by remember { mutableStateOf<SmartDevice?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Chytrá domácnost", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("Hue · Tasmota · Tuya · Webhook (Home Assistant / IFTTT).",
                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = { editing = newDraft() }) { Text("+ Přidat") }
        }

        message?.let {
            Text(it, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp))
        }

        Column(modifier = Modifier.padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (devices.isEmpty()) {
                Text("Žádná zařízení. Přidej Hue světlo, Tasmota zásuvku nebo webhook.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
            devices.forEach { d ->
                DeviceRow(d,
                    onOn = { vm.setPower(d, true) },
                    onOff = { vm.setPower(d, false) },
                    onEdit = { editing = d },
                    onDelete = { vm.delete(d.id) })
            }
        }
    }

    editing?.let { draft ->
        DeviceEditor(
            draft,
            onSave = { vm.upsert(it); editing = null },
            onCancel = { editing = null },
        )
    }
}

private fun newDraft() = SmartDevice(
    id = SmartHomeController.newId(),
    name = "Nové zařízení",
    kind = SmartKinds.HUE_LIGHT,
    icon = "💡",
)

@Composable
private fun DeviceRow(
    d: SmartDevice,
    onOn: () -> Unit, onOff: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        colors = androidx.tv.material3.SurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(20.dp)) {
            Text(d.icon, fontSize = 28.sp, modifier = Modifier.padding(end = 16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(d.name, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("${SmartKinds.displayName(d.kind)} · ${d.host.ifBlank { d.webhookUrl.takeIf { it.isNotBlank() } ?: "—" }}",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onOn) { Text("ON") }
            Box(modifier = Modifier.width(8.dp))
            Button(onClick = onOff) { Text("OFF") }
            Box(modifier = Modifier.width(8.dp))
            Button(onClick = onEdit) { Text("Upravit") }
            Box(modifier = Modifier.width(8.dp))
            Button(onClick = onDelete) { Text("Smazat") }
        }
    }
}

@Composable
private fun DeviceEditor(
    draft: SmartDevice,
    onSave: (SmartDevice) -> Unit,
    onCancel: () -> Unit,
) {
    var name by remember { mutableStateOf(draft.name) }
    var kind by remember { mutableStateOf(draft.kind) }
    var host by remember { mutableStateOf(draft.host) }
    var token by remember { mutableStateOf(draft.token) }
    var target by remember { mutableStateOf(draft.target) }
    var webhookUrl by remember { mutableStateOf(draft.webhookUrl) }
    var icon by remember { mutableStateOf(draft.icon) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        colors = androidx.tv.material3.SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Upravit zařízení — ${SmartKinds.displayName(kind)}",
                fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Typ: ", color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(end = 4.dp))
                listOf(SmartKinds.HUE_LIGHT, SmartKinds.HUE_GROUP, SmartKinds.TASMOTA,
                    SmartKinds.TUYA_LOCAL, SmartKinds.WEBHOOK).forEach { k ->
                    val sel = kind == k
                    Surface(
                        onClick = { kind = k },
                        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.primary,
                            contentColor = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            focusedContentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(SmartKinds.displayName(k),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontSize = 12.sp)
                    }
                }
            }

            Text("Detaily zadávat přes desktop/mobil verzi (TextField na D-pad je nepohodlné).",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Aktuální hodnoty:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Text("• Host: ${host.ifBlank { "—" }}", color = MaterialTheme.colorScheme.onBackground)
            Text("• Token: ${if (token.isBlank()) "—" else token.take(6) + "…"}",
                color = MaterialTheme.colorScheme.onBackground)
            Text("• Target: ${target.ifBlank { "—" }}", color = MaterialTheme.colorScheme.onBackground)
            Text("• Webhook: ${webhookUrl.ifBlank { "—" }}", color = MaterialTheme.colorScheme.onBackground)
            Text("Editor s plnými inputy přijde v 0.3.1 (USB klávesnice / voice input).",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onCancel) { Text("Zrušit") }
                Box(modifier = Modifier.weight(1f))
                Button(onClick = {
                    onSave(draft.copy(name = name, kind = kind, host = host,
                        token = token, target = target, webhookUrl = webhookUrl, icon = icon))
                }) { Text("Uložit") }
            }
        }
    }
}
