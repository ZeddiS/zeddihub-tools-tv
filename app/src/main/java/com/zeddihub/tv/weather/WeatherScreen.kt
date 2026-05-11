package com.zeddihub.tv.weather

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.PsBigChoice
import com.zeddihub.tv.ui.components.PsPrimaryButton
import com.zeddihub.tv.ui.components.PsSecondaryButton
import com.zeddihub.tv.ui.components.SectionTitle
import com.zeddihub.tv.ui.components.StatusPill
import com.zeddihub.tv.ui.components.Tone
import com.zeddihub.tv.ui.components.ZhCard
import com.zeddihub.tv.ui.components.ZhPageScaffold

/**
 * v0.1.15 — Profi počasí screen.
 *
 * Sections:
 *   • Hero current — 96sp teplota + label + ikona + IP/wind/humidity strip
 *   • 24h hourly — LazyRow s teplotami každou hodinu
 *   • 7-day daily — LazyRow s min/max + ikona pro každý den
 *   • City search — předdefinovaný seznam CZ měst, plus custom search přes
 *     Open-Meteo Geocoding API
 *   • Meteoradar — Windy.com WebView embed (uživatel zvolil Windy v 0.1.15)
 *
 * Data source: Open-Meteo (free, no API key) přes WeatherViewModel.
 */
@Composable
fun WeatherScreen(vm: WeatherViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    var showRadar by remember { mutableStateOf(false) }
    var showCityPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.refresh() }

    ZhPageScaffold {
        PageHeader(
            title = "Počasí",
            subtitle = "${state.cityLabel} · ${state.current?.tempText ?: "—"} · poslední update ${state.lastUpdated ?: "—"}",
            icon = Icons.Outlined.WbSunny,
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PsSecondaryButton(text = "🔍 Vybrat město", onClick = { showCityPicker = true })
                    PsPrimaryButton(text = if (showRadar) "↑ Zpět nahoru" else "🗺 Meteoradar",
                        onClick = { showRadar = !showRadar })
                }
            },
        )

        if (showRadar) {
            RadarSection(lat = state.lat, lon = state.lon)
            return@ZhPageScaffold
        }

        // ── Hero current ──────────────────────────────────────
        CurrentHero(state)

        // ── Strip — wind / humidity / feels-like / UV ─────────
        if (state.current != null) {
            DetailStrip(state.current!!)
        }

        // ── 24h hourly ────────────────────────────────────────
        if (state.hourly.isNotEmpty()) {
            SectionTitle("Příštích 24 hodin")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
            ) {
                items(state.hourly.take(24)) { hour -> HourlyCard(hour) }
            }
        }

        // ── 7-day forecast ────────────────────────────────────
        if (state.daily.isNotEmpty()) {
            SectionTitle("Předpověď na 7 dní")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
            ) {
                items(state.daily.take(7)) { day -> DailyCard(day) }
            }
        }

        state.message?.let {
            Text(it,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp))
        }
    }

    if (showCityPicker) {
        CityPickerDialog(
            current = state.cityLabel,
            onPick = { city, lat, lon ->
                vm.setLocation(city, lat, lon)
                showCityPicker = false
            },
            onDismiss = { showCityPicker = false },
        )
    }
}

@Composable
private fun CurrentHero(state: WeatherUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                        Color(0xFF06B6D4).copy(alpha = 0.22f),
                    ),
                )
            ),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 32.dp, bottom = 28.dp, end = 32.dp),
        ) {
            Text(state.current?.tempText ?: "—",
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 100.sp)
            Text(state.current?.label ?: "Načítám…",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 4.dp))
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                .padding(horizontal = 18.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.WbSunny, null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(28.dp))
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(state.cityLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground)
                    Text("%.2f° · %.2f°".format(state.lat, state.lon),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun DetailStrip(current: WeatherCurrent) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailTile("💨 Vítr", current.windText, Modifier.weight(1f))
        DetailTile("💧 Vlhkost", current.humidityText, Modifier.weight(1f))
        DetailTile("🌡 Pocitově", current.feelsLikeText, Modifier.weight(1f))
        DetailTile("☂ Srážky", current.precipText, Modifier.weight(1f))
    }
}

@Composable
private fun DetailTile(label: String, value: String, modifier: Modifier = Modifier) {
    ZhCard(modifier = modifier) {
        Column {
            Text(label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun HourlyCard(hour: WeatherHourly) {
    ZhCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(hour.timeLabel,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(hour.icon, fontSize = 22.sp)
            Text("%.0f°".format(hour.tempC),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)
            if (hour.precipProb > 0) {
                Text("☂ ${hour.precipProb}%",
                    fontSize = 10.sp,
                    color = Color(0xFF06B6D4))
            }
        }
    }
}

@Composable
private fun DailyCard(day: WeatherDaily) {
    ZhCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(day.dayLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)
            Text(day.icon, fontSize = 32.sp)
            Text("%.0f° / %.0f°".format(day.maxC, day.minC),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)
            if (day.precipMm > 0.1) {
                StatusPill(label = "%.1f mm".format(day.precipMm), tone = Tone.Info)
            }
        }
    }
}

@Composable
private fun RadarSection(lat: Double, lon: Double) {
    SectionTitle("🗺 Meteoradar — Windy.com")
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(540.dp)
        .clip(RoundedCornerShape(20.dp))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    webViewClient = WebViewClient()
                    // Windy.com embed with lat/lon centered on user's city.
                    // Default layer: radar (precipitation). User can switch
                    // to wind/temp/satellite via Windy's own controls.
                    loadUrl(
                        "https://embed.windy.com/embed2.html?" +
                        "lat=$lat&lon=$lon&zoom=7&level=surface&" +
                        "overlay=radar&menu=&message=&marker=&calendar=&" +
                        "pressure=&type=map&location=coordinates&detail=&" +
                        "metricWind=km%2Fh&metricTemp=%C2%B0C&radarRange=-1"
                    )
                }
            },
        )
    }
    Text("Tip: použij D-pad pro skrolování + Zoom; tlačítky vlevo Windy nastavíš jiné vrstvy (vítr, satelit, vlhkost…)",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, start = 4.dp))
}

@Composable
private fun CityPickerDialog(
    current: String,
    onPick: (String, Double, Double) -> Unit,
    onDismiss: () -> Unit,
) {
    val cities = listOf(
        Triple("Praha",   50.08, 14.43),
        Triple("Brno",    49.20, 16.61),
        Triple("Ostrava", 49.83, 18.28),
        Triple("Plzeň",   49.74, 13.38),
        Triple("Liberec", 50.77, 15.06),
        Triple("Olomouc", 49.59, 17.25),
        Triple("Hradec Králové", 50.21, 15.83),
        Triple("Pardubice", 50.04, 15.78),
        Triple("Zlín",    49.22, 17.66),
        Triple("Karlovy Vary", 50.23, 12.87),
        Triple("Bratislava", 48.15, 17.11),
        Triple("Vídeň",   48.21, 16.37),
        Triple("Berlín",  52.52, 13.40),
        Triple("Mnichov", 48.14, 11.58),
        Triple("Londýn",  51.51, -0.13),
        Triple("New York", 40.71, -74.01),
    )
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        androidx.tv.material3.Surface(
            shape = RoundedCornerShape(24.dp),
            colors = androidx.tv.material3.SurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(640.dp),
        ) {
            Column(modifier = Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Search, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp))
                    Text("Vyber město",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 14.dp))
                }
                Text("Aktuální: $current",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    cities.forEach { (city, lat, lon) ->
                        PsBigChoice(
                            title = city,
                            description = "%.2f° / %.2f°".format(lat, lon),
                            icon = Icons.Outlined.WbSunny,
                            selected = city == current,
                            onClick = { onPick(city, lat, lon) },
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    PsSecondaryButton(text = "Zrušit", onClick = onDismiss)
                }
            }
        }
    }
}
