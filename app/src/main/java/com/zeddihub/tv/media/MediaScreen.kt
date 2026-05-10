package com.zeddihub.tv.media

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.PsBigTile
import com.zeddihub.tv.ui.components.SectionTitle
import com.zeddihub.tv.ui.components.ZhPageScaffold

@Composable
fun MediaScreen(vm: MediaViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val apps by vm.apps.collectAsState()

    ZhPageScaffold {
        PageHeader(
            title = "Média",
            subtitle = "Spustit streamovací aplikaci. Pořadí editovatelné v admin panelu.",
            icon = Icons.Outlined.Tv,
        )

        SectionTitle("Streamovací aplikace (${apps.size})")

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(apps) { app ->
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
