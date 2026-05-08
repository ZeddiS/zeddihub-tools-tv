package com.zeddihub.tv.smarthome

import com.squareup.moshi.JsonClass

/**
 * A configured smart-home device the user can control from TV.
 * Each device has a [kind] that picks the wire protocol.
 *
 * - `hue_light`   : Philips Hue light by index (host = bridge IP, token = username, target = light id)
 * - `hue_group`   : Philips Hue room/group (target = group id)
 * - `tasmota`     : Tasmota REST device (host = device IP, target = "Power" or "Color")
 * - `tuya_local`  : Tuya local-key device (placeholder; full LAN support arrives in v0.4.0)
 * - `webhook`     : raw HTTP GET/POST (catch-all for IFTTT, HA webhooks, MQTT-HTTP bridges)
 */
@JsonClass(generateAdapter = true)
data class SmartDevice(
    val id: String,
    val name: String,
    val kind: String,                 // see KDoc
    val host: String = "",            // IP or hostname
    val token: String = "",           // bridge username / API key / Tasmota password
    val target: String = "",          // light id, group id, parameter name
    val webhookUrl: String = "",      // for webhook kind
    val webhookMethod: String = "POST",
    val icon: String = "💡",
)

object SmartKinds {
    const val HUE_LIGHT  = "hue_light"
    const val HUE_GROUP  = "hue_group"
    const val TASMOTA    = "tasmota"
    const val TUYA_LOCAL = "tuya_local"
    const val WEBHOOK    = "webhook"

    fun displayName(kind: String): String = when (kind) {
        HUE_LIGHT  -> "Hue světlo"
        HUE_GROUP  -> "Hue místnost"
        TASMOTA    -> "Tasmota"
        TUYA_LOCAL -> "Tuya (LAN)"
        WEBHOOK    -> "Webhook"
        else       -> kind
    }
}
