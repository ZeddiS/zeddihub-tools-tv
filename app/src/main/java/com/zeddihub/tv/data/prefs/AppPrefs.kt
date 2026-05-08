package com.zeddihub.tv.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "zeddihub_tv_prefs")

@Singleton
class AppPrefs @Inject constructor(
    @ApplicationContext private val ctx: Context,
) {
    private val ds = ctx.dataStore

    // Theme: "system" | "light" | "dark" | "amoled"
    val theme: Flow<String> = ds.data.map { it[KEY_THEME] ?: "dark" }
    suspend fun setTheme(v: String) = ds.edit { it[KEY_THEME] = v }

    // Language: "auto" | "cs" | "en"
    val language: Flow<String> = ds.data.map { it[KEY_LANG] ?: "auto" }
    suspend fun setLanguage(v: String) = ds.edit { it[KEY_LANG] = v }

    // Trigger button keycode (default KEYCODE_DPAD_CENTER = 23)
    val timerTriggerKey: Flow<Int> = ds.data.map { it[KEY_TIMER_TRIGGER] ?: 23 }
    suspend fun setTimerTriggerKey(v: Int) = ds.edit { it[KEY_TIMER_TRIGGER] = v }

    // Timer overlay corner: 0=TopLeft, 1=TopRight (default), 2=BottomLeft, 3=BottomRight
    val timerCorner: Flow<Int> = ds.data.map { it[KEY_TIMER_CORNER] ?: 1 }
    suspend fun setTimerCorner(v: Int) = ds.edit { it[KEY_TIMER_CORNER] = v }

    // Volume fade-out (last 10 s) on/off
    val timerFadeAudio: Flow<Boolean> = ds.data.map { it[KEY_TIMER_FADE] ?: true }
    suspend fun setTimerFadeAudio(v: Boolean) = ds.edit { it[KEY_TIMER_FADE] = v }

    // WoL devices stored as JSON array string
    val wolDevicesJson: Flow<String> = ds.data.map { it[KEY_WOL_DEVICES] ?: "[]" }
    suspend fun setWolDevicesJson(v: String) = ds.edit { it[KEY_WOL_DEVICES] = v }

    // Plex server (host:port + token)
    val plexHost: Flow<String> = ds.data.map { it[KEY_PLEX_HOST] ?: "" }
    val plexToken: Flow<String> = ds.data.map { it[KEY_PLEX_TOKEN] ?: "" }
    suspend fun setPlex(host: String, token: String) = ds.edit {
        it[KEY_PLEX_HOST] = host
        it[KEY_PLEX_TOKEN] = token
    }

    // Kodi server
    val kodiHost: Flow<String> = ds.data.map { it[KEY_KODI_HOST] ?: "" }
    val kodiUser: Flow<String> = ds.data.map { it[KEY_KODI_USER] ?: "kodi" }
    val kodiPass: Flow<String> = ds.data.map { it[KEY_KODI_PASS] ?: "" }
    suspend fun setKodi(host: String, user: String, pass: String) = ds.edit {
        it[KEY_KODI_HOST] = host
        it[KEY_KODI_USER] = user
        it[KEY_KODI_PASS] = pass
    }

    // Last seen update version code (so we don't re-prompt)
    val updateDismissedCode: Flow<Int> = ds.data.map { it[KEY_UPDATE_DISMISSED] ?: 0 }
    suspend fun setUpdateDismissedCode(v: Int) = ds.edit { it[KEY_UPDATE_DISMISSED] = v }

    // Sleep schedules — JSON list of Schedule
    val schedulesJson: Flow<String> = ds.data.map { it[KEY_SCHEDULES] ?: "[]" }
    suspend fun setSchedulesJson(v: String) = ds.edit { it[KEY_SCHEDULES] = v }

    // Bedtime routine — JSON list of routine steps
    val bedtimeRoutineJson: Flow<String> = ds.data.map { it[KEY_BEDTIME_ROUTINE] ?: "[]" }
    suspend fun setBedtimeRoutineJson(v: String) = ds.edit { it[KEY_BEDTIME_ROUTINE] = v }

    // Health alert: temperature threshold (°C), 0 = disabled
    val healthTempThreshold: Flow<Int> = ds.data.map { it[KEY_HEALTH_TEMP] ?: 70 }
    suspend fun setHealthTempThreshold(v: Int) = ds.edit { it[KEY_HEALTH_TEMP] = v }

    // Alerts: list of seen alert IDs (so we don't re-show the same one).
    val alertsSeenJson: Flow<String> = ds.data.map { it[KEY_ALERTS_SEEN] ?: "[]" }
    suspend fun setAlertsSeenJson(v: String) = ds.edit { it[KEY_ALERTS_SEEN] = v }

    // Home Assistant — base URL + long-lived access token
    val hassBaseUrl: Flow<String> = ds.data.map { it[KEY_HASS_URL] ?: "" }
    val hassToken: Flow<String> = ds.data.map { it[KEY_HASS_TOKEN] ?: "" }
    suspend fun setHass(url: String, token: String) = ds.edit {
        it[KEY_HASS_URL] = url
        it[KEY_HASS_TOKEN] = token
    }
    // Pinned HA entities to show as quick toggles on the SmartHome screen
    val hassPinnedJson: Flow<String> = ds.data.map { it[KEY_HASS_PINNED] ?: "[]" }
    suspend fun setHassPinnedJson(v: String) = ds.edit { it[KEY_HASS_PINNED] = v }

    // Smart sleep auto-detect — minutes of input idleness after which the
    // app proactively offers a shutdown nudge. 0 = disabled.
    val smartSleepIdleMinutes: Flow<Int> = ds.data.map { it[KEY_SMART_SLEEP] ?: 0 }
    suspend fun setSmartSleepIdleMinutes(v: Int) = ds.edit { it[KEY_SMART_SLEEP] = v }

    // Universal CC + dyslexia-friendly font toggles
    val ccUniversalEnabled: Flow<Boolean> = ds.data.map { it[KEY_CC_UNIVERSAL] ?: false }
    suspend fun setCcUniversalEnabled(v: Boolean) = ds.edit { it[KEY_CC_UNIVERSAL] = v }
    val dyslexiaFontEnabled: Flow<Boolean> = ds.data.map { it[KEY_DYSLEXIA_FONT] ?: false }
    suspend fun setDyslexiaFontEnabled(v: Boolean) = ds.edit { it[KEY_DYSLEXIA_FONT] = v }

    // Parental controls — PIN (4-6 digits, "" = not set), per-app daily quotas
    val parentalPin: Flow<String> = ds.data.map { it[KEY_PARENTAL_PIN] ?: "" }
    suspend fun setParentalPin(v: String) = ds.edit { it[KEY_PARENTAL_PIN] = v }
    val parentalQuotasJson: Flow<String> = ds.data.map { it[KEY_PARENTAL_QUOTAS] ?: "[]" }
    suspend fun setParentalQuotasJson(v: String) = ds.edit { it[KEY_PARENTAL_QUOTAS] = v }

    // Wake-up alarms — JSON list of WakeUp
    val wakeupsJson: Flow<String> = ds.data.map { it[KEY_WAKEUPS] ?: "[]" }
    suspend fun setWakeupsJson(v: String) = ds.edit { it[KEY_WAKEUPS] = v }

    // Smart-home devices (Hue/Tasmota/Tuya) — JSON list of SmartDevice
    val smartDevicesJson: Flow<String> = ds.data.map { it[KEY_SMART_DEVICES] ?: "[]" }
    suspend fun setSmartDevicesJson(v: String) = ds.edit { it[KEY_SMART_DEVICES] = v }

    // Watch later inbox cache (last fetched JSON list of items)
    val watchLaterJson: Flow<String> = ds.data.map { it[KEY_WATCHLATER] ?: "[]" }
    suspend fun setWatchLaterJson(v: String) = ds.edit { it[KEY_WATCHLATER] = v }

    // LocalSend receive directory (Storage Access Framework URI; null = default)
    val localSendDir: Flow<String> = ds.data.map { it[KEY_LOCALSEND_DIR] ?: "" }
    suspend fun setLocalSendDir(v: String) = ds.edit { it[KEY_LOCALSEND_DIR] = v }

    companion object {
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_LANG = stringPreferencesKey("lang")
        private val KEY_TIMER_TRIGGER = intPreferencesKey("timer_trigger_key")
        private val KEY_TIMER_CORNER = intPreferencesKey("timer_corner")
        private val KEY_TIMER_FADE = booleanPreferencesKey("timer_fade")
        private val KEY_WOL_DEVICES = stringPreferencesKey("wol_devices_json")
        private val KEY_PLEX_HOST = stringPreferencesKey("plex_host")
        private val KEY_PLEX_TOKEN = stringPreferencesKey("plex_token")
        private val KEY_KODI_HOST = stringPreferencesKey("kodi_host")
        private val KEY_KODI_USER = stringPreferencesKey("kodi_user")
        private val KEY_KODI_PASS = stringPreferencesKey("kodi_pass")
        private val KEY_UPDATE_DISMISSED = intPreferencesKey("update_dismissed_code")
        private val KEY_SCHEDULES = stringPreferencesKey("schedules_json")
        private val KEY_BEDTIME_ROUTINE = stringPreferencesKey("bedtime_routine_json")
        private val KEY_HEALTH_TEMP = intPreferencesKey("health_temp_threshold_c")
        private val KEY_ALERTS_SEEN = stringPreferencesKey("alerts_seen_json")
        private val KEY_HASS_URL = stringPreferencesKey("hass_base_url")
        private val KEY_HASS_TOKEN = stringPreferencesKey("hass_token")
        private val KEY_HASS_PINNED = stringPreferencesKey("hass_pinned_json")
        private val KEY_SMART_SLEEP = intPreferencesKey("smart_sleep_idle_min")
        private val KEY_CC_UNIVERSAL = booleanPreferencesKey("cc_universal_enabled")
        private val KEY_DYSLEXIA_FONT = booleanPreferencesKey("dyslexia_font_enabled")
        private val KEY_PARENTAL_PIN = stringPreferencesKey("parental_pin")
        private val KEY_PARENTAL_QUOTAS = stringPreferencesKey("parental_quotas_json")
        private val KEY_WAKEUPS = stringPreferencesKey("wakeups_json")
        private val KEY_SMART_DEVICES = stringPreferencesKey("smart_devices_json")
        private val KEY_WATCHLATER = stringPreferencesKey("watch_later_json")
        private val KEY_LOCALSEND_DIR = stringPreferencesKey("localsend_dir")
    }
}
