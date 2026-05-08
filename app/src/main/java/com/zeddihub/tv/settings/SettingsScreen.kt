package com.zeddihub.tv.settings

import android.content.Intent
import android.provider.Settings as AndroidSettings
import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.zeddihub.tv.BuildConfig

@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val theme by vm.theme.collectAsState()
    val triggerKey by vm.triggerKey.collectAsState()
    val corner by vm.corner.collectAsState()
    val fade by vm.fade.collectAsState()
    val updateState by vm.updateState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Nastavení", fontSize = 28.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)

        Section("Vzhled") {
            ChoiceRow("Téma", listOf("system" to "Systém", "dark" to "Tmavé", "amoled" to "AMOLED"),
                theme, vm::setTheme)
        }

        Section("Časovač vypnutí") {
            ChoiceRow(
                "Spouštěč rychlých možností (long-press)",
                listOf(
                    KeyEvent.KEYCODE_DPAD_CENTER.toString() to "OK / Center",
                    KeyEvent.KEYCODE_BACK.toString() to "Zpět",
                    KeyEvent.KEYCODE_HOME.toString() to "Home",
                    KeyEvent.KEYCODE_MENU.toString() to "Menu",
                ),
                triggerKey.toString(),
                onSelect = { vm.setTriggerKey(it.toInt()) }
            )
            ChoiceRow(
                "Pozice odpočtu",
                listOf("0" to "Levý horní", "1" to "Pravý horní", "2" to "Levý dolní", "3" to "Pravý dolní"),
                corner.toString(),
                onSelect = { vm.setCorner(it.toInt()) }
            )
            ToggleRow("Plynulé ztlumení v posledních 10 s", fade, vm::setFade)
        }

        Section("Aktualizace") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Aktuální verze: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f))
                Button(onClick = { vm.checkUpdates(ctx) }) {
                    Text(if (updateState.checking) "Kontroluji…" else "Zkontrolovat aktualizace")
                }
            }
            updateState.message?.let {
                Text(it, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp))
            }
        }

        Section("Oprávnění") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    ctx.startActivity(
                        Intent(AndroidSettings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            android.net.Uri.parse("package:" + ctx.packageName))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }) { Text("Zobrazení přes ostatní aplikace") }
                Button(onClick = {
                    ctx.startActivity(Intent(AndroidSettings.ACTION_ACCESSIBILITY_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }) { Text("Služba přístupnosti") }
            }
        }

        Section("O aplikaci") {
            Text("ZeddiHub TV — Android TV companion", color = MaterialTheme.colorScheme.onBackground)
            Text(BuildConfig.WEB_URL, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Surface(
        shape = androidx.tv.material3.SurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
        colors = androidx.tv.material3.SurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)
            content()
        }
    }
}

@Composable
private fun ChoiceRow(
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Column {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (value, name) ->
                val isSel = value == selected
                Surface(
                    onClick = { onSelect(value) },
                    shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (isSel) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.primary,
                        contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                        focusedContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(name, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
        Button(onClick = { onChange(!value) }) {
            Text(if (value) "Zapnuto" else "Vypnuto")
        }
    }
}
