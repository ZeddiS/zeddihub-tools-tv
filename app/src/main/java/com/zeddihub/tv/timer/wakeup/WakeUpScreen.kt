package com.zeddihub.tv.timer.wakeup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
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
import com.zeddihub.tv.media.StreamingApps
import com.zeddihub.tv.ui.components.EmptyState
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.PsPrimaryButton
import com.zeddihub.tv.ui.components.PsSecondaryButton
import com.zeddihub.tv.ui.components.Tone
import com.zeddihub.tv.ui.components.ZhPageScaffold

@Composable
fun WakeUpScreen(vm: WakeUpViewModel = hiltViewModel()) {
    val list by vm.wakeups.collectAsState()
    var editing by remember { mutableStateOf<WakeUp?>(null) }

    ZhPageScaffold {
        PageHeader(
            title = "Budík",
            subtitle = "Ráno zapne TV a volitelně spustí aplikaci.",
            icon = Icons.Outlined.Alarm,
            trailing = {
                PsPrimaryButton(text = "+ Přidat", onClick = { editing = newDraft() })
            },
        )

        if (list.isEmpty()) {
            EmptyState(
                title = "Žádný budík",
                hint = "Klikni + Přidat a nastav čas + dny v týdnu, kdy se má TV probudit.",
                icon = Icons.Outlined.Alarm,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            list.forEach { w ->
                WakeUpCard(w,
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
    daysOfWeek = 0x1F,
    launchPackage = null,
    volumePct = 50,
    enabled = true,
)

@Composable
private fun WakeUpCard(w: WakeUp, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val accent = if (w.enabled) MaterialTheme.colorScheme.primary else Tone.Muted
    Surface(
        onClick = onEdit,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (w.enabled) MaterialTheme.colorScheme.surface
                              else MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
            contentColor = MaterialTheme.colorScheme.onBackground,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp)) {
            // Big morning time on the left.
            Text(w.timeStr(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                modifier = Modifier.width(110.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(w.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("${w.daysText()}  ·  hlasitost ${w.volumePct}%  ·  " +
                        (w.launchPackage ?: "ZeddiHub TV"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PsSecondaryButton(
                    text = if (w.enabled) "Vypnout" else "Zapnout",
                    onClick = onToggle,
                )
                PsSecondaryButton(text = "Smazat", onClick = onDelete)
            }
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
        shape = RoundedCornerShape(20.dp),
        colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
        Column(modifier = Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("Upravit budík",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)

            // Time picker
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Čas",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(80.dp))
                StepBtn("Hod −", onClick = { hour = (hour + 23) % 24 })
                Box(modifier = Modifier.width(64.dp), contentAlignment = Alignment.Center) {
                    Text("%02d".format(hour),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }
                StepBtn("Hod +", onClick = { hour = (hour + 1) % 24 })
                Text(":", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                StepBtn("Min −", onClick = { minute = (minute + 55) % 60 })
                Box(modifier = Modifier.width(64.dp), contentAlignment = Alignment.Center) {
                    Text("%02d".format(minute),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }
                StepBtn("Min +", onClick = { minute = (minute + 5) % 60 })
            }

            // Days
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dny",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(80.dp))
                listOf("Po","Út","St","Čt","Pá","So","Ne").forEachIndexed { i, lbl ->
                    Pill(label = lbl,
                        selected = (days shr i) and 1 == 1,
                        onClick = { days = days xor (1 shl i) })
                }
            }

            // Volume
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Hlasitost",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(80.dp))
                listOf(20, 40, 60, 80).forEach { v ->
                    Pill(label = "$v %",
                        selected = volumePct == v,
                        onClick = { volumePct = v })
                }
            }

            // Launch app
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Spustit po probuzení",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val pkgChoices = listOf<Pair<String?, String>>(
                        null to "ZeddiHub TV",
                    ) + StreamingApps.all.take(4).map { it.pkg to it.name }
                    pkgChoices.forEach { (p, name) ->
                        Pill(label = name,
                            selected = pkg == p,
                            onClick = { pkg = p })
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PsSecondaryButton(text = "Zrušit", onClick = onCancel)
                if (onDelete != null) PsSecondaryButton(text = "Smazat", onClick = onDelete)
                Box(modifier = Modifier.weight(1f))
                PsPrimaryButton(text = "💾 Uložit", onClick = {
                    onSave(draft.copy(hour = hour, minute = minute, daysOfWeek = days,
                        volumePct = volumePct, launchPackage = pkg, enabled = true))
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
