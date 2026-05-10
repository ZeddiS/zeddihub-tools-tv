package com.zeddihub.tv.smarthome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
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
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.zeddihub.tv.ui.components.EmptyState
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.PsPrimaryButton
import com.zeddihub.tv.ui.components.PsSecondaryButton
import com.zeddihub.tv.ui.components.StatusPill
import com.zeddihub.tv.ui.components.Tone
import com.zeddihub.tv.ui.components.ZhCard
import com.zeddihub.tv.ui.components.ZhPageScaffold

@Composable
fun SmartHomeScreen(vm: SmartHomeViewModel = hiltViewModel()) {
    val devices by vm.devices.collectAsState()
    val message by vm.lastMessage.collectAsState()
    var editing by remember { mutableStateOf<SmartDevice?>(null) }

    ZhPageScaffold {
        PageHeader(
            title = "Chytrá domácnost",
            subtitle = "Hue · Tasmota · Tuya · Webhook (Home Assistant / IFTTT).",
            icon = Icons.Outlined.Lightbulb,
            trailing = {
                PsPrimaryButton(text = "+ Přidat", onClick = { editing = newDraft() })
            },
        )

        message?.let {
            ZhCard(container = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                Text(it,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium)
            }
        }

        if (devices.isEmpty()) {
            EmptyState(
                title = "Žádná zařízení",
                hint = "Přidej Hue světlo, Tasmota zásuvku, Tuya zařízení nebo generic webhook (Home Assistant / IFTTT).",
                icon = Icons.Outlined.Lightbulb,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            devices.forEach { d ->
                DeviceCard(d,
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
private fun DeviceCard(
    d: SmartDevice,
    onOn: () -> Unit, onOff: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit,
) {
    val accent = when (d.kind) {
        SmartKinds.HUE_LIGHT, SmartKinds.HUE_GROUP -> Tone.Warning
        SmartKinds.TASMOTA -> Tone.Info
        SmartKinds.TUYA_LOCAL -> Tone.Success
        SmartKinds.WEBHOOK -> MaterialTheme.colorScheme.primary
        else -> Tone.Muted
    }
    ZhCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Big icon at the left so the device is identifiable instantly.
            Text(d.icon,
                fontSize = 36.sp,
                modifier = Modifier.padding(end = 18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(d.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground)
                    Box(modifier = Modifier.padding(start = 10.dp)) {
                        StatusPill(label = SmartKinds.displayName(d.kind), tone = accent)
                    }
                }
                Text(d.host.ifBlank { d.webhookUrl.takeIf { it.isNotBlank() } ?: "—" },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp))
            }
            // Power controls — primary green for ON, gray for OFF
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PowerButton(label = "ON", tone = Tone.Success, onClick = onOn)
                PowerButton(label = "OFF", tone = Tone.Muted, onClick = onOff)
                PsSecondaryButton(text = "Upravit", onClick = onEdit)
                PsSecondaryButton(text = "Smazat", onClick = onDelete)
            }
        }
    }
}

@Composable
private fun PowerButton(
    label: String,
    tone: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = tone.copy(alpha = 0.18f),
            focusedContainerColor = tone,
            contentColor = tone,
            focusedContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DeviceEditor(
    draft: SmartDevice,
    onSave: (SmartDevice) -> Unit,
    onCancel: () -> Unit,
) {
    var kind by remember { mutableStateOf(draft.kind) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
        Column(modifier = Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Upravit zařízení — ${SmartKinds.displayName(kind)}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)

            Text("Typ zařízení",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(SmartKinds.HUE_LIGHT, SmartKinds.HUE_GROUP, SmartKinds.TASMOTA,
                    SmartKinds.TUYA_LOCAL, SmartKinds.WEBHOOK).forEach { k ->
                    KindPill(label = SmartKinds.displayName(k), selected = kind == k,
                        onClick = { kind = k })
                }
            }

            ZhCard(container = MaterialTheme.colorScheme.surfaceVariant) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Aktuální hodnoty",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    DetailRow("Host", draft.host.ifBlank { "—" })
                    DetailRow("Token", if (draft.token.isBlank()) "—" else draft.token.take(6) + "…")
                    DetailRow("Target", draft.target.ifBlank { "—" })
                    DetailRow("Webhook", draft.webhookUrl.ifBlank { "—" })
                }
            }

            Text("Plnohodnotný editor (host / token / target inputs) je dostupný v desktop / mobile " +
                    "appce. Pro TV ostávají hodnoty read-only — TextField na D-pad je nepohodlné.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PsSecondaryButton(text = "Zrušit", onClick = onCancel)
                Box(modifier = Modifier.weight(1f))
                PsPrimaryButton(text = "💾 Uložit", onClick = {
                    onSave(draft.copy(kind = kind))
                })
            }
        }
    }
}

@Composable
private fun KindPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary
                             else MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
                           else MaterialTheme.colorScheme.onSurface,
            focusedContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(end = 12.dp).width(80.dp))
        Text(value,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium)
    }
}

