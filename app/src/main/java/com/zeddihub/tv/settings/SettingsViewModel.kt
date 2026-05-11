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
    val triggerKey: StateFlow<Int> = prefs.timerTriggerKey.stateIn(viewModelScope, SharingStarted.Eagerly, 23)
    val corner: StateFlow<Int> = prefs.timerCorner.stateIn(viewModelScope, SharingStarted.Eagerly, 1)
    val fade: StateFlow<Boolean> = prefs.timerFadeAudio.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _updateState = MutableStateFlow(UpdateUiState())
    val updateState: StateFlow<UpdateUiState> = _updateState

    fun setTheme(v: String) = viewModelScope.launch { prefs.setTheme(v) }
    fun setTriggerKey(v: Int) = viewModelScope.launch { prefs.setTimerTriggerKey(v) }
    fun setCorner(v: Int) = viewModelScope.launch { prefs.setTimerCorner(v) }
    fun setFade(v: Boolean) = viewModelScope.launch { prefs.setTimerFadeAudio(v) }

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
