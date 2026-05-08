package com.zeddihub.tv.files

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.zeddihub.tv.ui.components.EmptyState
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.Tone
import com.zeddihub.tv.ui.components.ZhPageScaffold

@Composable
fun FilesScreen(vm: FilesViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val source by vm.source.collectAsState()
    val items by vm.items.collectAsState()
    val pathLabel by vm.currentPathLabel.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    ZhPageScaffold {
        PageHeader(
            title = "Soubory",
            subtitle = pathLabel,
            icon = Icons.Outlined.Folder,
            trailing = {
                if (vm.canGoUp()) Button(onClick = { vm.goUp() }) { Text("◂ Zpět") }
            },
        )

        // Source tabs
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()) {
            SourceChip("Lokální", FileSource.LOCAL, source) { vm.switchSource(FileSource.LOCAL) }
            SourceChip("USB / SD", FileSource.EXTERNAL, source) { vm.switchSource(FileSource.EXTERNAL) }
            SourceChip("LocalSend", FileSource.LOCALSEND, source) { vm.switchSource(FileSource.LOCALSEND) }
            SourceChip("SMB / NAS", FileSource.SMB, source) { vm.switchSource(FileSource.SMB) }
        }

        if (items.isEmpty()) {
            val (title, hint) = when (source) {
                FileSource.LOCALSEND -> "Žádné přijaté soubory" to "Pošli něco z mobilu přes LocalSend; pak se objeví tady."
                FileSource.SMB -> "SMB / NAS browser" to "V plánu na v0.5+. Zatím použij dedikované SMB apps."
                FileSource.EXTERNAL -> "Žádný externí storage" to "Připoj USB nebo SD kartu."
                else -> "Prázdná složka" to null
            }
            EmptyState(title = title, hint = hint, icon = Icons.Outlined.Folder)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
        Text(label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            fontSize = 13.sp,
            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
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
            contentColor = MaterialTheme.colorScheme.onBackground,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(item.name,
                    fontSize = 14.sp,
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

private fun iconFor(item: FileItem): Pair<ImageVector, Color> {
    if (item.isDirectory) return Icons.Outlined.Folder to Color(0xFFF39200)
    val ext = item.name.substringAfterLast('.', "").lowercase()
    return when (ext) {
        in setOf("jpg","jpeg","png","gif","webp","heic","bmp")
             -> Icons.Outlined.Image to Tone.Success
        in setOf("mp4","mkv","webm","mov","avi","m4v","wmv")
             -> Icons.Outlined.Movie to Tone.Error
        in setOf("mp3","wav","flac","ogg","aac","m4a","opus")
             -> Icons.Outlined.AudioFile to Tone.Info
        in setOf("txt","md","pdf","doc","docx","odt")
             -> Icons.Outlined.Description to Color(0xFF3B82F6)
        else -> Icons.Outlined.InsertDriveFile to Tone.Muted
    }
}

private fun humanSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024L * 1024 -> "%.1f KB".format(bytes / 1024.0)
    bytes < 1024L * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
    else -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
}
