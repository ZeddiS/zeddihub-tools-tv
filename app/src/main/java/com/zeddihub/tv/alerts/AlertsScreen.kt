package com.zeddihub.tv.alerts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.zeddihub.tv.ui.components.SectionTitle
import com.zeddihub.tv.ui.components.Tone
import com.zeddihub.tv.ui.components.ZhCard
import com.zeddihub.tv.ui.components.ZhPageScaffold

@Composable
fun AlertsScreen(vm: AlertsViewModel = hiltViewModel()) {
    val latest by vm.latest.collectAsState()
    ZhPageScaffold {
        PageHeader(
            title = "Upozornění",
            subtitle = "Server-down + admin push z ZeddiHub backend.",
            icon = Icons.Outlined.Campaign,
            trailing = { Button(onClick = { vm.test() }) { Text("Test overlay") } },
        )

        SectionTitle("Poslední upozornění")
        if (latest == null) {
            EmptyState(
                title = "Žádná upozornění",
                hint = "Polluju /api/alerts.php?kind=tv každých 60 s.",
                icon = Icons.Outlined.Campaign,
            )
        } else latest?.let { a ->
            ZhCard {
                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)) {
                    Text(a.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = severityColor(a.severity))
                    if (a.message.isNotBlank()) Text(a.message,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Zdroj: ${a.source}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun severityColor(s: String): Color = when (s.lowercase()) {
    "error" -> Tone.Error
    "warn"  -> Tone.Warning
    else    -> Tone.Info
}
