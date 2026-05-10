package com.zeddihub.tv.media

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.PsBigTile
import com.zeddihub.tv.ui.components.SectionTitle
import com.zeddihub.tv.ui.components.ZhPageScaffold

@Composable
fun MediaScreen() {
    val ctx = LocalContext.current

    ZhPageScaffold {
        PageHeader(
            title = "Média",
            subtitle = "Spustit streamovací aplikaci nebo otevřít remote pro Plex/Kodi.",
            icon = Icons.Outlined.Tv,
        )

        SectionTitle("Streamovací aplikace")

        // 4-col grid of large brand-tinted tiles. PsBigTile glows with the
        // app's brand color on focus.
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(StreamingApps.all) { app ->
                PsBigTile(
                    title = app.name,
                    icon = Icons.Outlined.PlayCircle,
                    accent = app.tintColor,
                    onClick = {
                        val intent = ctx.packageManager.getLaunchIntentForPackage(app.pkg)
                        if (intent != null) {
                            ctx.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        } else {
                            Toast.makeText(
                                ctx.applicationContext,
                                "${app.name} není nainstalován.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                )
            }
        }
    }
}
