package com.zeddihub.tv.media

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

@Composable
fun MediaScreen() {
    val ctx = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("Média", fontSize = 28.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp))
        Text("Spustit streamovací aplikaci nebo otevřít remote pro Plex/Kodi.",
            fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(StreamingApps.all) { app ->
                AppCard(app)
            }
        }
    }
}

@Composable
private fun AppCard(app: LaunchableApp) {
    val ctx = LocalContext.current
    Surface(
        onClick = {
            val intent = ctx.packageManager.getLaunchIntentForPackage(app.pkg)
            if (intent != null) ctx.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        },
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = app.tintColor.copy(alpha = 0.4f),
        ),
        modifier = Modifier.width(180.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Icon(Icons.Outlined.PlayCircle, null, tint = app.tintColor, modifier = Modifier.size(56.dp))
            Text(app.name, fontSize = 16.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 12.dp),
                color = MaterialTheme.colorScheme.onBackground)
        }
    }
}
