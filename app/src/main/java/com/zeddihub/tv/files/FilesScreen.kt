package com.zeddihub.tv.files

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

/**
 * File explorer. Three sources today:
 *   1. Local — internal & external (USB / SD) Documents-style browse
 *   2. LocalSend — files received via the LocalSend HTTP receiver
 *   3. SMB — placeholder; clicking shows a "coming in v0.5" hint
 *
 * Cloud (Google Drive / Dropbox) is in the user's wishlist but deferred —
 * it requires an OAuth library + per-provider client SDK and would dwarf
 * the other sources in implementation cost.
 *
 * Open behavior: tap a file → sends ACTION_VIEW with a content:// URI
 * (FileProvider) and the matching MIME type, so any installed media app
 * can render it. Falls back to a flash if nothing handles it.
 */
@Composable
fun FilesScreen(vm: FilesViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val source by vm.source.collectAsState()
    val items by vm.items.collectAsState()
    val pathLabel by vm.currentPathLabel.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Soubory", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text(pathLabel, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (vm.canGoUp()) Button(onClick = { vm.goUp() }) { Text("◂ Zpět") }
        }

        // Source tabs
        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SourceChip("Lokální", FileSource.LOCAL, source) { vm.switchSource(FileSource.LOCAL) }
            SourceChip("USB / SD", FileSource.EXTERNAL, source) { vm.switchSource(FileSource.EXTERNAL) }
            SourceChip("LocalSend", FileSource.LOCALSEND, source) { vm.switchSource(FileSource.LOCALSEND) }
            SourceChip("SMB / NAS", FileSource.SMB, source) { vm.switchSource(FileSource.SMB) }
        }

        // File list
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (items.isEmpty()) {
                item {
                    Text(
                        when (source) {
                            FileSource.LOCALSEND -> "Žádné přijaté soubory. Pošli něco z mobilu přes LocalSend."
                            FileSource.SMB -> "SMB / NAS browser je v plánu na v0.5+. Zatím použij dedikované SMB apps z Play Store."
                            else -> "Prázdná složka."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
            }
            items(items.size) { idx ->
                val it = items[idx]
                FileRow(it, onOpen = { vm.openItem(ctx, it) })
            }
        }
    }
}

@Composable
private fun SourceChip(label: String, kind: FileSource, current: FileSource, onClick: () -> Unit) {
    val sel = kind == current
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (sel) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
            contentColor = if (sel) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onBackground,
        ),
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun FileRow(item: FileItem, onOpen: () -> Unit) {
    val (icon, tint) = iconFor(item)
    Surface(
        onClick = onOpen,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(10.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                Text(item.name, fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = if (item.isDirectory) FontWeight.Bold else FontWeight.Normal)
                if (!item.isDirectory) {
                    Text(humanSize(item.sizeBytes),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun iconFor(item: FileItem): Pair<ImageVector, androidx.compose.ui.graphics.Color> {
    if (item.isDirectory) return Icons.Outlined.Folder to androidx.compose.ui.graphics.Color(0xFFF39200)
    val ext = item.name.substringAfterLast('.', "").lowercase()
    return when (ext) {
        in setOf("jpg","jpeg","png","gif","webp","heic","bmp")
             -> Icons.Outlined.Image to androidx.compose.ui.graphics.Color(0xFF22C55E)
        in setOf("mp4","mkv","webm","mov","avi","m4v","wmv")
             -> Icons.Outlined.Movie to androidx.compose.ui.graphics.Color(0xFFEF4444)
        in setOf("mp3","wav","flac","ogg","aac","m4a","opus")
             -> Icons.Outlined.AudioFile to androidx.compose.ui.graphics.Color(0xFFA78BFA)
        in setOf("txt","md","pdf","doc","docx","odt")
             -> Icons.Outlined.Description to androidx.compose.ui.graphics.Color(0xFF3B82F6)
        else -> Icons.Outlined.InsertDriveFile to androidx.compose.ui.graphics.Color(0xFFA0A0B8)
    }
}

private fun humanSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024L * 1024 -> "%.1f KB".format(bytes / 1024.0)
    bytes < 1024L * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
    else -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
}
