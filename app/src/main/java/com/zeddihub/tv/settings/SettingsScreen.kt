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
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.zeddihub.tv.BuildConfig
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.PsPrimaryButton
import com.zeddihub.tv.ui.components.PsSecondaryButton
import com.zeddihub.tv.ui.components.SectionTitle
import com.zeddihub.tv.ui.components.StatusPill
import com.zeddihub.tv.ui.components.Tone
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

        // ── VZHLED ──────────────────────────────────────────
        SectionTitle("Vzhled")
        ZhCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ChoiceRow(
                    label = "Téma",
                    options = listOf("system" to "Systém", "dark" to "Tmavé", "amoled" to "AMOLED"),
                    selected = theme,
                    onSelect = vm::setTheme,
                )
            }
        }

        // ── ČASOVAČ ──────────────────────────────────────────
        SectionTitle("Časovač vypnutí")
        ZhCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ChoiceRow(
                    label = "Spouštěč rychlých možností (long-press)",
                    options = listOf(
                        KeyEvent.KEYCODE_DPAD_CENTER.toString() to "OK",
                        KeyEvent.KEYCODE_BACK.toString() to "Zpět",
                        KeyEvent.KEYCODE_HOME.toString() to "Home",
                        KeyEvent.KEYCODE_MENU.toString() to "Menu",
                    ),
                    selected = triggerKey.toString(),
                    onSelect = { vm.setTriggerKey(it.toInt()) },
                )
                ChoiceRow(
                    label = "Pozice odpočtu",
                    options = listOf(
                        "0" to "Levý horní",
                        "1" to "Pravý horní",
                        "2" to "Levý dolní",
                        "3" to "Pravý dolní",
                    ),
                    selected = corner.toString(),
                    onSelect = { vm.setCorner(it.toInt()) },
                )
                ToggleRow(
                    label = "Plynulé ztlumení v posledních 10 s",
                    value = fade,
                    onChange = vm::setFade,
                )
            }
        }

        // ── AKTUALIZACE ──────────────────────────────────────
        SectionTitle("Aktualizace")
        ZhCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Aktuální verze",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                BuildConfig.VERSION_NAME,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            StatusPill(label = "code ${BuildConfig.VERSION_CODE}", tone = Tone.Info)
                        }
                    }
                    PsPrimaryButton(
                        text = if (updateState.checking) "Kontroluji…" else "🔄 Zkontrolovat",
                        onClick = { vm.checkUpdates(ctx) },
                    )
                }
                updateState.message?.let {
                    Text(
                        it,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        // ── OPRÁVNĚNÍ ────────────────────────────────────────
        SectionTitle("Oprávnění (systémová Nastavení)")
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PsSecondaryButton(text = "🪟 Overlay", onClick = {
                ctx.startActivity(
                    Intent(AndroidSettings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        android.net.Uri.parse("package:" + ctx.packageName))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            })
            PsSecondaryButton(text = "♿ Přístupnost", onClick = {
                ctx.startActivity(Intent(AndroidSettings.ACTION_ACCESSIBILITY_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            })
            PsSecondaryButton(text = "🔊 Zvuk", onClick = {
                ctx.startActivity(Intent(AndroidSettings.ACTION_SOUND_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            })
        }

        // ── O APLIKACI ───────────────────────────────────────
        SectionTitle("O aplikaci")
        ZhCard(container = MaterialTheme.colorScheme.surfaceVariant) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("ZeddiHub TV — Android TV companion",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold)
                Text(BuildConfig.WEB_URL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp)
            }
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
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium)
        Row(modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (value, name) ->
                ChoicePill(
                    label = name,
                    selected = value == selected,
                    onClick = { onSelect(value) },
                )
            }
        }
    }
}

@Composable
private fun ChoicePill(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary
                             else MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
                           else MaterialTheme.colorScheme.onSurface,
            focusedContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun ToggleRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f))
        StatusPill(
            label = if (value) "zapnuto" else "vypnuto",
            tone = if (value) Tone.Success else Tone.Muted,
            modifier = Modifier.padding(end = 12.dp),
        )
        PsSecondaryButton(
            text = if (value) "Vypnout" else "Zapnout",
            onClick = { onChange(!value) },
        )
    }
}
