package com.zeddihub.tv.timer

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.runtime.Composable
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
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.PsPrimaryButton
import com.zeddihub.tv.ui.components.PsSecondaryButton
import com.zeddihub.tv.ui.components.SectionTitle
import com.zeddihub.tv.ui.components.ZhCard
import com.zeddihub.tv.ui.components.ZhPageScaffold

@Composable
fun TimerScreen(vm: TimerViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val snapshot by vm.snapshot.collectAsState()
    val canDrawOverlays by vm.canDrawOverlays.collectAsState()
    val a11yEnabled by vm.a11yEnabled.collectAsState()

    ZhPageScaffold {
        PageHeader(
            title = "Časovač vypnutí",
            subtitle = "TV se uspí po uplynutí času. Plovoucí odpočet zůstane viditelný i v jiných aplikacích.",
            icon = Icons.Outlined.Bedtime,
        )

        if (!canDrawOverlays) PermissionCard(
            text = "Pro plovoucí odpočet je potřeba povolení \"Zobrazení přes ostatní aplikace\".",
            buttonText = "Udělit povolení",
            onClick = {
                ctx.startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + ctx.packageName))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            },
        )
        if (!a11yEnabled) PermissionCard(
            text = "Pro reakci na dlouhý stisk tlačítka je potřeba zapnout službu přístupnosti.",
            buttonText = "Otevřít nastavení",
            onClick = {
                ctx.startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            },
        )

        StatusCard(snapshot)

        if (snapshot.status == TimerStatus.IDLE) {
            SectionTitle("Vyber dobu")
            // Two rows of large preset cards — much easier to focus on
            // a TV than the previous narrow pill row.
            val presets = listOf(15, 30, 45, 60, 90, 120)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()) {
                presets.take(3).forEach { mins ->
                    PresetCard("$mins min", onClick = { vm.start(ctx, mins * 60_000L) },
                        modifier = Modifier.weight(1f))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()) {
                presets.drop(3).forEach { mins ->
                    PresetCard("$mins min", onClick = { vm.start(ctx, mins * 60_000L) },
                        modifier = Modifier.weight(1f))
                }
            }
        } else {
            SectionTitle("Ovládání")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (snapshot.status == TimerStatus.RUNNING) {
                    PsPrimaryButton(text = "⏸ Pozastavit", onClick = { vm.pause(ctx) })
                } else if (snapshot.status == TimerStatus.PAUSED) {
                    PsPrimaryButton(text = "▶ Pokračovat", onClick = { vm.resume(ctx) })
                }
                PsSecondaryButton(text = "⏹ Zastavit", onClick = { vm.stop(ctx) })
                PsSecondaryButton(text = "⏻ Vypnout teď", onClick = { vm.shutdownNow(ctx) })
            }
        }
    }
}

@Composable
private fun PresetCard(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onBackground,
            focusedContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(label,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PermissionCard(text: String, buttonText: String, onClick: () -> Unit) {
    ZhCard(container = MaterialTheme.colorScheme.error.copy(alpha = 0.10f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f))
            Button(onClick = onClick) { Text(buttonText) }
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
        shape = RoundedCornerShape(20.dp),
        colors = SurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(28.dp)) {
            Text(label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold)
            Text(
                if (snap.status == TimerStatus.IDLE) "—" else formatRemaining(snap.remainingMs),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                lineHeight = 70.sp,
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
