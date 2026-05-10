package com.zeddihub.tv.smarthome.hass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.zeddihub.tv.ui.components.EmptyState
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.PsPrimaryButton
import com.zeddihub.tv.ui.components.PsSecondaryButton
import com.zeddihub.tv.ui.components.SectionTitle
import com.zeddihub.tv.ui.components.StatusPill
import com.zeddihub.tv.ui.components.Tone
import com.zeddihub.tv.ui.components.ZhCard
import com.zeddihub.tv.ui.components.ZhPageScaffold

@Composable
fun HomeAssistantScreen(vm: HomeAssistantViewModel = hiltViewModel()) {
    val baseUrl by vm.baseUrl.collectAsState()
    val token by vm.token.collectAsState()
    val pinged by vm.lastPing.collectAsState()
    val pinned by vm.pinnedEntities.collectAsState()
    val states by vm.statesByEntity.collectAsState()
    val message by vm.message.collectAsState()

    ZhPageScaffold {
        PageHeader(
            title = "Home Assistant",
            subtitle = "Bearer-token integrace s HA. Připnuté entity = quick toggles z TV.",
            icon = Icons.Outlined.Home,
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PsSecondaryButton(text = "Ping", onClick = { vm.ping() })
                    PsPrimaryButton(text = "↻ Refresh", onClick = { vm.refreshStates() })
                }
            },
        )

        // Connection status card
        ZhCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isConfigured = baseUrl.isNotBlank() && token.isNotBlank()
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isConfigured) Tone.Success else Tone.Muted),
                    )
                    Text(
                        if (isConfigured) "Připojeno" else "Nenastaveno",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                    Box(modifier = Modifier.weight(1f))
                    if (pinged != null) StatusPill(label = pinged ?: "—",
                        tone = if ((pinged ?: "").startsWith("✓")) Tone.Success else Tone.Error)
                }
                LabelValue("URL", baseUrl.ifBlank { "(nenastaveno)" })
                LabelValue("Token", if (token.isBlank()) "(nenastaveno)"
                                     else "••••••••${token.takeLast(4)}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)) {
                    PsSecondaryButton(text = "Default LAN URL", onClick = { vm.useDefault() })
                }
            }
        }

        message?.let {
            ZhCard(container = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)) {
                Text(it,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary)
            }
        }

        // Pinned entities
        SectionTitle("Připnuté entity (${pinned.size})")
        if (pinned.isEmpty()) {
            EmptyState(
                title = "Žádné entity připnuté",
                hint = "Po prvním Refresh stavů můžeš entity připnout přes admin panel nebo desktop appku.",
                icon = Icons.Outlined.Home,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            pinned.forEach { eid ->
                EntityCard(
                    entityId = eid,
                    state = states[eid] ?: "?",
                    onToggle = { vm.toggle(eid) },
                    onUnpin = { vm.unpin(eid) },
                )
            }
        }

        // Quick scene/script tester
        SectionTitle("Rychlý test")
        ZhCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Použij ID z HA (např. \"movie_mode\" pro script, \"scene.evening\" pro scénu).",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PsSecondaryButton(
                        text = "▶ script.movie_mode",
                        onClick = { vm.runScript("movie_mode") },
                    )
                    PsSecondaryButton(
                        text = "🎬 scene.evening",
                        onClick = { vm.activateScene("scene.evening") },
                    )
                }
            }
        }
    }
}

@Composable
private fun LabelValue(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp))
        Text(value,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EntityCard(
    entityId: String,
    state: String,
    onToggle: () -> Unit,
    onUnpin: () -> Unit,
) {
    val isOn = state.equals("on", ignoreCase = true) ||
               state.equals("home", ignoreCase = true) ||
               state.equals("playing", ignoreCase = true) ||
               state.equals("open", ignoreCase = true)
    val accent = if (isOn) Tone.Success else Tone.Muted
    val domain = entityId.substringBefore('.', "")
    val emoji = when (domain) {
        "light" -> "💡"
        "switch" -> "🔌"
        "fan" -> "🌀"
        "media_player" -> "🎬"
        "cover" -> "🪟"
        "lock" -> "🔒"
        "climate" -> "🌡"
        "input_boolean" -> "🔘"
        else -> "📡"
    }
    Surface(
        onClick = onToggle,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(14.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = accent.copy(alpha = 0.20f),
            contentColor = MaterialTheme.colorScheme.onBackground,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            // Big emoji + ON/OFF dot
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, fontSize = 28.sp)
            }
            Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                Text(entityId,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accent),
                    )
                    Text(state,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp))
                }
            }
            StatusPill(label = if (isOn) "ON" else "OFF", tone = accent)
            Box(modifier = Modifier.padding(start = 8.dp))
            PsSecondaryButton(text = "Odepnout", onClick = onUnpin)
        }
    }
}
