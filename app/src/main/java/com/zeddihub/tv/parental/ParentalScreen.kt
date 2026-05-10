package com.zeddihub.tv.parental

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.zeddihub.tv.ui.components.EmptyState
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.PsSecondaryButton
import com.zeddihub.tv.ui.components.SectionTitle
import com.zeddihub.tv.ui.components.StatusPill
import com.zeddihub.tv.ui.components.Tone
import com.zeddihub.tv.ui.components.ZhCard
import com.zeddihub.tv.ui.components.ZhPageScaffold

@Composable
fun ParentalScreen(vm: ParentalViewModel = hiltViewModel()) {
    val rules by vm.rules.collectAsState()
    val pinIsSet by vm.pinIsSet.collectAsState()
    val message by vm.message.collectAsState()

    var newPin by remember { mutableStateOf("") }

    ZhPageScaffold {
        PageHeader(
            title = "Rodičovská kontrola",
            subtitle = "PIN-locked aplikace + bedtime hodiny per-app.",
            icon = Icons.Outlined.FamilyRestroom,
        )

        // PIN section
        ZhCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (pinIsSet) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                        null,
                        tint = if (pinIsSet) Tone.Success else Tone.Muted,
                        modifier = Modifier.size(28.dp),
                    )
                    Text(
                        if (pinIsSet) "PIN je nastaven" else "PIN nenastaven",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 12.dp).weight(1f),
                    )
                    StatusPill(
                        label = if (pinIsSet) "aktivní" else "vypnuto",
                        tone = if (pinIsSet) Tone.Success else Tone.Muted,
                    )
                }
                Text(
                    "Vstup: ${"•".repeat(newPin.length)}${"_".repeat((4 - newPin.length).coerceAtLeast(0))}" +
                        if (newPin.length > 4) "  (+${newPin.length - 4})" else "  (${newPin.length}/6)",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (newPin.length in 4..6) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // 3×4 D-pad keypad — PsKeyButton scales on focus
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        listOf("1","2","3"),
                        listOf("4","5","6"),
                        listOf("7","8","9"),
                        listOf("⌫","0","✓"),
                    ).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { key ->
                                PsKeyButton(
                                    label = key,
                                    onClick = {
                                        when (key) {
                                            "⌫" -> if (newPin.isNotEmpty())
                                                newPin = newPin.dropLast(1)
                                            "✓" -> if (newPin.length in 4..6) {
                                                vm.setPin(newPin); newPin = ""
                                            }
                                            else -> if (newPin.length < 6)
                                                newPin += key
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
                if (pinIsSet) {
                    Row(modifier = Modifier.padding(top = 4.dp)) {
                        PsSecondaryButton(text = "🗑 Zrušit PIN", onClick = { vm.setPin("") })
                    }
                }
            }
        }

        // Rules
        SectionTitle("Pravidla (${rules.size})")

        if (rules.isEmpty()) {
            EmptyState(
                title = "Žádná pravidla",
                hint = "Použij jeden z presetů níže — Netflix / YouTube / Disney+ / Twitch.",
                icon = Icons.Outlined.Lock,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            rules.forEach { rule ->
                RuleCard(
                    rule = rule,
                    onTogglePin = { vm.togglePinRequired(rule) },
                    onBedtime = { vm.setBedtime(rule, 22 * 60, 7 * 60) },
                    onClear = { vm.removeRule(rule.packageName) },
                )
            }
        }

        // Add rule
        SectionTitle("Přidat pravidlo")
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PsSecondaryButton(text = "🎬 Netflix", onClick = { vm.addRule("com.netflix.ninja") })
            PsSecondaryButton(text = "▶️ YouTube TV", onClick = { vm.addRule("com.google.android.youtube.tv") })
            PsSecondaryButton(text = "🏰 Disney+", onClick = { vm.addRule("com.disney.disneyplus") })
            PsSecondaryButton(text = "🎮 Twitch", onClick = { vm.addRule("tv.twitch.android.app") })
        }

        message?.let {
            Text(it,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun RuleCard(
    rule: ParentalRule,
    onTogglePin: () -> Unit,
    onBedtime: () -> Unit,
    onClear: () -> Unit,
) {
    val accent = if (rule.pinRequired) Tone.Warning else Tone.Muted
    ZhCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (rule.pinRequired) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                null,
                tint = accent,
                modifier = Modifier.size(28.dp),
            )
            Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                Text(rule.packageName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Row(modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill(
                        label = if (rule.pinRequired) "PIN required" else "open",
                        tone = accent,
                    )
                    StatusPill(
                        label = "bedtime ${rule.bedtimeText()}",
                        tone = Tone.Info,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PsSecondaryButton(
                    text = if (rule.pinRequired) "PIN off" else "PIN on",
                    onClick = onTogglePin,
                )
                PsSecondaryButton(text = "🌙 22-7", onClick = onBedtime)
                PsSecondaryButton(text = "🗑", onClick = onClear)
            }
        }
    }
}

@Composable
private fun PsKeyButton(label: String, onClick: () -> Unit) {
    val isAction = label == "⌫" || label == "✓"
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(14.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isAction) MaterialTheme.colorScheme.surfaceVariant
                              else MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onBackground,
            focusedContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = Modifier.size(width = 80.dp, height = 56.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(label,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold)
        }
    }
}
