package com.zeddihub.tv.audio

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Cable
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Speaker
import androidx.compose.material.icons.outlined.SpeakerGroup
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.zeddihub.tv.ui.components.EmptyState
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.SectionTitle
import com.zeddihub.tv.ui.components.StatusPill
import com.zeddihub.tv.ui.components.ZhCard
import com.zeddihub.tv.ui.components.ZhPageScaffold

@Composable
fun AudioScreen(vm: AudioViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val outputs by vm.outputs.collectAsState()
    val volume by vm.volumePct.collectAsState()
    val muted by vm.muted.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    ZhPageScaffold {
        PageHeader(
            title = "Audio výstup",
            subtitle = "Připojené výstupy + rychlé přepnutí.",
            icon = Icons.Outlined.SpeakerGroup,
        )

        // Volume card
        ZhCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.VolumeUp, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp))
                Text("Hlasitost",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 10.dp))
                Text("$volume %",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (muted) com.zeddihub.tv.ui.components.Tone.Muted
                            else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp).weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.setVolume((volume - 10).coerceAtLeast(0)) }) { Text("− 10") }
                    Button(onClick = { vm.setVolume((volume + 10).coerceAtMost(100)) }) { Text("+ 10") }
                    Button(onClick = { vm.toggleMute() }) {
                        Text(if (muted) "Zapnout zvuk" else "Ztlumit")
                    }
                }
            }
        }

        SectionTitle("Připojená zařízení (${outputs.size})")

        if (outputs.isEmpty()) {
            EmptyState(
                title = "Žádné výstupy detekovány",
                hint = "Připoj Bluetooth speaker nebo HDMI ARC zařízení a obnov sekci.",
                icon = Icons.Outlined.SpeakerGroup,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            outputs.forEach { OutputRow(it) }
        }

        // System shortcuts
        Row(modifier = Modifier.padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {
                ctx.startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }) { Text("Bluetooth") }
            Button(onClick = {
                ctx.startActivity(Intent(android.provider.Settings.ACTION_SOUND_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }) { Text("Zvuk v systému") }
        }
    }
}

@Composable
private fun OutputRow(o: AudioOutput) {
    val icon = when (o.type) {
        "speaker" -> Icons.Outlined.Speaker
        "hdmi" -> Icons.Outlined.Cable
        "bluetooth" -> Icons.Outlined.Bluetooth
        "headphones" -> Icons.Outlined.Headphones
        else -> Icons.Outlined.VolumeUp
    }
    val accent = if (o.isCurrent) MaterialTheme.colorScheme.primary
                 else com.zeddihub.tv.ui.components.Tone.Muted
    ZhCard(
        container = if (o.isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                    else MaterialTheme.colorScheme.surface,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(22.dp))
            Text(o.name,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 14.dp).weight(1f))
            if (o.isCurrent) StatusPill("aktivní", tone = MaterialTheme.colorScheme.primary)
        }
    }
}
