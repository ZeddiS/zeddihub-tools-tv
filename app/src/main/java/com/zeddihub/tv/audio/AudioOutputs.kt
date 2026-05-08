package com.zeddihub.tv.audio

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class AudioOutput(
    val id: Int,
    val name: String,
    val type: String,         // "speaker" | "hdmi" | "bluetooth" | "headphones" | "other"
    val isCurrent: Boolean,
)

/**
 * On Android TV, audio routing is largely system-controlled — we can read
 * what's connected and the current default route, but actually switching
 * usually requires opening Settings → Sound. This wrapper shows the
 * available outputs and provides intents to jump to the right Settings
 * activity for the user to flip the toggle.
 */
@Singleton
class AudioOutputs @Inject constructor(
    @ApplicationContext private val ctx: Context,
) {
    private val am: AudioManager =
        ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun list(): List<AudioOutput> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return emptyList()
        val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        // Best-effort: the default device used for music playback is the
        // one matching the AudioAttributes routing for STREAM_MUSIC. The
        // platform doesn't expose "current" cleanly, so we treat the
        // first non-USB-output device as primary on TV.
        val current = devices.firstOrNull { it.type !in IGNORED_TYPES }?.id
        return devices.filter { it.type !in IGNORED_TYPES }.map { d ->
            AudioOutput(
                id = d.id,
                name = describe(d),
                type = simpleType(d.type),
                isCurrent = d.id == current,
            )
        }
    }

    fun streamVolumePct(): Int {
        val cur = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        return cur * 100 / max
    }

    fun setStreamVolumePct(pct: Int) {
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        am.setStreamVolume(AudioManager.STREAM_MUSIC, (max * pct / 100).coerceIn(0, max), 0)
    }

    fun isMuted(): Boolean = am.getStreamVolume(AudioManager.STREAM_MUSIC) == 0

    fun toggleMute() {
        if (isMuted()) {
            // Restore to a sane default — many TV speakers won't restore
            // their pre-mute level if we just call setStreamVolume(prev),
            // so 50% is a safe "unmute" target.
            am.setStreamVolume(AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, 0)
        } else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
        }
    }

    private fun describe(d: AudioDeviceInfo): String {
        val name = d.productName?.toString().orEmpty().ifBlank { typeLabel(d.type) }
        return "$name (${typeLabel(d.type)})"
    }

    private fun typeLabel(t: Int): String = when (t) {
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Reproduktor"
        AudioDeviceInfo.TYPE_HDMI -> "HDMI"
        AudioDeviceInfo.TYPE_HDMI_ARC -> "HDMI ARC"
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth"
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth headset"
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Sluchátka"
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Headset"
        AudioDeviceInfo.TYPE_USB_DEVICE -> "USB audio"
        AudioDeviceInfo.TYPE_USB_HEADSET -> "USB headset"
        AudioDeviceInfo.TYPE_AUX_LINE -> "Aux"
        AudioDeviceInfo.TYPE_LINE_DIGITAL -> "Optický (digital)"
        AudioDeviceInfo.TYPE_LINE_ANALOG -> "Aux (analog)"
        AudioDeviceInfo.TYPE_TELEPHONY -> "Telefon"
        AudioDeviceInfo.TYPE_FM -> "FM"
        else -> "Jiné"
    }

    private fun simpleType(t: Int): String = when (t) {
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "speaker"
        AudioDeviceInfo.TYPE_HDMI, AudioDeviceInfo.TYPE_HDMI_ARC -> "hdmi"
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "bluetooth"
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES, AudioDeviceInfo.TYPE_WIRED_HEADSET -> "headphones"
        else -> "other"
    }

    companion object {
        private val IGNORED_TYPES = setOf(
            AudioDeviceInfo.TYPE_TELEPHONY, AudioDeviceInfo.TYPE_FM,
            AudioDeviceInfo.TYPE_BUILTIN_EARPIECE,
        )
    }
}
