package com.zeddihub.tv.localsend

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.WifiOff
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
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
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

    ZhPageScaffold {
        PageHeader(
            title = "LocalSend příjem",
            subtitle = "Přijímej soubory z mobilu / desktop přes lokální Wi-Fi.",
            icon = if (running) Icons.Outlined.Wifi else Icons.Outlined.WifiOff,
            trailing = {
                if (running) {
                    PsSecondaryButton(text = "⏹ Vypnout server", onClick = { vm.stop() })
                } else {
                    PsPrimaryButton(text = "▶ Zapnout server", onClick = { vm.start() })
                }
            },
        )

        // Hero status card — running gets a primary-tinted gradient with
        // big address text; stopped is muted.
        ZhCard(
            container = if (running) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.surface,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        if (running) Icons.Outlined.Wifi else Icons.Outlined.WifiOff,
                        null,
                        tint = if (running) Tone.Success else Tone.Muted,
                        modifier = Modifier.size(32.dp),
                    )
                    Text(
                        if (running) "Server běží" else "Server vypnutý",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    StatusPill(
                        label = if (running) "online" else "offline",
                        tone = if (running) Tone.Success else Tone.Muted,
                    )
                    if (running) {
                        StatusPill(
                            label = if (mdns) "mDNS ✓" else "mDNS off",
                            tone = if (mdns) Tone.Info else Tone.Warning,
                        )
                    }
                }

                if (running && addr != null) {
                    Text("Pošli soubor na adresu:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("http://$addr",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                    Text(
                        if (mdns) "TV se objeví automaticky v LocalSend appce na telefonu (mDNS)."
                        else "Otevři LocalSend na telefonu → \"Přidat zařízení ručně\" → tato adresa.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                } else if (running) {
                    Text("Server běží na portu 53317, ale nelze určit IP. Zkontroluj Wi-Fi.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp)
                } else {
                    Text("Klikni \"Zapnout server\" pro spuštění LocalSend příjmu.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp)
                }
            }
        }

        SectionTitle("Přijaté soubory (${received.size})")

        if (received.isEmpty()) {
            EmptyState(
                title = "Zatím nic nepřišlo",
                hint = "Zapni server a pošli soubor z mobilu/desktopu přes LocalSend.",
                icon = Icons.Outlined.Description,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                received.reversed().forEach { f ->
                    ReceivedRow(f)
                }
            }
        }
    }
}

@Composable
private fun ReceivedRow(f: LocalSendServer.ReceivedFile) {
    ZhCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.Description, null,
                tint = Tone.Info,
                modifier = Modifier.size(28.dp),
            )
            Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                Text(f.name,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold)
                Text("${formatBytes(f.size)} · ${formatTime(f.timestamp)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp))
            }
            Text(f.path,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp)
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
