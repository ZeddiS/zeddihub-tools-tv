package com.zeddihub.tv.accessibility

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

@Composable
fun AccessibilityScreen(vm: AccessibilityViewModel = hiltViewModel()) {
    val cc by vm.ccUniversal.collectAsState()
    val font by vm.dyslexiaFont.collectAsState()
    val ctx = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("Přístupnost", fontSize = 28.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)
        Text("Universal CC + dyslektický font + odkazy do system accessibility.",
            fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Universal CC
        Card(
            title = "Universal closed captions",
            sub = "Vynutí titulky napříč podporovanými přehrávači. Použít v kombinaci se systémovým Accessibility → Captions.",
            on = cc,
            onToggle = { vm.toggleCc() },
            extra = {
                Button(onClick = {
                    ctx.startActivity(Intent(Settings.ACTION_CAPTIONING_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }) { Text("System captions") }
            }
        )

        // Dyslexia font
        Card(
            title = "Dyslektický font",
            sub = "Aplikuje OpenDyslexic-style font na nadpisy v aplikaci. Nemá efekt na obsah uvnitř streamovacích apps.",
            on = font,
            onToggle = { vm.toggleFont() }
        )

        // System accessibility shortcuts
        Surface(
            shape = RoundedCornerShape(12.dp),
            colors = androidx.tv.material3.SurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Systémové zkratky", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }) { Text("Accessibility settings") }
                    Button(onClick = {
                        ctx.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }) { Text("Display settings") }
                }
            }
        }
    }
}

@Composable
private fun Card(
    title: String,
    sub: String,
    on: Boolean,
    onToggle: () -> Unit,
    extra: @Composable (() -> Unit)? = null,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        colors = androidx.tv.material3.SurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(20.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text(sub, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp))
            }
            if (extra != null) Row(modifier = Modifier.padding(end = 8.dp)) { extra() }
            Button(onClick = onToggle) {
                Text(if (on) "Vypnout" else "Zapnout")
            }
        }
    }
}
