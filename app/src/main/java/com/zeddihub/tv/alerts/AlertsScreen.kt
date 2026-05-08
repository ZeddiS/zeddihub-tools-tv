package com.zeddihub.tv.alerts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

@Composable
fun AlertsScreen(vm: AlertsViewModel = hiltViewModel()) {
    val latest by vm.latest.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Upozornění", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("Server-down alerty + admin push z ZeddiHub backend.",
                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = { vm.test() }) { Text("Test overlay") }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            colors = androidx.tv.material3.SurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Poslední upozornění", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                if (latest == null) {
                    Text("Žádná upozornění. Polluju ${"/api/alerts.php?kind=tv"} každých 60 s.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp))
                } else latest?.let { a ->
                    Text(a.title, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = severityColor(a.severity),
                        modifier = Modifier.padding(top = 12.dp))
                    if (a.message.isNotBlank()) Text(a.message, fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp))
                    Text("Source: ${a.source}", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

private fun severityColor(s: String): Color = when (s.lowercase()) {
    "error" -> Color(0xFFEF4444)
    "warn"  -> Color(0xFFF59E0B)
    else    -> Color(0xFF3B82F6)
}
