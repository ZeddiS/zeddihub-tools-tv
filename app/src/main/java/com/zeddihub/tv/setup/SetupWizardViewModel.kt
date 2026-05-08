package com.zeddihub.tv.setup

import android.view.KeyEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeddihub.tv.data.prefs.AppPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupWizardViewModel @Inject constructor(
    private val prefs: AppPrefs,
) : ViewModel() {

    data class WizardState(
        val language: String = "auto",
        val theme: String = "dark",
        val triggerKey: Int = KeyEvent.KEYCODE_DPAD_CENTER,
        val corner: Int = 1,
        val weatherLat: Double = 50.08,
        val weatherLon: Double = 14.43,
        val weatherLabel: String = "Praha",
        val hassUrl: String = "",
        val hassToken: String = "",
    )

    private val _state = MutableStateFlow(WizardState())
    val state: StateFlow<WizardState> = _state.asStateFlow()

    init {
        // Pre-fill the wizard from existing prefs so the user sees their
        // current values (or defaults). Lets them edit incrementally
        // instead of starting from blank — important if they re-enter
        // the wizard via a future "Reset onboarding" toggle in Settings.
        viewModelScope.launch {
            _state.value = WizardState(
                language = prefs.language.first(),
                theme = prefs.theme.first(),
                triggerKey = prefs.timerTriggerKey.first(),
                corner = prefs.timerCorner.first(),
                weatherLat = prefs.weatherLat.first(),
                weatherLon = prefs.weatherLon.first(),
                weatherLabel = prefs.weatherLabel.first(),
                hassUrl = prefs.hassBaseUrl.first(),
                hassToken = prefs.hassToken.first(),
            )
        }
    }

    fun setLanguage(v: String) { _state.value = _state.value.copy(language = v) }
    fun setTheme(v: String) { _state.value = _state.value.copy(theme = v) }
    fun setTriggerKey(v: Int) { _state.value = _state.value.copy(triggerKey = v) }
    fun setCorner(v: Int) { _state.value = _state.value.copy(corner = v) }
    fun setWeather(label: String, lat: Double, lon: Double) {
        _state.value = _state.value.copy(weatherLabel = label, weatherLat = lat, weatherLon = lon)
    }
    fun setHass(url: String, token: String) {
        _state.value = _state.value.copy(hassUrl = url, hassToken = token)
    }

    /** Persist all collected answers + flip the wizardCompleted flag. */
    fun persist() {
        val s = _state.value
        viewModelScope.launch {
            prefs.setLanguage(s.language)
            prefs.setTheme(s.theme)
            prefs.setTimerTriggerKey(s.triggerKey)
            prefs.setTimerCorner(s.corner)
            prefs.setWeather(s.weatherLat, s.weatherLon, s.weatherLabel)
            prefs.setHass(s.hassUrl, s.hassToken)
            prefs.setWizardCompleted(true)
        }
    }
}
