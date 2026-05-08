package com.zeddihub.tv.dashboard

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.zeddihub.tv.media.LaunchableApp
import com.zeddihub.tv.media.StreamingApps
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(vm: DashboardViewModel = hiltViewModel()) {
    val now by vm.now.collectAsState()
    val sysInfo by vm.sysInfo.collectAsState()
    val weather by vm.weather.collectAsState()
    val ctx = LocalContext.current

    LaunchedEffect(Unit) { vm.refresh(ctx) }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        // Big clock + date
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(1f)) {
                Text(now.timeStr, fontSize = 96.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text(now.dateStr, fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            WeatherCard(weather)
        }

        // System info row
        Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoTile(Icons.Outlined.Memory, "RAM", sysInfo.ramText, modifier = Modifier.weight(1f))
            InfoTile(Icons.Outlined.Storage, "Úložiště", sysInfo.storageText, modifier = Modifier.weight(1f))
            InfoTile(Icons.Outlined.Wifi, "Síť", sysInfo.networkText, modifier = Modifier.weight(1f))
        }

        // Quick launch streaming apps
        Text("Spustit aplikaci", fontSize = 18.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 32.dp, bottom = 12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StreamingApps.all.take(6).forEach { app ->
                LaunchTile(app)
            }
        }
    }
}

@Composable
private fun WeatherCard(w: WeatherInfo) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        colors = androidx.tv.material3.SurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.padding(start = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(20.dp)) {
            Icon(Icons.Outlined.WbSunny, null, tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(40.dp))
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(w.tempText, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text(w.label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun InfoTile(icon: ImageVector, title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        colors = androidx.tv.material3.SurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Text(title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp))
            }
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun LaunchTile(app: LaunchableApp) {
    val ctx = LocalContext.current
    Surface(
        onClick = {
            val pm = ctx.packageManager
            val intent = pm.getLaunchIntentForPackage(app.pkg)
            if (intent != null) {
                ctx.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        },
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = app.tintColor.copy(alpha = 0.3f),
        ),
        modifier = Modifier.width(140.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp),
        ) {
            Icon(Icons.Outlined.PlayCircle, null, tint = app.tintColor, modifier = Modifier.size(40.dp))
            Text(app.name, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onBackground)
        }
    }
}
