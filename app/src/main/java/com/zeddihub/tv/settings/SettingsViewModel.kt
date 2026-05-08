package com.zeddihub.tv.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeddihub.tv.data.prefs.AppPrefs
import com.zeddihub.tv.data.update.UpdateChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UpdateUiState(val checking: Boolean = false, val message: String? = null)

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

    fun checkUpdates(ctx: Context) = viewModelScope.launch {
        _updateState.value = UpdateUiState(checking = true)
        val result = updateChecker.checkNow()
        _updateState.value = when {
            result == null -> UpdateUiState(checking = false, message = "Server nedostupný.")
            result.isUpdateAvailable -> {
                updateChecker.startInstall(ctx, result)
                UpdateUiState(checking = false,
                    message = "Nová verze ${result.versionName} — stahuji…")
            }
            else -> UpdateUiState(checking = false, message = "Máte nejnovější verzi.")
        }
    }
}
