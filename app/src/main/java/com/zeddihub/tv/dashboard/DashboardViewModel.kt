package com.zeddihub.tv.dashboard

import android.app.ActivityManager
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class NowText(val timeStr: String, val dateStr: String)
data class SysInfo(val ramText: String, val storageText: String, val networkText: String)
data class WeatherInfo(val tempText: String, val label: String)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val appCtx: Context,
    private val client: OkHttpClient,
    private val moshi: Moshi,
) : ViewModel() {

    private val _now = MutableStateFlow(NowText("", ""))
    val now: StateFlow<NowText> = _now.asStateFlow()

    private val _sysInfo = MutableStateFlow(SysInfo("—", "—", "—"))
    val sysInfo: StateFlow<SysInfo> = _sysInfo.asStateFlow()

    private val _weather = MutableStateFlow(WeatherInfo("—", "Načítám…"))
    val weather: StateFlow<WeatherInfo> = _weather.asStateFlow()

    init {
        // Tick clock every second
        viewModelScope.launch {
            while (true) {
                _now.value = formatNow()
                delay(1_000)
            }
        }
    }

    fun refresh(ctx: Context) {
        viewModelScope.launch {
            _sysInfo.value = readSysInfo(ctx)
        }
        viewModelScope.launch {
            _weather.value = fetchWeather()
        }
    }

    private fun formatNow(): NowText {
        val locale = Locale.forLanguageTag("cs")
        val time = SimpleDateFormat("HH:mm:ss", locale).format(Date())
        val date = SimpleDateFormat("EEEE d. MMMM yyyy", locale).format(Date())
            .replaceFirstChar { it.titlecase(locale) }
        return NowText(time, date)
    }

    private fun readSysInfo(ctx: Context): SysInfo {
        val ram = runCatching {
            val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mi = ActivityManager.MemoryInfo()
            am.getMemoryInfo(mi)
            val total = mi.totalMem / (1024L * 1024L)
            val avail = mi.availMem / (1024L * 1024L)
            val used = total - avail
            "$used / $total MB"
        }.getOrDefault("—")

        val storage = runCatching {
            val stat = StatFs(Environment.getDataDirectory().path)
            val total = (stat.blockSizeLong * stat.blockCountLong) / (1024L * 1024L * 1024L)
            val avail = (stat.blockSizeLong * stat.availableBlocksLong) / (1024L * 1024L * 1024L)
            val used = total - avail
            "$used / $total GB"
        }.getOrDefault("—")

        val net = runCatching {
            val wm = ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info = wm.connectionInfo
            val ssid = (info?.ssid ?: "").trim('"')
            val rssi = info?.rssi ?: 0
            val ip = info?.ipAddress ?: 0
            val ipStr = if (ip != 0) "%d.%d.%d.%d".format(ip and 0xff, (ip shr 8) and 0xff, (ip shr 16) and 0xff, (ip shr 24) and 0xff) else "—"
            "$ssid · $ipStr · $rssi dBm"
        }.getOrDefault("—")

        return SysInfo(ram, storage, net)
    }

    private suspend fun fetchWeather(): WeatherInfo = withContext(Dispatchers.IO) {
        // Open-Meteo, no API key. Default Prague (50.08, 14.43).
        val url = "https://api.open-meteo.com/v1/forecast?latitude=50.08&longitude=14.43&current=temperature_2m,weather_code"
        runCatching {
            val resp = client.newCall(Request.Builder().url(url).build()).execute()
            val body = resp.body?.string() ?: return@runCatching null
            val adapter = moshi.adapter(OpenMeteoResp::class.java)
            adapter.fromJson(body)
        }.getOrNull()?.let {
            val t = it.current?.temperature_2m
            val code = it.current?.weather_code ?: -1
            WeatherInfo(
                tempText = if (t != null) "%.1f °C".format(t) else "—",
                label = weatherLabel(code),
            )
        } ?: WeatherInfo("—", "Offline")
    }

    private fun weatherLabel(code: Int): String = when (code) {
        0 -> "Jasno"
        1, 2 -> "Skoro jasno"
        3 -> "Zataženo"
        45, 48 -> "Mlha"
        51, 53, 55 -> "Mrholení"
        61, 63, 65, 80, 81, 82 -> "Déšť"
        71, 73, 75, 85, 86 -> "Sníh"
        95, 96, 99 -> "Bouřka"
        else -> "—"
    }
}

@JsonClass(generateAdapter = true)
data class OpenMeteoResp(val current: OpenMeteoCurrent?)

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrent(val temperature_2m: Double?, val weather_code: Int?)
