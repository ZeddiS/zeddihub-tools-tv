package com.zeddihub.tv.watchlater

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.zeddihub.tv.ui.components.EmptyState
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.PsPrimaryButton
import com.zeddihub.tv.ui.components.PsSecondaryButton
import com.zeddihub.tv.ui.components.StatusPill
import com.zeddihub.tv.ui.components.Tone
import com.zeddihub.tv.ui.components.ZhPageScaffold
import java.net.URI

@Composable
fun WatchLaterScreen(vm: WatchLaterViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val items by vm.items.collectAsState()
    val loading by vm.loading.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    ZhPageScaffold {
        PageHeader(
            title = "Watch Later",
            subtitle = "Sdílená fronta odkazů z mobile / desktop. Klikni → otevře se v cílové appce.",
            icon = Icons.Outlined.Bookmark,
            trailing = {
                PsPrimaryButton(
                    text = if (loading) "Načítám…" else "↻ Refresh",
                    onClick = { vm.refresh() },
                )
            },
        )

        if (items.isEmpty() && !loading) {
            EmptyState(
                title = "Prázdná fronta",
                hint = "Pošli odkaz z mobile aplikace nebo přes API. Naplnit se může i z desktop appky.",
                icon = Icons.Outlined.Bookmark,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.forEach { item ->
                WatchItemCard(
                    item = item,
                    onOpen = {
                        runCatching {
                            ctx.startActivity(
                                Intent(Intent.ACTION_VIEW, android.net.Uri.parse(item.url))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    },
                    onMarkWatched = { vm.markWatched(item.id) },
                )
            }
        }
    }
}

@Composable
private fun WatchItemCard(
    item: WatchLaterItem,
    onOpen: () -> Unit,
    onMarkWatched: () -> Unit,
) {
    val accent = sourceAccent(item.source)
    val sourceLabel = sourceLabel(item.source, item.url)
    Surface(
        onClick = onOpen,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (item.watched) MaterialTheme.colorScheme.surfaceVariant
                              else MaterialTheme.colorScheme.surface,
            focusedContainerColor = accent.copy(alpha = 0.20f),
            contentColor = MaterialTheme.colorScheme.onBackground,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp)) {
            // Source pill
            StatusPill(label = sourceLabel, tone = accent,
                modifier = Modifier.size(width = 90.dp, height = 22.dp))
            Column(modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)) {
                Text(item.title.ifBlank { item.url },
                    fontSize = 16.sp,
                    fontWeight = if (item.watched) FontWeight.Normal else FontWeight.Bold,
                    color = if (item.watched) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onBackground,
                    maxLines = 1)
                Text(item.url,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1)
            }
            if (!item.watched) {
                PsSecondaryButton(
                    text = "✓ Označit",
                    onClick = onMarkWatched,
                )
            } else {
                StatusPill(label = "shlédnuto", tone = Tone.Muted)
            }
        }
    }
}

private fun sourceAccent(source: String?): Color = when (source?.lowercase()) {
    "youtube" -> Color(0xFFFF0000)
    "plex" -> Color(0xFFE5A00D)
    "netflix" -> Color(0xFFE50914)
    "twitch" -> Color(0xFF9146FF)
    "spotify" -> Color(0xFF1DB954)
    else -> Tone.Info
}

private fun sourceLabel(source: String?, url: String): String {
    if (!source.isNullOrBlank()) return source.uppercase()
    return runCatching { URI(url).host?.removePrefix("www.")?.uppercase() ?: "LINK" }
        .getOrDefault("LINK")
}
