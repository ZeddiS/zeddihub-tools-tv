package com.zeddihub.tv.setup

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

/**
 * First-run setup wizard. Routed by MainActivity when AppPrefs.wizardCompleted
 * is false. Skip → goes straight to the app with defaults; Finish → persists
 * collected answers and the completion flag, then navigates to Dashboard.
 *
 * Steps (in order):
 *   1. Welcome (just a "let's go" screen with the brand)
 *   2. Language (CS / EN)
 *   3. Theme (system / dark / amoled)
 *   4. Permission "draw over apps" (deep-links to Settings; user comes back)
 *   5. Permission "accessibility service" (deep-links to Settings)
 *   6. Sleep Timer trigger button (OK / Back / Home / Menu)
 *   7. Sleep Timer overlay corner (4 corners)
 *   8. Weather location (Prague / Brno / Ostrava / custom)
 *   9. Home Assistant URL + token (skip OK)
 *   10. Done (recap + Finish)
 *
 * Why one big stateful Composable instead of Compose Navigation: the wizard
 * is short-lived, fully linear, and we want a single back/next animation
 * pattern. A nav graph would add ceremony for no real benefit.
 */
@Composable
fun SetupWizard(
    onFinished: () -> Unit,
    vm: SetupWizardViewModel = hiltViewModel(),
) {
    val ctx = LocalContext.current
    var step by remember { mutableStateOf(0) }
    val totalSteps = 10

    val state by vm.state.collectAsState()

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(48.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.size(56.dp)) {
                    Image(
                        painter = painterResource(com.zeddihub.tv.R.drawable.zh_logo_square),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                    Text(stringRes(com.zeddihub.tv.R.string.wizard_welcome_title),
                        fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground)
                    Text("Krok ${step + 1} z $totalSteps",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                ProgressDots(current = step, total = totalSteps)
            }

            // Step content
            Box(modifier = Modifier.weight(1f).padding(top = 32.dp)) {
                when (step) {
                    0 -> StepWelcome()
                    1 -> StepLanguage(state.language, vm::setLanguage)
                    2 -> StepTheme(state.theme, vm::setTheme)
                    3 -> StepPermOverlay()
                    4 -> StepPermAccessibility()
                    5 -> StepTriggerKey(state.triggerKey, vm::setTriggerKey)
                    6 -> StepCorner(state.corner, vm::setCorner)
                    7 -> StepWeather(state.weatherLabel, vm::setWeather)
                    8 -> StepHomeAssistant(state.hassUrl, state.hassToken, vm::setHass)
                    9 -> StepDone(state)
                }
            }

            // Footer with Back / Skip / Next or Finish
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (step > 0) Button(onClick = { step-- }) {
                    Text(stringRes(com.zeddihub.tv.R.string.wizard_back))
                }
                Box(modifier = Modifier.weight(1f))
                Button(onClick = {
                    vm.persist()
                    onFinished()
                }) { Text(stringRes(com.zeddihub.tv.R.string.wizard_skip)) }
                if (step < totalSteps - 1) {
                    Button(onClick = { step++ }) {
                        Text(stringRes(com.zeddihub.tv.R.string.wizard_next))
                    }
                } else {
                    Button(onClick = {
                        vm.persist()
                        onFinished()
                    }) { Text(stringRes(com.zeddihub.tv.R.string.wizard_finish)) }
                }
            }
        }
    }
}

@Composable
private fun ProgressDots(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 0 until total) {
            val on = i <= current
            Box(modifier = Modifier
                .size(8.dp)
                .padding()
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    colors = androidx.tv.material3.SurfaceDefaults.colors(
                        containerColor = if (on) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.size(8.dp),
                ) { Box(modifier = Modifier.fillMaxSize()) }
            }
        }
    }
}

@Composable
private fun StepWelcome() {
    Column {
        Text("Vítej v ZeddiHub TV.", fontSize = 28.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)
        Text("Pár otázek a aplikace bude přesně tvá. Většinu nastavíš během minuty, " +
                "zbytek později v Nastavení.",
            fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp))
        Text("Tip: kdykoliv můžeš kliknout Přeskočit a vrátit se k tomu později.",
            fontSize = 13.sp, color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 24.dp))
    }
}

@Composable
private fun StepLanguage(value: String, onSet: (String) -> Unit) {
    StepHeader("Jazyk", "Aplikace nyní mluví hlavně česky; angličtina je v některých oblastech.")
    Choices(
        listOf("auto" to "Podle systému", "cs" to "Čeština", "en" to "English"),
        selected = value, onSelect = onSet,
    )
}

@Composable
private fun StepTheme(value: String, onSet: (String) -> Unit) {
    StepHeader("Téma", "TV se nejlépe ovládá v tlumeném světle; tmavé téma má menší ghosting.")
    Choices(
        listOf("system" to "Podle systému", "dark" to "Tmavé", "amoled" to "AMOLED černé"),
        selected = value, onSelect = onSet,
    )
}

@Composable
private fun StepPermOverlay() {
    val ctx = LocalContext.current
    StepHeader("Zobrazení přes ostatní aplikace",
        "Bez tohoto se neukáže plovoucí odpočet Sleep Timeru ani banner upozornění serverů. " +
                "Klikni na tlačítko, povol v Settings a vrať se zpět tlačítkem Back.")
    Button(onClick = {
        ctx.startActivity(
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + ctx.packageName))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }) { Text("Otevřít Settings") }
}

@Composable
private fun StepPermAccessibility() {
    val ctx = LocalContext.current
    StepHeader("Služba přístupnosti",
        "Bez tohoto nejde long-press tlačítko časovače ani auto-detekce spánku. " +
                "Najdi položku ZeddiHub TV v seznamu a zapni ji.")
    Button(onClick = {
        ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }) { Text("Otevřít přístupnost") }
}

@Composable
private fun StepTriggerKey(value: Int, onSet: (Int) -> Unit) {
    StepHeader("Spouštěč rychlých možností časovače",
        "Long-press tohoto tlačítka (~800 ms) otevře pauza/stop/vypnout přes celou TV.")
    Choices(
        listOf(
            KeyEvent.KEYCODE_DPAD_CENTER to "OK / Center",
            KeyEvent.KEYCODE_BACK to "Zpět",
            KeyEvent.KEYCODE_HOME to "Home",
            KeyEvent.KEYCODE_MENU to "Menu",
        ).map { it.first.toString() to it.second },
        selected = value.toString(),
        onSelect = { onSet(it.toInt()) },
    )
}

@Composable
private fun StepCorner(value: Int, onSet: (Int) -> Unit) {
    StepHeader("Roh pro plovoucí odpočet",
        "Kde se zobrazí malý chip s odpočtem během běžícího Sleep Timeru.")
    Choices(
        listOf("0" to "Levý horní", "1" to "Pravý horní", "2" to "Levý dolní", "3" to "Pravý dolní"),
        selected = value.toString(),
        onSelect = { onSet(it.toInt()) },
    )
}

@Composable
private fun StepWeather(label: String, onSet: (String, Double, Double) -> Unit) {
    StepHeader("Počasí", "Pro Dashboard widget. Vyber město nebo nech Prahu (default).")
    Choices(
        listOf(
            "Praha" to "Praha",
            "Brno" to "Brno",
            "Ostrava" to "Ostrava",
            "Plzeň" to "Plzeň",
        ),
        selected = label,
        onSelect = { city ->
            val (lat, lon) = when (city) {
                "Praha" -> 50.08 to 14.43
                "Brno" -> 49.20 to 16.61
                "Ostrava" -> 49.83 to 18.28
                "Plzeň" -> 49.74 to 13.38
                else -> 50.08 to 14.43
            }
            onSet(city, lat, lon)
        },
    )
}

@Composable
private fun StepHomeAssistant(url: String, token: String, onSet: (String, String) -> Unit) {
    StepHeader("Home Assistant (volitelné)",
        "Pokud máš HA, můžeš zadat teď. Jinak přeskoč — nastavíš později v sekci HA.")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("URL: ${url.ifBlank { "(nenastaveno)" }}", fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Token: ${if (token.isBlank()) "(nenastaveno)" else "••••••••${token.takeLast(4)}"}",
            fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onSet("http://homeassistant.local:8123", token) }) {
                Text("LAN default")
            }
            Button(onClick = { onSet("", "") }) { Text("Vymazat") }
        }
        Text("HA token zadáš později v sekci Home Assistant — TV input je nepohodlný; doporučujeme " +
                "v admin panelu na zeddihub.eu, nebo pomoci sdílet bookmark přes LocalSend z mobilu.",
            fontSize = 12.sp, color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun StepDone(state: SetupWizardViewModel.WizardState) {
    StepHeader("Hotovo!", "Tady je co jsme nastavili. Můžeš to kdykoli změnit v Nastavení.")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 12.dp)) {
        Recap("Jazyk", state.language)
        Recap("Téma", state.theme)
        Recap("Trigger", triggerLabel(state.triggerKey))
        Recap("Roh odpočtu", cornerLabel(state.corner))
        Recap("Počasí", state.weatherLabel)
        Recap("Home Assistant", if (state.hassUrl.isBlank()) "—" else state.hassUrl)
    }
}

@Composable
private fun Recap(label: String, value: String) {
    Row {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp,
            modifier = Modifier.width(140.dp))
        Text(value, color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp,
            fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StepHeader(title: String, sub: String) {
    Column {
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)
        Text(sub, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun Choices(options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (value, label) ->
            val isSel = value == selected
            Surface(
                onClick = { onSelect(value) },
                shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = if (isSel) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                    contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onBackground,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(label, modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    fontSize = 16.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun stringRes(id: Int): String = androidx.compose.ui.res.stringResource(id)

private fun triggerLabel(code: Int): String = when (code) {
    KeyEvent.KEYCODE_DPAD_CENTER -> "OK / Center"
    KeyEvent.KEYCODE_BACK -> "Zpět"
    KeyEvent.KEYCODE_HOME -> "Home"
    KeyEvent.KEYCODE_MENU -> "Menu"
    else -> "?"
}

private fun cornerLabel(c: Int): String = when (c) {
    0 -> "Levý horní"
    1 -> "Pravý horní"
    2 -> "Levý dolní"
    3 -> "Pravý dolní"
    else -> "?"
}
