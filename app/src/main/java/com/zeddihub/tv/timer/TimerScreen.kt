package com.zeddihub.tv.timer

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

@Composable
fun TimerScreen(vm: TimerViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val snapshot by vm.snapshot.collectAsState()
    val canDrawOverlays by vm.canDrawOverlays.collectAsState()
    val a11yEnabled by vm.a11yEnabled.collectAsState()
    var customMinutes by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Header()
        if (!canDrawOverlays) PermissionCard(
            text = stringRes(com.zeddihub.tv.R.string.timer_overlay_perm_required),
            buttonText = stringRes(com.zeddihub.tv.R.string.timer_overlay_perm_grant),
            onClick = {
                ctx.startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + ctx.packageName))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        )
        if (!a11yEnabled) PermissionCard(
            text = stringRes(com.zeddihub.tv.R.string.timer_a11y_perm_required),
            buttonText = stringRes(com.zeddihub.tv.R.string.timer_a11y_perm_open),
            onClick = {
                ctx.startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        )

        StatusCard(snapshot)

        if (snapshot.status == TimerStatus.IDLE) {
            PresetGrid(onSelect = { mins -> vm.start(ctx, mins * 60_000L) })
            Row(modifier = Modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Vlastní (min): ", color = MaterialTheme.colorScheme.onBackground)
                // Using simple Text input via remembering minutes string
                CustomMinutesPills(onPick = { mins -> vm.start(ctx, mins * 60_000L) })
            }
        } else {
            ActiveControls(snapshot, vm)
        }
    }
}

@Composable
private fun Header() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .padding(),
        ) { Icon(Icons.Outlined.Bedtime, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) }
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(stringRes(com.zeddihub.tv.R.string.timer_title), fontSize = 28.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)
            Text(stringRes(com.zeddihub.tv.R.string.timer_subtitle), fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PermissionCard(text: String, buttonText: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
            focusedContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.24f),
            contentColor = MaterialTheme.colorScheme.onBackground,
        ),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Text(text, modifier = Modifier.weight(1f), fontSize = 14.sp)
            Text(buttonText, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp,
                modifier = Modifier.padding(start = 12.dp))
        }
    }
}

@Composable
private fun StatusCard(snap: TimerSnapshot) {
    val label = when (snap.status) {
        TimerStatus.IDLE -> "Připraveno"
        TimerStatus.RUNNING -> "Běží"
        TimerStatus.PAUSED -> "Pozastaveno"
        TimerStatus.EXPIRED -> "Hotovo"
    }
    Surface(
        shape = androidx.tv.material3.SurfaceDefaults.shape(shape = RoundedCornerShape(16.dp)),
        colors = androidx.tv.material3.SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Text(
                if (snap.status == TimerStatus.IDLE) "—" else formatRemaining(snap.remainingMs),
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            if (snap.status == TimerStatus.RUNNING || snap.status == TimerStatus.PAUSED) {
                val pct = if (snap.totalMs > 0) (snap.remainingMs.toFloat() / snap.totalMs) else 0f
                ProgressBar(pct)
            }
        }
    }
}

@Composable
private fun ProgressBar(fraction: Float) {
    androidx.compose.material3.LinearProgressIndicator(
        progress = { fraction.coerceIn(0f, 1f) },
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Composable
private fun PresetGrid(onSelect: (Int) -> Unit) {
    val presets = listOf(15, 30, 45, 60, 90, 120)
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        presets.forEach { mins ->
            PresetChip("$mins min", onClick = { onSelect(mins) })
        }
    }
}

@Composable
private fun CustomMinutesPills(onPick: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(start = 8.dp)) {
        listOf(5, 10, 20, 180, 240).forEach { m ->
            PresetChip("$m", onClick = { onPick(m) })
        }
    }
}

@Composable
private fun PresetChip(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onSurface,
            focusedContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = Modifier.width(120.dp),
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            fontSize = 16.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun ActiveControls(snap: TimerSnapshot, vm: TimerViewModel) {
    val ctx = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (snap.status == TimerStatus.RUNNING) {
            Button(onClick = { vm.pause(ctx) }) { Text(stringRes(com.zeddihub.tv.R.string.timer_pause)) }
        } else if (snap.status == TimerStatus.PAUSED) {
            Button(onClick = { vm.resume(ctx) }) { Text(stringRes(com.zeddihub.tv.R.string.timer_resume)) }
        }
        Button(onClick = { vm.stop(ctx) }) { Text(stringRes(com.zeddihub.tv.R.string.timer_stop)) }
        Button(onClick = { vm.shutdownNow(ctx) }) { Text(stringRes(com.zeddihub.tv.R.string.timer_shutdown_now)) }
    }
}

@Composable
private fun stringRes(id: Int): String = androidx.compose.ui.res.stringResource(id)
