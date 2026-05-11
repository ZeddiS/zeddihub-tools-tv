package com.zeddihub.tv.pair

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.PsPrimaryButton
import com.zeddihub.tv.ui.components.PsSecondaryButton
import com.zeddihub.tv.ui.components.SectionTitle
import com.zeddihub.tv.ui.components.StatusPill
import com.zeddihub.tv.ui.components.Tone
import com.zeddihub.tv.ui.components.ZhCard
import com.zeddihub.tv.ui.components.ZhPageScaffold

/**
 * v0.1.15 — Spárování telefonu přes QR kód.
 *
 * Mechanism (user-chosen: ZeddiHub Mobile pairing):
 *   1. TV běží jako LocalSend server (port 53317) + má unique device ID
 *      generovaný při prvním spuštění a uložený v DataStore
 *   2. Screen generuje QR kód obsahující JSON:
 *      { "kind": "zeddihub-tv-pair", "device_id": "...", "host": "192.168.x.x",
 *        "port": 53317, "name": "Living room TV", "fingerprint": "..." }
 *   3. Mobile ZeddiHub app naskenuje QR, vidí device info, klikne „Spárovat",
 *      uloží do mobile DataStore. Od té doby může posílat data přímo na TV
 *      přes LAN bez ručního zadávání IP
 *   4. Standardní LocalSend kompatibilita zachována (TV pořád přijímá z
 *      veřejné LocalSend app), ZeddiHub mobile používá vlastní pair flow
 *
 * Status v této screen jen ukazuje QR + IP + port + fingerprint + reset button.
 * Skutečný pair protokol (mobile-side handshake) přijde v ZeddiHub mobile 0.9.4+.
 */
@Composable
fun QrPairScreen(vm: QrPairViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    ZhPageScaffold {
        PageHeader(
            title = "Spárovat telefon",
            subtitle = "Naskenuj QR kód pomocí ZeddiHub Mobile aplikace. Pak můžeš posílat soubory, ovládat časovač a přijímat notifikace.",
            icon = Icons.Outlined.QrCode2,
            trailing = {
                PsSecondaryButton(text = "🔄 Obnovit", onClick = { vm.refresh() })
            },
        )

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)) {

            // ── QR card ─────────────────────────────────────
            ZhCard(
                modifier = Modifier.weight(1f),
                container = Color.White,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Naskenuj v ZeddiHub Mobile",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF14141F))
                    state.qrBitmap?.let { bmp ->
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "QR code",
                            modifier = Modifier
                                .size(280.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White),
                        )
                    } ?: Box(
                        modifier = Modifier
                            .size(280.dp)
                            .background(Color(0xFFEEEEF5), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("⌛ Generuji…", fontSize = 14.sp, color = Color(0xFF14141F))
                    }
                    Text("ZeddiHub Mobile · Účet · Spárovat zařízení",
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280))
                }
            }

            // ── Connection info ─────────────────────────────
            ZhCard(modifier = Modifier.weight(1.3f)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Informace o spárování",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground)
                    InfoRow("Název TV", state.deviceName)
                    InfoRow("Device ID", state.deviceId.take(16) + "…")
                    InfoRow("Adresa", state.host ?: "—")
                    InfoRow("Port", state.port.toString())
                    InfoRow("Fingerprint", state.fingerprint.take(16) + "…")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusPill(
                            label = if (state.host != null) "LAN dostupné" else "Bez sítě",
                            tone = if (state.host != null) Tone.Success else Tone.Warning,
                        )
                        StatusPill(label = "LocalSend kompatibilní", tone = Tone.Info)
                    }
                }
            }
        }

        SectionTitle("Co můžeš dělat po spárování")
        ZhCard {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FeatureBullet("📤", "Posílat soubory z mobilu přes LocalSend (foto, video, dokumenty)")
                FeatureBullet("⏰", "Ovládat Sleep Timer z mobilu (Start/Pauza/+5min/Stop)")
                FeatureBullet("🌐", "Sdílet odkazy → otevřou se v TV prohlížeči / Watch later")
                FeatureBullet("🔔", "Přijímat push notifikace na TV (admin alerty, server-down)")
                FeatureBullet("📊", "Vidět TV diagnostiku z mobilu (Wi-Fi, teplota, RAM)")
            }
        }

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PsPrimaryButton(text = "🔄 Vygenerovat nový QR", onClick = { vm.regenerateFingerprint() })
            PsSecondaryButton(text = "🗑 Zapomenout všechna zařízení", onClick = { vm.forgetAll() })
        }

        state.message?.let {
            Text(it,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp).fillMaxWidth(0.32f))
        Text(value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f))
    }
}

@Composable
private fun FeatureBullet(emoji: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 18.sp, modifier = Modifier.padding(end = 12.dp))
        Text(text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground)
    }
}

/**
 * Generic QR-from-string helper. Returns ARGB bitmap (B/W) of given size.
 * Uses ZXing core.
 */
fun generateQr(content: String, sizePx: Int = 600): Bitmap? = runCatching {
    val writer = QRCodeWriter()
    val matrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
    val bmp = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
    for (x in 0 until matrix.width) {
        for (y in 0 until matrix.height) {
            bmp.setPixel(x, y,
                if (matrix.get(x, y)) android.graphics.Color.BLACK
                else android.graphics.Color.WHITE)
        }
    }
    bmp
}.getOrNull()
