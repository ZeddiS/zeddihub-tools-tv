package com.zeddihub.tv.pair

import android.content.Context
import android.graphics.Bitmap
import android.net.wifi.WifiManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeddihub.tv.BuildConfig
import com.zeddihub.tv.data.prefs.AppPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

data class QrPairUiState(
    val qrBitmap: Bitmap? = null,
    val deviceName: String = "",
    val deviceId: String = "",
    val host: String? = null,
    val port: Int = 53317,
    val fingerprint: String = "",
    val message: String? = null,
)

@HiltViewModel
class QrPairViewModel @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val prefs: AppPrefs,
) : ViewModel() {

    private val _state = MutableStateFlow(QrPairUiState())
    val state: StateFlow<QrPairUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { refresh() }
    }

    fun refresh() = viewModelScope.launch {
        val deviceId = prefs.pairDeviceId.first().ifBlank {
            // First-run: generate stable device ID (persists across launches)
            val gen = "tv-${UUID.randomUUID().toString().replace("-", "").take(16)}"
            prefs.setPairDeviceId(gen); gen
        }
        val fingerprint = prefs.pairFingerprint.first().ifBlank {
            val gen = sha256(deviceId + System.currentTimeMillis())
            prefs.setPairFingerprint(gen); gen
        }
        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL} (TV)"
            .replace("unknown ", "")
            .replace("samsung ", "Samsung ")
            .take(48)

        val host = withContext(Dispatchers.IO) { localIpAddress() }

        val payload = JSONObject().apply {
            put("kind", "zeddihub-tv-pair")
            put("version", BuildConfig.VERSION_NAME)
            put("device_id", deviceId)
            put("name", deviceName)
            put("host", host ?: "")
            put("port", 53317)
            put("fingerprint", fingerprint)
            put("ts", System.currentTimeMillis() / 1000)
        }.toString()

        val bmp = withContext(Dispatchers.Default) { generateQr(payload, 720) }

        _state.value = QrPairUiState(
            qrBitmap = bmp,
            deviceName = deviceName,
            deviceId = deviceId,
            host = host,
            port = 53317,
            fingerprint = fingerprint,
            message = null,
        )
    }

    fun regenerateFingerprint() = viewModelScope.launch {
        val deviceId = prefs.pairDeviceId.first()
        val newFp = sha256("$deviceId-${System.currentTimeMillis()}-${UUID.randomUUID()}")
        prefs.setPairFingerprint(newFp)
        _state.value = _state.value.copy(message = "✓ Nový fingerprint vygenerován — staré spárování zneplatněno.")
        refresh()
    }

    fun forgetAll() = viewModelScope.launch {
        prefs.setPairedDevicesJson("[]")
        _state.value = _state.value.copy(message = "✓ Všechna spárovaná zařízení zapomenuta. Budou se muset spárovat znovu.")
    }

    @Suppress("DEPRECATION")
    private fun localIpAddress(): String? {
        return runCatching {
            val wm = ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ip = wm.connectionInfo?.ipAddress ?: 0
            if (ip == 0) null
            else "%d.%d.%d.%d".format(
                ip and 0xff, (ip shr 8) and 0xff, (ip shr 16) and 0xff, (ip shr 24) and 0xff
            )
        }.getOrNull()
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
