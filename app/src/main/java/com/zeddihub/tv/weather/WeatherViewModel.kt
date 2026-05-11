package com.zeddihub.tv.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.zeddihub.tv.data.prefs.AppPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Pro weather model — pulls forecast + hourly + daily from Open-Meteo.
 * Free, no API key, decent global coverage. City selection is persisted
 * via AppPrefs.weatherLat/Lon/Label so the Dashboard widget and this
 * screen stay in sync.
 *
 * Radar source = Windy.com WebView (user-chosen in 0.1.15 questionnaire).
 */
data class WeatherCurrent(
    val tempText: String,
    val label: String,
    val windText: String,
    val humidityText: String,
    val feelsLikeText: String,
    val precipText: String,
)
data class WeatherHourly(
    val timeLabel: String,
    val tempC: Double,
    val icon: String,
    val precipProb: Int,
)
data class WeatherDaily(
    val dayLabel: String,
    val minC: Double,
    val maxC: Double,
    val precipMm: Double,
    val icon: String,
)
data class WeatherUiState(
    val cityLabel: String = "Praha",
    val lat: Double = 50.08,
    val lon: Double = 14.43,
    val current: WeatherCurrent? = null,
    val hourly: List<WeatherHourly> = emptyList(),
    val daily: List<WeatherDaily> = emptyList(),
    val lastUpdated: String? = null,
    val message: String? = null,
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val client: OkHttpClient,
    private val moshi: Moshi,
    private val prefs: AppPrefs,
) : ViewModel() {

    private val _state = MutableStateFlow(WeatherUiState())
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val lat = prefs.weatherLat.first()
            val lon = prefs.weatherLon.first()
            val label = prefs.weatherLabel.first()
            _state.value = _state.value.copy(cityLabel = label, lat = lat, lon = lon)
            fetch(label, lat, lon)
        }
    }

    fun refresh() = viewModelScope.launch {
        val s = _state.value
        fetch(s.cityLabel, s.lat, s.lon)
    }

    fun setLocation(label: String, lat: Double, lon: Double) = viewModelScope.launch {
        prefs.setWeather(lat, lon, label)
        _state.value = _state.value.copy(cityLabel = label, lat = lat, lon = lon, message = "Aktualizuji…")
        fetch(label, lat, lon)
    }

    private suspend fun fetch(label: String, lat: Double, lon: Double) = withContext(Dispatchers.IO) {
        val url = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=$lat&longitude=$lon" +
            "&current=temperature_2m,weather_code,relative_humidity_2m,apparent_temperature,precipitation,wind_speed_10m" +
            "&hourly=temperature_2m,weather_code,precipitation_probability" +
            "&daily=temperature_2m_min,temperature_2m_max,weather_code,precipitation_sum" +
            "&timezone=auto&forecast_days=7"
        runCatching {
            val resp = client.newCall(Request.Builder().url(url).build()).execute()
            val body = resp.body?.string()
            resp.close()
            if (body.isNullOrBlank()) return@withContext
            val parsed = moshi.adapter(OpenMeteoResp::class.java).fromJson(body) ?: return@withContext

            val curr = parsed.current?.let { c ->
                WeatherCurrent(
                    tempText = c.temperature_2m?.let { "%.1f °C".format(it) } ?: "—",
                    label = weatherLabel(c.weather_code ?: -1),
                    windText = c.wind_speed_10m?.let { "%.0f km/h".format(it) } ?: "—",
                    humidityText = c.relative_humidity_2m?.let { "$it %" } ?: "—",
                    feelsLikeText = c.apparent_temperature?.let { "%.1f °C".format(it) } ?: "—",
                    precipText = c.precipitation?.let { "%.1f mm".format(it) } ?: "0 mm",
                )
            }
            val now = System.currentTimeMillis() / 1000L
            val timeFmt = SimpleDateFormat("HH'h'", Locale("cs"))
            val hourly = parsed.hourly?.let { h ->
                val times = h.time ?: emptyList()
                val temps = h.temperature_2m ?: emptyList()
                val codes = h.weather_code ?: emptyList()
                val probs = h.precipitation_probability ?: emptyList()
                val all = times.indices.mapNotNull { i ->
                    val t = times.getOrNull(i) ?: return@mapNotNull null
                    val ts = parseISO(t) ?: return@mapNotNull null
                    if (ts < now) return@mapNotNull null
                    WeatherHourly(
                        timeLabel = timeFmt.format(Date(ts * 1000L)),
                        tempC = temps.getOrNull(i) ?: 0.0,
                        icon = weatherIcon(codes.getOrNull(i) ?: -1),
                        precipProb = probs.getOrNull(i) ?: 0,
                    )
                }
                all.take(24)
            } ?: emptyList()

            val dayFmt = SimpleDateFormat("EEE", Locale("cs"))
            val daily = parsed.daily?.let { d ->
                val times = d.time ?: emptyList()
                val mins = d.temperature_2m_min ?: emptyList()
                val maxs = d.temperature_2m_max ?: emptyList()
                val codes = d.weather_code ?: emptyList()
                val precs = d.precipitation_sum ?: emptyList()
                times.indices.mapNotNull { i ->
                    val t = times.getOrNull(i) ?: return@mapNotNull null
                    val ts = parseISODate(t) ?: return@mapNotNull null
                    WeatherDaily(
                        dayLabel = dayFmt.format(Date(ts * 1000L))
                            .replaceFirstChar { it.titlecase(Locale("cs")) },
                        minC = mins.getOrNull(i) ?: 0.0,
                        maxC = maxs.getOrNull(i) ?: 0.0,
                        precipMm = precs.getOrNull(i) ?: 0.0,
                        icon = weatherIcon(codes.getOrNull(i) ?: -1),
                    )
                }
            } ?: emptyList()

            val updatedAt = SimpleDateFormat("HH:mm", Locale("cs")).format(Date())
            _state.value = _state.value.copy(
                cityLabel = label, lat = lat, lon = lon,
                current = curr, hourly = hourly, daily = daily,
                lastUpdated = updatedAt, message = null,
            )
        }.onFailure {
            _state.value = _state.value.copy(message = "Síť nedostupná: ${it.message}")
        }
    }

    // ── ISO date parsing helpers (Open-Meteo returns "2026-05-11T13:00") ─
    private fun parseISO(t: String): Long? = runCatching {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).parse(t)?.time?.div(1000)
    }.getOrNull()

    private fun parseISODate(t: String): Long? = runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(t)?.time?.div(1000)
    }.getOrNull()

    private fun weatherIcon(code: Int): String = when (code) {
        0 -> "☀️"; 1, 2 -> "🌤"; 3 -> "☁️"
        45, 48 -> "🌫"
        51, 53, 55 -> "🌦"
        61, 63, 65, 80, 81, 82 -> "🌧"
        71, 73, 75, 85, 86 -> "❄️"
        95, 96, 99 -> "⛈"
        else -> "❓"
    }

    private fun weatherLabel(code: Int): String = when (code) {
        0 -> "Jasno"; 1, 2 -> "Skoro jasno"; 3 -> "Zataženo"
        45, 48 -> "Mlha"
        51, 53, 55 -> "Mrholení"
        61, 63, 65, 80, 81, 82 -> "Déšť"
        71, 73, 75, 85, 86 -> "Sníh"
        95, 96, 99 -> "Bouřka"
        else -> "—"
    }
}

@JsonClass(generateAdapter = true)
data class OpenMeteoResp(
    val current: OMCurrent?,
    val hourly: OMHourly?,
    val daily: OMDaily?,
)
@JsonClass(generateAdapter = true)
data class OMCurrent(
    val temperature_2m: Double?,
    val weather_code: Int?,
    val relative_humidity_2m: Int?,
    val apparent_temperature: Double?,
    val precipitation: Double?,
    val wind_speed_10m: Double?,
)
@JsonClass(generateAdapter = true)
data class OMHourly(
    val time: List<String>?,
    val temperature_2m: List<Double>?,
    val weather_code: List<Int>?,
    val precipitation_probability: List<Int>?,
)
@JsonClass(generateAdapter = true)
data class OMDaily(
    val time: List<String>?,
    val temperature_2m_min: List<Double>?,
    val temperature_2m_max: List<Double>?,
    val weather_code: List<Int>?,
    val precipitation_sum: List<Double>?,
)
