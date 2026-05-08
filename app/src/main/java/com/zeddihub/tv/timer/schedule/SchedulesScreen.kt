package com.zeddihub.tv.timer.schedule

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
fun SchedulesScreen(vm: SchedulesViewModel = hiltViewModel()) {
    val list by vm.schedules.collectAsState()
    var editing by remember { mutableStateOf<Schedule?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Plán časovače", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("Opakovaný sleep timer podle dnů v týdnu.",
                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = { editing = newDraft() }) { Text("+ Přidat") }
        }

        Column(modifier = Modifier.padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (list.isEmpty()) {
                Text("Žádné plány. Použij tlačítko + Přidat nahoře.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
            list.forEach { s ->
                ScheduleRow(s,
                    onToggle = { vm.toggle(s.id) },
                    onEdit = { editing = s },
                    onDelete = { vm.delete(s.id) })
            }
        }
    }

    editing?.let { draft ->
        ScheduleEditor(
            draft,
            onSave = { vm.upsert(it); editing = null },
            onCancel = { editing = null },
            onDelete = if (list.any { it.id == draft.id }) ({ vm.delete(draft.id); editing = null }) else null,
        )
    }
}

private fun newDraft() = Schedule(
    id = ScheduleStore.newId(),
    name = "Nový plán",
    hour = 23, minute = 0, durationMinutes = 30,
    daysOfWeek = 0x1F, // Po-Pá
    enabled = true,
)

@Composable
private fun ScheduleRow(s: Schedule, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Surface(
        onClick = onEdit,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (s.enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(20.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(s.name, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("${s.timeStr()} · ${s.daysText()} · ${s.durationMinutes} min",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onToggle) {
                Text(if (s.enabled) "Vypnout" else "Zapnout")
            }
            Box(modifier = Modifier.width(8.dp))
            Button(onClick = onDelete) { Text("Smazat") }
        }
    }
}

@Composable
private fun ScheduleEditor(
    draft: Schedule,
    onSave: (Schedule) -> Unit,
    onCancel: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    var name by remember { mutableStateOf(draft.name) }
    var hour by remember { mutableStateOf(draft.hour) }
    var minute by remember { mutableStateOf(draft.minute) }
    var dur by remember { mutableStateOf(draft.durationMinutes) }
    var days by remember { mutableStateOf(draft.daysOfWeek) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        colors = androidx.tv.material3.SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Upravit plán", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)

            // Time pickers — D-pad ± buttons since TextField on TV is awkward
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Čas: ", color = MaterialTheme.colorScheme.onBackground)
                StepButton("Hod -", onClick = { hour = (hour + 23) % 24 })
                Text("%02d".format(hour), fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                StepButton("Hod +", onClick = { hour = (hour + 1) % 24 })
                Box(modifier = Modifier.width(16.dp))
                StepButton("Min -", onClick = { minute = (minute + 55) % 60 }) // step 5
                Text("%02d".format(minute), fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                StepButton("Min +", onClick = { minute = (minute + 5) % 60 })
            }

            // Duration
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Trvání: ", color = MaterialTheme.colorScheme.onBackground)
                listOf(15, 30, 45, 60, 90, 120).forEach { m ->
                    val sel = dur == m
                    Surface(
                        onClick = { dur = m },
                        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.primary,
                            contentColor = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            focusedContentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text("$m min", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 13.sp)
                    }
                }
            }

            // Days of week
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dny: ", color = MaterialTheme.colorScheme.onBackground)
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

            // Day presets
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { days = 0x1F }) { Text("Po-Pá") }
                Button(onClick = { days = 0x60 }) { Text("So-Ne") }
                Button(onClick = { days = 0x7F }) { Text("Každý den") }
            }

            // Action row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onCancel) { Text("Zrušit") }
                if (onDelete != null) Button(onClick = onDelete) { Text("Smazat") }
                Box(modifier = Modifier.weight(1f))
                Button(onClick = {
                    onSave(draft.copy(name = name, hour = hour, minute = minute,
                        durationMinutes = dur, daysOfWeek = days, enabled = true))
                }) { Text("Uložit") }
            }
        }
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick) { Text(label, fontSize = 12.sp) }
}
