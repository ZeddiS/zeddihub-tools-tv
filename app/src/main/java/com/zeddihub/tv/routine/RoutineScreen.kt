package com.zeddihub.tv.routine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NightsStay
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.zeddihub.tv.ui.components.EmptyState
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.PsPrimaryButton
import com.zeddihub.tv.ui.components.PsSecondaryButton
import com.zeddihub.tv.ui.components.SectionTitle
import com.zeddihub.tv.ui.components.Tone
import com.zeddihub.tv.ui.components.ZhCard
import com.zeddihub.tv.ui.components.ZhPageScaffold

@Composable
fun RoutineScreen(vm: RoutineViewModel = hiltViewModel()) {
    val steps by vm.steps.collectAsState()
    val running by vm.running.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    ZhPageScaffold {
        PageHeader(
            title = "Bedtime routine",
            subtitle = "Posloupnost akcí pro spaní — jeden press → vše se stane.",
            icon = Icons.Outlined.NightsStay,
            trailing = {
                PsPrimaryButton(
                    text = if (running) "Běží…" else "▶ Spustit teď",
                    onClick = { if (!running) vm.run() },
                )
            },
        )

        SectionTitle("Aktuální routina (${steps.size} kroků)")

        if (steps.isEmpty()) {
            EmptyState(
                title = "Prázdná routina",
                hint = "Klikni Spustit teď — použije se výchozí (fade-out 30s + start timer 30 min).",
                icon = Icons.Outlined.NightsStay,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            steps.forEachIndexed { idx, s ->
                StepCard(idx + 1, s)
            }
        }

        SectionTitle("Přidat krok")
        // Two rows of action chips for adding steps
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PsSecondaryButton(text = "↺ Výchozí", onClick = { vm.resetToDefault() })
            PsSecondaryButton(text = "+ Fade out", onClick = { vm.addVolumeFade() })
            PsSecondaryButton(text = "+ Spustit timer", onClick = { vm.addStartTimer() })
            PsSecondaryButton(text = "+ Pauza", onClick = { vm.addDelay() })
            PsSecondaryButton(text = "+ Webhook", onClick = { vm.addWebhook() })
        }
    }
}

/**
 * Step card — large numbered circle on the left, icon + label centred,
 * description with parameters underneath. Mirrors the wizard's choice
 * card scale so the routine sequence reads as deliberate steps not
 * cramped rows.
 */
@Composable
private fun StepCard(idx: Int, s: RoutineStep) {
    val (icon, title, desc, accent) = when (s.kind) {
        RoutineKinds.VOLUME_FADE -> StepInfo(
            icon = "🔉",
            title = "Ztlumit zvuk",
            desc = "Z aktuální hlasitosti na ${s.targetVolumePct}% za ${s.durationSeconds}s",
            accent = Tone.Info,
        )
        RoutineKinds.START_TIMER -> StepInfo(
            icon = "⏰",
            title = "Spustit Sleep Timer",
            desc = "Časovač na ${s.timerMinutes} min",
            accent = MaterialTheme.colorScheme.primary,
        )
        RoutineKinds.DELAY -> StepInfo(
            icon = "⏳",
            title = "Pauza",
            desc = "Počkat ${s.durationSeconds} sekund před dalším krokem",
            accent = Tone.Muted,
        )
        RoutineKinds.WEBHOOK -> StepInfo(
            icon = "🌐",
            title = "Webhook",
            desc = "${s.webhookMethod} ${s.webhookUrl.ifBlank { "(žádná URL)" }}",
            accent = Tone.Success,
        )
        else -> StepInfo("❓", s.kind, "", Tone.Muted)
    }

    ZhCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Numbered step circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("$idx",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent)
            }
            Text(icon,
                fontSize = 24.sp,
                modifier = Modifier.padding(horizontal = 16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text(desc,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

private data class StepInfo(
    val icon: String,
    val title: String,
    val desc: String,
    val accent: Color,
)
