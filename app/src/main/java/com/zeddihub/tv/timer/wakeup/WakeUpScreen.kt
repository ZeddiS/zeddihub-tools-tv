package com.zeddihub.tv.timer.wakeup

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
import com.zeddihub.tv.media.StreamingApps

@Composable
fun WakeUpScreen(vm: WakeUpViewModel = hiltViewModel()) {
    val list by vm.wakeups.collectAsState()
    var editing by remember { mutableStateOf<WakeUp?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Budík", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("Ráno zapne TV a volitelně spustí aplikaci.",
                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = { editing = newDraft() }) { Text("+ Přidat") }
        }

        Column(modifier = Modifier.padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (list.isEmpty()) {
                Text("Žádný budík.", fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            list.forEach { w ->
                WakeUpRow(w,
                    onToggle = { vm.toggle(w.id) },
                    onEdit = { editing = w },
                    onDelete = { vm.delete(w.id) })
            }
        }
    }

    editing?.let { draft ->
        WakeUpEditor(
            draft,
            onSave = { vm.upsert(it); editing = null },
            onCancel = { editing = null },
            onDelete = if (list.any { it.id == draft.id }) ({ vm.delete(draft.id); editing = null }) else null,
        )
    }
}

private fun newDraft() = WakeUp(
    id = WakeUpStore.newId(),
    name = "Ranní budík",
    hour = 7, minute = 0,
    daysOfWeek = 0x1F, // Po-Pá
    launchPackage = null,
    volumePct = 50,
    enabled = true,
)

@Composable
private fun WakeUpRow(w: WakeUp, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Surface(
        onClick = onEdit,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (w.enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(20.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(w.name, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("${w.timeStr()} · ${w.daysText()} · hlasitost ${w.volumePct}% · ${w.launchPackage ?: "MainActivity"}",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onToggle) { Text(if (w.enabled) "Vypnout" else "Zapnout") }
            Box(modifier = Modifier.width(8.dp))
            Button(onClick = onDelete) { Text("Smazat") }
        }
    }
}

@Composable
private fun WakeUpEditor(
    draft: WakeUp, onSave: (WakeUp) -> Unit, onCancel: () -> Unit, onDelete: (() -> Unit)?,
) {
    var hour by remember { mutableStateOf(draft.hour) }
    var minute by remember { mutableStateOf(draft.minute) }
    var days by remember { mutableStateOf(draft.daysOfWeek) }
    var volumePct by remember { mutableStateOf(draft.volumePct) }
    var pkg by remember { mutableStateOf(draft.launchPackage) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        colors = androidx.tv.material3.SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Upravit budík", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Čas: ", color = MaterialTheme.colorScheme.onBackground)
                Button(onClick = { hour = (hour + 23) % 24 }) { Text("Hod -") }
                Text("%02d".format(hour), fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                Button(onClick = { hour = (hour + 1) % 24 }) { Text("Hod +") }
                Box(modifier = Modifier.width(16.dp))
                Button(onClick = { minute = (minute + 55) % 60 }) { Text("Min -") }
                Text("%02d".format(minute), fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                Button(onClick = { minute = (minute + 5) % 60 }) { Text("Min +") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val labels = listOf("Po","Út","St","Čt","Pá","So","Ne")
                labels.forEachIndexed { i, lbl ->
                    val sel = (days shr i) and 1 == 1
                    Surface(
                        onClick = { days = days xor (1 shl i) },
                        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.primary,
                            contentColor = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            focusedContentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(lbl, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 13.sp)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Hlasitost: $volumePct%", color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(end = 8.dp))
                listOf(20, 40, 60, 80).forEach { v ->
                    Button(onClick = { volumePct = v }) { Text("$v%") }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Spustit: ", color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(end = 8.dp))
                val pkgChoices = listOf<Pair<String?, String>>(
                    null to "Jen ZeddiHub",
                ) + StreamingApps.all.take(4).map { it.pkg to it.name }
                pkgChoices.forEach { (p, name) ->
                    val sel = pkg == p
                    Surface(
                        onClick = { pkg = p },
                        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.primary,
                            contentColor = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            focusedContentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(name, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onCancel) { Text("Zrušit") }
                if (onDelete != null) Button(onClick = onDelete) { Text("Smazat") }
                Box(modifier = Modifier.weight(1f))
                Button(onClick = {
                    onSave(draft.copy(hour = hour, minute = minute, daysOfWeek = days,
                        volumePct = volumePct, launchPackage = pkg, enabled = true))
                }) { Text("Uložit") }
            }
        }
    }
}
