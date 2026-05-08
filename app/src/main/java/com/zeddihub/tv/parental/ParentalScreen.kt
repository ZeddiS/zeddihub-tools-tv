package com.zeddihub.tv.parental

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

@Composable
fun ParentalScreen(vm: ParentalViewModel = hiltViewModel()) {
    val rules by vm.rules.collectAsState()
    val pinIsSet by vm.pinIsSet.collectAsState()
    val message by vm.message.collectAsState()

    var newPin by remember { mutableStateOf("") }
    var pkgInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("Rodičovská kontrola", fontSize = 28.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)
        Text("PIN-locked apps + bedtime hours. Soft-block na launcher tile (UsageStatsManager v0.5+).",
            fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        // PIN section
        Surface(
            shape = RoundedCornerShape(16.dp),
            colors = androidx.tv.material3.SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PIN", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text(if (pinIsSet) "PIN je nastaven" else "Žádný PIN — nastav níže (4-6 číslic).",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0,1,2,3,4,5,6,7,8,9).forEach { d ->
                        Button(onClick = { if (newPin.length < 6) newPin += d.toString() }) { Text(d.toString()) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Vstup: ${"•".repeat(newPin.length)} (${newPin.length}/6)",
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f))
                    Button(onClick = { newPin = "" }) { Text("Smazat") }
                    Button(onClick = {
                        if (newPin.length in 4..6) { vm.setPin(newPin); newPin = "" }
                    }) { Text("Uložit PIN") }
                    if (pinIsSet) Button(onClick = { vm.setPin("") }) { Text("Zrušit PIN") }
                }
            }
        }

        // Rules
        Text("Pravidla (${rules.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))

        if (rules.isEmpty()) {
            Text("Žádné pravidla. Přidej package níže.",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            rules.forEach { rule ->
                RuleRow(
                    rule = rule,
                    onTogglePin = { vm.togglePinRequired(rule) },
                    onBedtime = { vm.setBedtime(rule, 22 * 60, 7 * 60) }, // 22:00 → 07:00
                    onClear = { vm.removeRule(rule.packageName) },
                )
            }
        }

        // Add rule
        Surface(
            shape = RoundedCornerShape(12.dp),
            colors = androidx.tv.material3.SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Přidat pravidlo (vyber preset)", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.addRule("com.netflix.ninja") }) { Text("Netflix") }
                    Button(onClick = { vm.addRule("com.google.android.youtube.tv") }) { Text("YouTube TV") }
                    Button(onClick = { vm.addRule("com.disney.disneyplus") }) { Text("Disney+") }
                    Button(onClick = { vm.addRule("tv.twitch.android.app") }) { Text("Twitch") }
                }
            }
        }

        message?.let {
            Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
private fun RuleRow(
    rule: ParentalRule,
    onTogglePin: () -> Unit,
    onBedtime: () -> Unit,
    onClear: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        colors = androidx.tv.material3.SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(rule.packageName, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                Text("PIN ${if (rule.pinRequired) "✓" else "—"}  ·  Bedtime: ${rule.bedtimeText()}",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onTogglePin) {
                    Text(if (rule.pinRequired) "PIN off" else "PIN on")
                }
                Button(onClick = onBedtime) { Text("22-7") }
                Button(onClick = onClear) { Text("Smazat") }
            }
        }
    }
}
