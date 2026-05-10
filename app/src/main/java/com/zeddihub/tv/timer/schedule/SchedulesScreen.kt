package com.zeddihub.tv.timer.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.zeddihub.tv.ui.components.EmptyState
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.PsPrimaryButton
import com.zeddihub.tv.ui.components.PsSecondaryButton
import com.zeddihub.tv.ui.components.Tone
import com.zeddihub.tv.ui.components.ZhPageScaffold

@Composable
fun SchedulesScreen(vm: SchedulesViewModel = hiltViewModel()) {
    val list by vm.schedules.collectAsState()
    var editing by remember { mutableStateOf<Schedule?>(null) }

    ZhPageScaffold {
        PageHeader(
            title = "Plán časovače",
            subtitle = "Opakovaný sleep timer podle dnů v týdnu.",
            icon = Icons.Outlined.CalendarMonth,
            trailing = {
                PsPrimaryButton(text = "+ Přidat", onClick = { editing = newDraft() })
            },
        )

        if (list.isEmpty()) {
            EmptyState(
                title = "Žádné plány",
                hint = "Použij tlačítko + Přidat nahoře a nastav časovač který se opakuje.",
                icon = Icons.Outlined.Schedule,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
    val accent = if (s.enabled) MaterialTheme.colorScheme.primary else Tone.Muted
    Surface(
        onClick = onEdit,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (s.enabled) MaterialTheme.colorScheme.surface
                              else MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
            contentColor = MaterialTheme.colorScheme.onBackground,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp)) {
            // Big time display on the left — instant at-a-glance read.
            Text(s.timeStr(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                modifier = Modifier.width(110.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(s.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("${s.daysText()}  ·  ${s.durationMinutes} min spánku",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PsSecondaryButton(
                    text = if (s.enabled) "Vypnout" else "Zapnout",
                    onClick = onToggle,
                )
                PsSecondaryButton(text = "Smazat", onClick = onDelete)
            }
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
    var hour by remember { mutableStateOf(draft.hour) }
    var minute by remember { mutableStateOf(draft.minute) }
    var dur by remember { mutableStateOf(draft.durationMinutes) }
    var days by remember { mutableStateOf(draft.daysOfWeek) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
        Column(modifier = Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("Upravit plán",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)

            // Time picker — D-pad ± buttons in a balanced row.
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Čas",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(80.dp))
                StepBtn("Hod −", onClick = { hour = (hour + 23) % 24 })
                Box(
                    modifier = Modifier.width(64.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("%02d".format(hour),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }
                StepBtn("Hod +", onClick = { hour = (hour + 1) % 24 })
                Text(":", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                StepBtn("Min −", onClick = { minute = (minute + 55) % 60 })
                Box(
                    modifier = Modifier.width(64.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("%02d".format(minute),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }
                StepBtn("Min +", onClick = { minute = (minute + 5) % 60 })
            }

            // Duration choice
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Trvání",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(80.dp))
                listOf(15, 30, 45, 60, 90, 120).forEach { m ->
                    Pill(label = "$m min", selected = dur == m, onClick = { dur = m })
                }
            }

            // Days
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dny",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(80.dp))
                listOf("Po","Út","St","Čt","Pá","So","Ne").forEachIndexed { i, lbl ->
                    Pill(
                        label = lbl,
                        selected = (days shr i) and 1 == 1,
                        onClick = { days = days xor (1 shl i) },
                    )
                }
            }

            // Day presets
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Předvolby",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(80.dp))
                Pill(label = "Po-Pá", selected = days == 0x1F, onClick = { days = 0x1F })
                Pill(label = "So-Ne", selected = days == 0x60, onClick = { days = 0x60 })
                Pill(label = "Každý den", selected = days == 0x7F, onClick = { days = 0x7F })
            }

            // Action row
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PsSecondaryButton(text = "Zrušit", onClick = onCancel)
                if (onDelete != null) PsSecondaryButton(text = "Smazat", onClick = onDelete)
                Box(modifier = Modifier.weight(1f))
                PsPrimaryButton(text = "💾 Uložit", onClick = {
                    onSave(draft.copy(hour = hour, minute = minute,
                        durationMinutes = dur, daysOfWeek = days, enabled = true))
                })
            }
        }
    }
}

@Composable
private fun Pill(label: String, selected: Boolean, onClick: () -> Unit) {
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
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun StepBtn(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onSurface,
            focusedContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 11.sp)
    }
}
