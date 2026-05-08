package com.zeddihub.tv.watchlater

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.tv.material3.Text

@Composable
fun WatchLaterScreen(vm: WatchLaterViewModel = hiltViewModel()) {
    val items by vm.items.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val ctx = LocalContext.current

    LaunchedEffect(Unit) { vm.refresh() }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Watch later", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("Sdílená fronta odkazů ze všech ZeddiHub klientů (mobil, desktop).",
                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = { vm.refresh() }, enabled = !loading) {
                Text(if (loading) "Aktualizuji…" else "Aktualizovat")
            }
        }

        error?.let {
            Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp))
        }

        Column(modifier = Modifier.padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (items.isEmpty() && !loading) {
                Text("Fronta je prázdná. Pošli odkaz z mobilní/desktop appky.",
                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items.forEach { item ->
                ItemRow(
                    item = item,
                    onOpen = {
                        runCatching {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            ctx.startActivity(intent)
                        }
                        vm.markWatched(item.id)
                    },
                    onMarkWatched = { vm.markWatched(item.id) },
                )
            }
        }
    }
}

@Composable
private fun ItemRow(
    item: WatchLaterItem,
    onOpen: () -> Unit,
    onMarkWatched: () -> Unit,
) {
    val sourceIcon = when (item.source?.lowercase()) {
        "youtube"   -> "▶️"
        "plex"      -> "🎬"
        "netflix"   -> "🎬"
        "twitch"    -> "📺"
        "spotify"   -> "🎵"
        else        -> "🔗"
    }
    Surface(
        onClick = onOpen,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (item.watched) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(20.dp)) {
            Text(sourceIcon, fontSize = 28.sp, modifier = Modifier.padding(end = 16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = if (item.watched) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onBackground)
                Text(item.url, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!item.watched) {
                Button(onClick = onMarkWatched) { Text("Označit zhlédnuté") }
            } else {
                Text("✓ zhlédnuto", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            }
        }
    }
}
