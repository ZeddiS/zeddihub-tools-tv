package com.zeddihub.tv.audio

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Cable
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Speaker
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
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

@Composable
fun AudioScreen(vm: AudioViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val outputs by vm.outputs.collectAsState()
    val volume by vm.volumePct.collectAsState()
    val muted by vm.muted.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("Audio výstup", fontSize = 28.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)
        Text("Připojené výstupy + rychlé přepnutí.", fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Volume control card
        Surface(
            shape = RoundedCornerShape(16.dp),
            colors = androidx.tv.material3.SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(20.dp)) {
                Icon(Icons.Outlined.VolumeUp, null, tint = MaterialTheme.colorScheme.primary)
                Text("Hlasitost", color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 12.dp))
                Text("$volume %", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp).weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.setVolume((volume - 10).coerceAtLeast(0)) }) { Text("- 10") }
                    Button(onClick = { vm.setVolume((volume + 10).coerceAtMost(100)) }) { Text("+ 10") }
                    Button(onClick = { vm.toggleMute() }) {
                        Text(if (muted) "Zapnout zvuk" else "Ztlumit")
                    }
                }
            }
        }

        // Output list
        Text("Připojená zařízení (${outputs.size})",
            fontSize = 16.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (outputs.isEmpty()) {
                Text("Žádné výstupy detekovány.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
            outputs.forEach { out -> OutputRow(out) }
        }

        // System shortcuts — Android TV doesn't let us programmatically
        // switch the default output, so we route the user to the right
        // settings page for their use case.
        Row(modifier = Modifier.padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                ctx.startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }) { Text("Bluetooth nastavení") }
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
    val borderColor = if (o.isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    Surface(
        shape = RoundedCornerShape(12.dp),
        colors = androidx.tv.material3.SurfaceDefaults.colors(
            containerColor = if (o.isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = borderColor)
            Box(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(o.name, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
            }
            if (o.isCurrent) {
                Text("AKTIVNÍ", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
