package com.zeddihub.tv.routine

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
fun RoutineScreen(vm: RoutineViewModel = hiltViewModel()) {
    val steps by vm.steps.collectAsState()
    val running by vm.running.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Bedtime routine", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("Posloupnost akcí pro spaní — jeden press → vše se stane.",
                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = { vm.run() }, enabled = !running) {
                Text(if (running) "Běží…" else "Spustit teď")
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            colors = androidx.tv.material3.SurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Aktuální routina (${steps.size} kroků)", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                if (steps.isEmpty()) {
                    Text("Prázdná. Klikni na Spustit teď — použije se výchozí (fade-out 30s + start timer 30 min).",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                steps.forEachIndexed { idx, s ->
                    StepRow(idx + 1, s)
                }
                Box(modifier = Modifier.padding(top = 12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { vm.resetToDefault() }) { Text("↺ Výchozí") }
                        Button(onClick = { vm.addVolumeFade() }) { Text("+ Fade out") }
                        Button(onClick = { vm.addStartTimer() }) { Text("+ Spustit timer") }
                        Button(onClick = { vm.addDelay() }) { Text("+ Pauza") }
                        Button(onClick = { vm.addWebhook() }) { Text("+ Webhook") }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepRow(idx: Int, s: RoutineStep) {
    val (icon, label) = when (s.kind) {
        RoutineKinds.VOLUME_FADE -> "🔉" to "Ztlumit zvuk na ${s.targetVolumePct}% za ${s.durationSeconds}s"
        RoutineKinds.START_TIMER -> "⏰" to "Spustit Sleep Timer ${s.timerMinutes} min"
        RoutineKinds.DELAY       -> "⏳" to "Počkat ${s.durationSeconds}s"
        RoutineKinds.WEBHOOK     -> "🌐" to "${s.webhookMethod} ${s.webhookUrl.ifBlank { "(žádná URL)" }}"
        else                     -> "❓" to s.kind
    }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text("$idx. $icon", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp,
            modifier = Modifier.padding(end = 8.dp))
        Text(label, color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp,
            modifier = Modifier.weight(1f))
    }
}
