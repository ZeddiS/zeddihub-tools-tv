package com.zeddihub.tv.servers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.zeddihub.tv.ui.components.EmptyState
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.Tone
import com.zeddihub.tv.ui.components.ZhCard
import com.zeddihub.tv.ui.components.ZhPageScaffold

@Composable
fun ServersScreen(vm: ServersViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    ZhPageScaffold {
        PageHeader(
            title = "Herní servery",
            subtitle = "Live status z ZeddiHub backend.",
            icon = Icons.Outlined.Dns,
            trailing = {
                Button(onClick = { vm.refresh() }, enabled = !state.loading) {
                    Text(if (state.loading) "Aktualizuji…" else "Aktualizovat")
                }
            },
        )

        state.error?.let {
            ZhCard {
                Text(it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp)
            }
        }

        if (state.servers.isEmpty() && !state.loading && state.error == null) {
            EmptyState(
                title = "Žádné servery",
                hint = "Až bude backend obsahovat servery, ukážou se tady.",
                icon = Icons.Outlined.Dns,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.servers.forEach { ServerCard(it) }
        }
    }
}

@Composable
private fun ServerCard(s: ServerStatus) {
    ZhCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusDot(s.online)
            Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                Text(s.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text(s.address,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${s.players}/${s.maxPlayers}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (s.online) MaterialTheme.colorScheme.primary else Tone.Muted)
                Text(if (s.online) "Online" else "Offline",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StatusDot(online: Boolean) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(if (online) Tone.Success else Tone.Muted),
    )
}
