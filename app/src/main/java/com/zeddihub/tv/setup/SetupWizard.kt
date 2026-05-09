package com.zeddihub.tv.setup

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsRemote
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.zeddihub.tv.ui.components.PsBigChoice
import com.zeddihub.tv.ui.components.PsHeroFrame
import com.zeddihub.tv.ui.components.PsPrimaryButton
import com.zeddihub.tv.ui.components.PsSecondaryButton

/**
 * First-run setup wizard — PlayStation-style. Each step is a hero with
 * big choice tiles (instead of the cramped pill row from 0.1.x). Sticky
 * footer keeps Back / Skip / Next visible regardless of content length.
 */
@Composable
fun SetupWizard(
    onFinished: () -> Unit,
    vm: SetupWizardViewModel = hiltViewModel(),
) {
    var step by remember { mutableStateOf(0) }
    val totalSteps = 10
    val state by vm.state.collectAsState()

    PsHeroFrame(
        title = stepTitle(step),
        subtitle = stepSubtitle(step),
        stepCurrent = step,
        stepTotal = totalSteps,
        actions = {
            if (step > 0) {
                PsSecondaryButton(text = "← Zpět", onClick = { step-- })
                Spacer(Modifier.width(12.dp))
            }
            PsSecondaryButton(text = "Přeskočit", onClick = {
                vm.persist(); onFinished()
            })
            Spacer(Modifier.weight(1f))
            if (step < totalSteps - 1) {
                PsPrimaryButton(text = "Další →", onClick = { step++ })
            } else {
                PsPrimaryButton(text = "Dokončit ✓", onClick = {
                    vm.persist(); onFinished()
                })
            }
        },
    ) {
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
}

@Composable
private fun StepWelcome() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(32.dp)) {
            Box(modifier = Modifier.size(120.dp)) {
                androidx.compose.foundation.Image(
                    painter = painterResource(com.zeddihub.tv.R.drawable.zh_logo_square),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.width(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Vítej v ZeddiHub TV",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    "Pár otázek a aplikace bude přesně tvá. Většinu nastavíš během minuty, " +
                            "zbytek později v Nastavení.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    "Tip: kdykoli můžeš stisknout Přeskočit a vrátit se k tomu později.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun StepLanguage(value: String, onSet: (String) -> Unit) {
    ChoiceGrid(
        listOf(
            ChoiceOption("auto", "Podle systému", "Aplikace převezme jazyk Android TV.", Icons.Outlined.Settings),
            ChoiceOption("cs", "Čeština", "Plně přeloženo, výchozí pro CZ region.", Icons.Outlined.Language),
            ChoiceOption("en", "English", "International / English UI.", Icons.Outlined.Language),
        ),
        selected = value,
        onSelect = onSet,
    )
}

@Composable
private fun StepTheme(value: String, onSet: (String) -> Unit) {
    ChoiceGrid(
        listOf(
            ChoiceOption("system", "Podle systému", "Sleduje světlo / tma podle TV nastavení.", Icons.Outlined.Settings),
            ChoiceOption("dark", "Tmavé", "Default — méně ghostingu, lepší kontrast.", Icons.Outlined.DarkMode),
            ChoiceOption("amoled", "AMOLED černé", "Plně černé pozadí pro OLED panely.", Icons.Outlined.LightMode),
        ),
        selected = value,
        onSelect = onSet,
    )
}

@Composable
private fun StepPermOverlay() {
    val ctx = LocalContext.current
    PermStep(
        icon = Icons.Outlined.Visibility,
        bullets = listOf(
            "Plovoucí odpočet Sleep Timeru přes všechny aplikace",
            "Banner upozornění o serverech na pozadí",
            "Smart-sleep nudge",
        ),
        buttonLabel = "Otevřít systémové nastavení",
        onClick = {
            ctx.startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + ctx.packageName))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        },
        hint = "Po povolení stiskni Back na ovladači a tady klikni Další.",
    )
}

@Composable
private fun StepPermAccessibility() {
    val ctx = LocalContext.current
    PermStep(
        icon = Icons.Outlined.Accessibility,
        bullets = listOf(
            "Long-press tlačítka časovače mimo aplikaci",
            "Auto-detekce spánku podle inputu + audio",
            "Vypnutí TV i boxu z časovače na 0:00",
        ),
        buttonLabel = "Otevřít přístupnost",
        onClick = {
            ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        },
        hint = "Najdi položku ZeddiHub TV v seznamu a zapni ji.",
    )
}

@Composable
private fun StepTriggerKey(value: Int, onSet: (Int) -> Unit) {
    ChoiceGrid(
        listOf(
            ChoiceOption(KeyEvent.KEYCODE_DPAD_CENTER.toString(), "OK / Center",
                "Doporučeno — nejdostupnější tlačítko.", Icons.Outlined.SettingsRemote),
            ChoiceOption(KeyEvent.KEYCODE_BACK.toString(), "Zpět",
                "Univerzální, ale může kolidovat s exit gesturou.", Icons.Outlined.SettingsRemote),
            ChoiceOption(KeyEvent.KEYCODE_HOME.toString(), "Home",
                "Na některých dálkách problémové (intercept od OS).", Icons.Outlined.SettingsRemote),
            ChoiceOption(KeyEvent.KEYCODE_MENU.toString(), "Menu",
                "Pokud má dálkové menu tlačítko.", Icons.Outlined.SettingsRemote),
        ),
        selected = value.toString(),
        onSelect = { onSet(it.toInt()) },
    )
}

@Composable
private fun StepCorner(value: Int, onSet: (Int) -> Unit) {
    ChoiceGrid(
        listOf(
            ChoiceOption("0", "Levý horní", "Standardní pozice clock overlaye.", Icons.Outlined.Tv),
            ChoiceOption("1", "Pravý horní", "Doporučeno — mimo subtitle a UI Netflixu.", Icons.Outlined.Tv),
            ChoiceOption("2", "Levý dolní", "Pokud sleduješ obsah s top-bar.", Icons.Outlined.Tv),
            ChoiceOption("3", "Pravý dolní", "Pokud máš plné side rail UI vlevo.", Icons.Outlined.Tv),
        ),
        selected = value.toString(),
        onSelect = { onSet(it.toInt()) },
    )
}

@Composable
private fun StepWeather(label: String, onSet: (String, Double, Double) -> Unit) {
    val cities = listOf(
        Triple("Praha", 50.08, 14.43),
        Triple("Brno", 49.20, 16.61),
        Triple("Ostrava", 49.83, 18.28),
        Triple("Plzeň", 49.74, 13.38),
    )
    ChoiceGrid(
        cities.map { (city, lat, lon) ->
            ChoiceOption(city, city, "%.2f° / %.2f°".format(lat, lon), Icons.Outlined.LocationCity)
        },
        selected = label,
        onSelect = { city ->
            val (_, lat, lon) = cities.first { it.first == city }
            onSet(city, lat, lon)
        },
    )
}

@Composable
private fun StepHomeAssistant(url: String, token: String, onSet: (String, String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Home, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("URL", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            url.ifBlank { "(nenastaveno)" },
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(48.dp))
                    Column {
                        Text("Token", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (token.isBlank()) "(nenastaveno)" else "••••••••${token.takeLast(4)}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PsSecondaryButton(text = "LAN default", onClick = {
                onSet("http://homeassistant.local:8123", token)
            })
            PsSecondaryButton(text = "Vymazat", onClick = { onSet("", "") })
        }

        Text(
            "TV remote pro psaní URL je nepohodlný. Doporučujeme zadat detaily později " +
                    "z admin panelu nebo přes LocalSend z mobilu — najdeš to v sekci Home Assistant.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun StepDone(state: SetupWizardViewModel.WizardState) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Hotovo!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "Tady je co jsme nastavili. Můžeš to kdykoli změnit v Nastavení.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            Recap("Jazyk", state.language)
            Recap("Téma", state.theme)
            Recap("Spouštěč", triggerLabel(state.triggerKey))
            Recap("Roh odpočtu", cornerLabel(state.corner))
            Recap("Počasí", state.weatherLabel)
            Recap("Home Assistant", if (state.hassUrl.isBlank()) "—" else state.hassUrl)
        }
    }
}

@Composable
private fun Recap(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            modifier = Modifier.width(180.dp))
        Text(value,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PermStep(
    icon: ImageVector,
    bullets: List<String>,
    buttonLabel: String,
    onClick: () -> Unit,
    hint: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(28.dp)) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .padding(end = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp))
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Bez tohoto nepůjde:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    bullets.forEach {
                        Text("• $it",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
        }
        PsPrimaryButton(text = buttonLabel, onClick = onClick)
        Text(hint, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private data class ChoiceOption(
    val value: String,
    val title: String,
    val description: String?,
    val icon: ImageVector?,
)

@Composable
private fun ChoiceGrid(
    options: List<ChoiceOption>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(options.size) { idx ->
            val o = options[idx]
            PsBigChoice(
                title = o.title,
                description = o.description,
                icon = o.icon,
                selected = o.value == selected,
                onClick = { onSelect(o.value) },
            )
        }
    }
}

private fun stepTitle(step: Int): String = when (step) {
    0 -> "Vítej v ZeddiHub TV"
    1 -> "Vyber jazyk"
    2 -> "Vyber téma"
    3 -> "Povol overlay"
    4 -> "Povol přístupnost"
    5 -> "Trigger button časovače"
    6 -> "Roh pro odpočet"
    7 -> "Počasí"
    8 -> "Home Assistant"
    9 -> "Hotovo"
    else -> ""
}

private fun stepSubtitle(step: Int): String? = when (step) {
    0 -> "Pár otázek a jdeme na to."
    1 -> "Aplikace nyní mluví hlavně česky; angličtina je v některých oblastech."
    2 -> "TV se nejlépe ovládá v tlumeném světle; tmavé téma má menší ghosting."
    3 -> "Plovoucí odpočet a banner upozornění potřebují tohle povolení."
    4 -> "Pro long-press triggers a smart-sleep detekci."
    5 -> "Které tlačítko long-pressnutím (~800 ms) otevře rychlé možnosti."
    6 -> "Kde se zobrazí malý chip s odpočtem během běžícího Sleep Timeru."
    7 -> "Pro Dashboard widget. Vyber město."
    8 -> "Volitelné. Pokud máš HA, ušetříš si setup později."
    9 -> "Recap a hotovo!"
    else -> null
}

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

private fun androidx.compose.foundation.lazy.LazyListScope.items(count: Int, content: @Composable (Int) -> Unit) {
    items(count = count, key = null, contentType = { 0 }, itemContent = { content(it) })
}
