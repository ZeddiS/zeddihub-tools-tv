package com.zeddihub.tv.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings as AndroidSettings
import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.zeddihub.tv.BuildConfig
import com.zeddihub.tv.ui.components.NoFocusBorder
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.PsPrimaryButton
import com.zeddihub.tv.ui.components.PsSecondaryButton
import com.zeddihub.tv.ui.components.PsTertiaryButton
import com.zeddihub.tv.ui.components.SectionTitle
import com.zeddihub.tv.ui.components.StatusPill
import com.zeddihub.tv.ui.components.Tone
import com.zeddihub.tv.ui.components.ZhCard
import com.zeddihub.tv.ui.components.ZhPageScaffold

@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val theme by vm.theme.collectAsState()
    val language by vm.language.collectAsState()
    val triggerKey by vm.triggerKey.collectAsState()
    val corner by vm.corner.collectAsState()
    val fade by vm.fade.collectAsState()
    val smartSleep by vm.smartSleep.collectAsState()
    val healthTemp by vm.healthTemp.collectAsState()
    val ccUniversal by vm.ccUniversal.collectAsState()
    val dyslexiaFont by vm.dyslexiaFont.collectAsState()
    val updateState by vm.updateState.collectAsState()
    val resetMessage by vm.resetMessage.collectAsState()

    var showResetConfirm by remember { mutableStateOf(false) }

    ZhPageScaffold {
        PageHeader(
            title = "Nastavení",
            subtitle = "Vzhled · Časovač · Aktualizace · Přístupnost · Reset · O aplikaci",
            icon = Icons.Outlined.Settings,
        )

        // ── VZHLED ──────────────────────────────────────────
        SectionTitle("Vzhled")
        ZhCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ChoiceRow(
                    label = "Téma",
                    options = listOf("system" to "Systém", "dark" to "Tmavé", "amoled" to "AMOLED"),
                    selected = theme,
                    onSelect = vm::setTheme,
                )
                ChoiceRow(
                    label = "Jazyk",
                    options = listOf("auto" to "Podle systému", "cs" to "Čeština", "en" to "English"),
                    selected = language,
                    onSelect = vm::setLanguage,
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
                ChoiceRow(
                    label = "Smart-sleep nudge (po N minutách nečinnosti)",
                    options = listOf(
                        "0"  to "Vypnuto",
                        "10" to "10 min",
                        "20" to "20 min",
                        "30" to "30 min",
                        "60" to "60 min",
                    ),
                    selected = smartSleep.toString(),
                    onSelect = { vm.setSmartSleep(it.toInt()) },
                )
            }
        }

        // ── AKTUALIZACE ──────────────────────────────────────
        SectionTitle("Aktualizace")
        ZhCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Aktuální verze",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 4.dp)) {
                            Text(
                                BuildConfig.VERSION_NAME,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            StatusPill(label = "code ${BuildConfig.VERSION_CODE}", tone = Tone.Info)
                            if (updateState.available != null) {
                                StatusPill(
                                    label = "↑ ${updateState.available!!.versionName} k dispozici",
                                    tone = Tone.Success,
                                )
                            }
                        }
                    }
                }

                // Banner s release notes nové verze
                updateState.available?.let { upd ->
                    ZhCard(container = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🎉 Nová verze ${upd.versionName} je dostupná",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                            if (upd.notes.isNotBlank()) {
                                Text(
                                    upd.notes.take(280) + if (upd.notes.length > 280) "…" else "",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                    }
                }

                // 2 tlačítka — Vyhledat aktualizaci + Spustit aktualizaci
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PsSecondaryButton(
                        text = if (updateState.checking) "Kontroluji…" else "🔍 Vyhledat aktualizace",
                        onClick = { vm.check() },
                    )
                    PsPrimaryButton(
                        text = if (updateState.installing) "Stahuji…"
                               else if (updateState.available != null) "🚀 Stáhnout a nainstalovat"
                               else "🚀 Spustit aktualizaci",
                        onClick = { vm.installNow(ctx) },
                    )
                }

                updateState.message?.let {
                    Text(it,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (it.startsWith("✗")) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary)
                }
            }
        }

        // ── PŘÍSTUPNOST ──────────────────────────────────────
        SectionTitle("Přístupnost")
        ZhCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ToggleRow(
                    label = "Universal closed captions",
                    value = ccUniversal,
                    onChange = vm::setCcUniversal,
                )
                ToggleRow(
                    label = "Dyslektický font",
                    value = dyslexiaFont,
                    onChange = vm::setDyslexiaFont,
                )
            }
        }

        // ── STAV TV — alert teploty ──────────────────────────
        SectionTitle("Stav TV")
        ZhCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ChoiceRow(
                    label = "Alert teploty CPU (°C)",
                    options = listOf(
                        "0"  to "Vypnuto",
                        "60" to "60 °C",
                        "70" to "70 °C",
                        "80" to "80 °C",
                        "90" to "90 °C",
                    ),
                    selected = healthTemp.toString(),
                    onSelect = { vm.setHealthTemp(it.toInt()) },
                )
            }
        }

        // ── OPRÁVNĚNÍ ────────────────────────────────────────
        SectionTitle("Systémová oprávnění")
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
            PsSecondaryButton(text = "📍 Poloha (Wi-Fi SSID)", onClick = {
                ctx.startActivity(Intent(AndroidSettings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            })
        }

        // ── RESET ────────────────────────────────────────────
        SectionTitle("Reset aplikace")
        ZhCard(container = MaterialTheme.colorScheme.error.copy(alpha = 0.10f)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.RestartAlt, null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp))
                    Column(modifier = Modifier.padding(start = 14.dp)) {
                        Text("Vrátit nastavení do výchozího stavu",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground)
                        Text("Téma, jazyk, časovač, přístupnost, oblíbené, browser bookmarks budou smazány. Wakeups, parental PIN a smart-home zařízení zůstávají.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                resetMessage?.let {
                    Text(it,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary)
                }
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PsTertiaryButton(
                        text = "↻ Reset aplikace",
                        onClick = { showResetConfirm = true },
                    )
                    if (resetMessage != null) {
                        PsSecondaryButton(text = "✓ OK", onClick = { vm.clearResetMessage() })
                    }
                }
            }
        }

        // ── O APLIKACI ───────────────────────────────────────
        SectionTitle("O aplikaci")
        ZhCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp))
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text("ZeddiHub TV",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground)
                        Text("Android TV companion app",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                InfoRow("Verze", "${BuildConfig.VERSION_NAME} (code ${BuildConfig.VERSION_CODE})")
                InfoRow("Package", ctx.packageName)
                InfoRow("Android",
                    "${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}")
                InfoRow("Zařízení", "${Build.MANUFACTURER} ${Build.MODEL}")
                InfoRow("Webová stránka", BuildConfig.WEB_URL)
                InfoRow("Discord", BuildConfig.DISCORD_URL)
                InfoRow("GitHub", "github.com/ZeddiS/zeddihub-tools-tv")
                InfoRow("Build", "${BuildConfig.BUILD_TYPE} · debug=${BuildConfig.DEBUG}")
            }
        }
    }

    // Reset confirm dialog
    if (showResetConfirm) {
        ResetConfirmDialog(
            onConfirm = {
                vm.resetSettings()
                showResetConfirm = false
            },
            onCancel = { showResetConfirm = false },
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp).fillMaxWidth(0.30f))
        Text(value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f))
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
                ChoicePill(label = name, selected = value == selected, onClick = { onSelect(value) })
            }
        }
    }
}

@Composable
private fun ChoicePill(label: String, selected: Boolean, onClick: () -> Unit) {
    // v0.1.14 — NoFocusBorder applied to remove the focus rectangle.
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
        border = NoFocusBorder,
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

@Composable
private fun ResetConfirmDialog(onConfirm: () -> Unit, onCancel: () -> Unit) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true,
            usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(0.55f),
        ) {
            Column(modifier = Modifier.padding(36.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier
                        .size(56.dp)
                        .padding(end = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.RestartAlt, null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp))
                    }
                    Text("Opravdu vrátit nastavení do výchozího stavu?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground)
                }
                Text("Smazají se:\n• Téma, jazyk, časovač\n• Smart-sleep, alert teploty\n• Universal CC, dyslektický font\n• Oblíbené dlaždice + browser záložky\n• Setup Wizard se znovu spustí\n\nZachovají se:\n• Wakeups (budíky)\n• Parental PIN + pravidla\n• Smart-home zařízení + Home Assistant\n• Sleep schedules",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PsSecondaryButton(text = "Zrušit", onClick = onCancel)
                    Box(modifier = Modifier.weight(1f))
                    PsTertiaryButton(text = "↻ Ano, resetovat", onClick = onConfirm)
                }
            }
        }
    }
}
