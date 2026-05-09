package com.zeddihub.tv.dashboard

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.zeddihub.tv.media.LaunchableApp
import com.zeddihub.tv.media.StreamingApps
import com.zeddihub.tv.ui.components.PsBigTile
import com.zeddihub.tv.ui.components.SectionTitle
import com.zeddihub.tv.ui.components.ZhCard
import com.zeddihub.tv.ui.components.ZhPageScaffold

@Composable
fun DashboardScreen(vm: DashboardViewModel = hiltViewModel()) {
    val now by vm.now.collectAsState()
    val sysInfo by vm.sysInfo.collectAsState()
    val weather by vm.weather.collectAsState()
    val ctx = LocalContext.current

    LaunchedEffect(Unit) { vm.refresh(ctx) }

    ZhPageScaffold {
        // ── Hero clock card ──────────────────────────────────────────
        // Single full-width card — instead of bare 88sp text floating in
        // empty space (which looked like a placeholder), the clock now
        // sits inside a gradient surface that anchors the screen and
        // gives the page a clear focal point on first open.
        HeroClockCard(now, weather)

        // ── System info row ──────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoTile(Icons.Outlined.Memory, "RAM", sysInfo.ramText, modifier = Modifier.weight(1f))
            InfoTile(Icons.Outlined.Storage, "Úložiště", sysInfo.storageText, modifier = Modifier.weight(1f))
            InfoTile(Icons.Outlined.Wifi, "Síť", sysInfo.networkText, modifier = Modifier.weight(1f))
        }

        // ── Quick actions row ────────────────────────────────────────
        SectionTitle("Rychlé akce")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PsBigTile(
                title = "Sleep Timer",
                icon = Icons.Outlined.Bedtime,
                accent = MaterialTheme.colorScheme.primary,
                onClick = { /* nav handled by side rail; placeholder no-op */ },
                modifier = Modifier.width(160.dp),
            )
            PsBigTile(
                title = "Plán",
                icon = Icons.Outlined.CalendarMonth,
                accent = MaterialTheme.colorScheme.primary,
                onClick = { },
                modifier = Modifier.width(160.dp),
            )
            PsBigTile(
                title = "Bedtime",
                icon = Icons.Outlined.NightsStay,
                accent = MaterialTheme.colorScheme.primary,
                onClick = { },
                modifier = Modifier.width(160.dp),
            )
            PsBigTile(
                title = "Stav TV",
                icon = Icons.Outlined.MonitorHeart,
                accent = MaterialTheme.colorScheme.primary,
                onClick = { },
                modifier = Modifier.width(160.dp),
            )
        }

        // ── Streaming apps ───────────────────────────────────────────
        SectionTitle("Spustit aplikaci")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StreamingApps.all.take(6).forEach { app ->
                PsBigTile(
                    title = app.name,
                    icon = Icons.Outlined.PlayCircle,
                    accent = app.tintColor,
                    onClick = {
                        val intent = ctx.packageManager.getLaunchIntentForPackage(app.pkg)
                        if (intent != null) {
                            ctx.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                    },
                    modifier = Modifier.width(140.dp),
                )
            }
        }
    }
}

/**
 * Big hero clock card. Gradient background tied to the brand orange so
 * the screen has identity on first open; weather sits as a chip in the
 * top-right corner so it's seen but doesn't compete with the time.
 */
@Composable
private fun HeroClockCard(now: NowText, weather: WeatherInfo) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                    ),
                ),
            ),
    ) {
        // Weather chip (top-right)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp),
        ) {
            Icon(
                Icons.Outlined.WbSunny, null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(28.dp),
            )
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(
                    weather.tempText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    weather.label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        // Clock + date (bottom-left)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(28.dp),
        ) {
            Text(
                now.timeStr,
                fontSize = 92.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 96.sp,
            )
            Text(
                now.dateStr,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun InfoTile(icon: ImageVector, title: String, value: String,
                     modifier: Modifier = Modifier) {
    ZhCard(modifier = modifier) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp))
                Text(title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp))
            }
            Text(value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 6.dp))
        }
    }
}
