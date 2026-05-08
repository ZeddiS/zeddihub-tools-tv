package com.zeddihub.tv.settings

import android.content.Intent
import android.provider.Settings as AndroidSettings
import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
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
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.ZhCard
import com.zeddihub.tv.ui.components.ZhPageScaffold

@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val theme by vm.theme.collectAsState()
    val triggerKey by vm.triggerKey.collectAsState()
    val corner by vm.corner.collectAsState()
    val fade by vm.fade.collectAsState()
    val updateState by vm.updateState.collectAsState()

    ZhPageScaffold {
        PageHeader(
            title = "Nastavení",
            subtitle = "Téma, časovač, oprávnění a aktualizace.",
            icon = Icons.Outlined.Settings,
        )

        Section("Vzhled") {
            ChoiceRow(
                "Téma",
                listOf("system" to "Systém", "dark" to "Tmavé", "amoled" to "AMOLED"),
                theme, vm::setTheme,
            )
        }

        Section("Časovač vypnutí") {
            ChoiceRow(
                "Spouštěč rychlých možností (long-press)",
                listOf(
                    KeyEvent.KEYCODE_DPAD_CENTER.toString() to "OK",
                    KeyEvent.KEYCODE_BACK.toString() to "Zpět",
                    KeyEvent.KEYCODE_HOME.toString() to "Home",
                    KeyEvent.KEYCODE_MENU.toString() to "Menu",
                ),
                triggerKey.toString(),
                onSelect = { vm.setTriggerKey(it.toInt()) },
            )
            ChoiceRow(
                "Pozice odpočtu",
                listOf("0" to "Levý horní", "1" to "Pravý horní", "2" to "Levý dolní", "3" to "Pravý dolní"),
                corner.toString(),
                onSelect = { vm.setCorner(it.toInt()) },
            )
            ToggleRow("Plynulé ztlumení v posledních 10 s", fade, vm::setFade)
        }

        Section("Aktualizace") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Aktuální verze",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "${BuildConfig.VERSION_NAME}  (code ${BuildConfig.VERSION_CODE})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Button(onClick = { vm.checkUpdates(ctx) }) {
                    Text(if (updateState.checking) "Kontroluji…" else "Zkontrolovat")
                }
            }
            updateState.message?.let {
                Text(
                    it,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        Section("Oprávnění") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = {
                    ctx.startActivity(
                        Intent(AndroidSettings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            android.net.Uri.parse("package:" + ctx.packageName))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }) { Text("Overlay") }
                Button(onClick = {
                    ctx.startActivity(Intent(AndroidSettings.ACTION_ACCESSIBILITY_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }) { Text("Přístupnost") }
                Button(onClick = {
                    ctx.startActivity(Intent(AndroidSettings.ACTION_SOUND_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }) { Text("Zvuk") }
            }
        }

        Section("O aplikaci") {
            Text("ZeddiHub TV — Android TV companion",
                color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
            Text(BuildConfig.WEB_URL,
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    ZhCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
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
        Text(label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp)
        Row(modifier = Modifier.padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Text(name,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        fontSize = 13.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f))
        Button(onClick = { onChange(!value) }) {
            Text(if (value) "Zapnuto" else "Vypnuto")
        }
    }
}
