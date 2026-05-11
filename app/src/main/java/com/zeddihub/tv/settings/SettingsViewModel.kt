package com.zeddihub.tv.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeddihub.tv.data.prefs.AppPrefs
import com.zeddihub.tv.data.update.UpdateCheckResult
import com.zeddihub.tv.data.update.UpdateChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * v0.1.13 — split into 3 explicit states + 3 explicit actions so the
 * Settings screen can render both "Zkontrolovat" and "Spustit aktualizaci"
 * (and a banner with the available version) as distinct affordances.
 *
 *   checking   — currently hitting /api/app-version.php
 *   installing — DownloadManager handed an APK URL; system installer
 *                will pop up when the file finishes streaming
 *   available  — last successful check returned an update; populated
 *                when check() succeeds with a newer version_code than
 *                BuildConfig.VERSION_CODE. Cleared after install().
 *   message    — human-readable status line shown below the buttons.
 */
data class UpdateUiState(
    val checking:  Boolean = false,
    val installing: Boolean = false,
    val available: UpdateCheckResult? = null,
    val message:   String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPrefs,
    private val updateChecker: UpdateChecker,
) : ViewModel() {

    val theme: StateFlow<String> = prefs.theme.stateIn(viewModelScope, SharingStarted.Eagerly, "dark")
    val language: StateFlow<String> = prefs.language.stateIn(viewModelScope, SharingStarted.Eagerly, "auto")
    val triggerKey: StateFlow<Int> = prefs.timerTriggerKey.stateIn(viewModelScope, SharingStarted.Eagerly, 23)
    val corner: StateFlow<Int> = prefs.timerCorner.stateIn(viewModelScope, SharingStarted.Eagerly, 1)
    val fade: StateFlow<Boolean> = prefs.timerFadeAudio.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val smartSleep: StateFlow<Int> = prefs.smartSleepIdleMinutes.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val healthTemp: StateFlow<Int> = prefs.healthTempThreshold.stateIn(viewModelScope, SharingStarted.Eagerly, 70)
    val ccUniversal: StateFlow<Boolean> = prefs.ccUniversalEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val dyslexiaFont: StateFlow<Boolean> = prefs.dyslexiaFontEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _updateState = MutableStateFlow(UpdateUiState())
    val updateState: StateFlow<UpdateUiState> = _updateState

    private val _resetMessage = MutableStateFlow<String?>(null)
    val resetMessage: StateFlow<String?> = _resetMessage

    fun setTheme(v: String) = viewModelScope.launch { prefs.setTheme(v) }
    fun setLanguage(v: String) = viewModelScope.launch { prefs.setLanguage(v) }
    fun setTriggerKey(v: Int) = viewModelScope.launch { prefs.setTimerTriggerKey(v) }
    fun setCorner(v: Int) = viewModelScope.launch { prefs.setTimerCorner(v) }
    fun setFade(v: Boolean) = viewModelScope.launch { prefs.setTimerFadeAudio(v) }
    fun setSmartSleep(v: Int) = viewModelScope.launch { prefs.setSmartSleepIdleMinutes(v) }
    fun setHealthTemp(v: Int) = viewModelScope.launch { prefs.setHealthTempThreshold(v) }
    fun setCcUniversal(v: Boolean) = viewModelScope.launch { prefs.setCcUniversalEnabled(v) }
    fun setDyslexiaFont(v: Boolean) = viewModelScope.launch { prefs.setDyslexiaFontEnabled(v) }

    /**
     * Reset uživatelských preferencí na výchozí hodnoty (jako kdyby uživatel
     * appku nově nainstaloval). NEMAZE wakeups / parental pin (citlivé). Spustí
     * znovu Setup Wizard při příštím cold startu.
     */
    fun resetSettings() = viewModelScope.launch {
        prefs.setTheme("dark")
        prefs.setLanguage("auto")
        prefs.setTimerTriggerKey(23)
        prefs.setTimerCorner(1)
        prefs.setTimerFadeAudio(true)
        prefs.setSmartSleepIdleMinutes(0)
        prefs.setHealthTempThreshold(70)
        prefs.setCcUniversalEnabled(false)
        prefs.setDyslexiaFontEnabled(false)
        prefs.setBrowserBookmarksJson("[]")
        prefs.setBrowserHomeUrl("https://duckduckgo.com")
        prefs.setFavoriteRoutesJson("[]")
        prefs.setWizardCompleted(false)
        _resetMessage.value = "✓ Nastavení vráceno do výchozích hodnot. Wizard se znovu spustí při příštím restartu aplikace."
    }

    fun clearResetMessage() { _resetMessage.value = null }

    /**
     * Just check — populate `available` if there's a newer version, never
     * trigger DownloadManager. UI exposes this as the "🔄 Zkontrolovat"
     * button so the user can verify before committing.
     */
    fun check() = viewModelScope.launch {
        _updateState.value = _updateState.value.copy(checking = true, message = null)
        val result = updateChecker.checkNow()
        _updateState.value = when {
            result == null -> _updateState.value.copy(
                checking = false,
                message = "Server nedostupný — zkus to později.",
            )
            result.isUpdateAvailable -> _updateState.value.copy(
                checking = false,
                available = result,
                message = "✓ Nová verze ${result.versionName} je dostupná.",
            )
            else -> _updateState.value.copy(
                checking = false,
                available = null,
                message = "✓ Máte nejnovější verzi.",
            )
        }
    }

    /**
     * Install the version stored in `available`. If we don't have one
     * (user clicked install before check), do an inline check first.
     * Exposed as the "🚀 Spustit aktualizaci" button.
     */
    fun installNow(ctx: Context) = viewModelScope.launch {
        val current = _updateState.value
        _updateState.value = current.copy(installing = true, message = "Připravuji aktualizaci…")
        val result = current.available ?: updateChecker.checkNow()
        when {
            result == null -> _updateState.value = _updateState.value.copy(
                installing = false,
                message = "✗ Server nedostupný — nelze stáhnout aktualizaci.",
            )
            !result.isUpdateAvailable -> _updateState.value = _updateState.value.copy(
                installing = false,
                available = null,
                message = "✓ Už máte nejnovější verzi (${result.versionName}).",
            )
            else -> {
                updateChecker.startInstall(ctx, result)
                _updateState.value = _updateState.value.copy(
                    installing = false,
                    available = result,
                    message = "⬇ Stahuji ${result.versionName}… Po dokončení se otevře instalátor.",
                )
            }
        }
    }
}
