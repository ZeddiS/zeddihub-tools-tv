package com.zeddihub.tv.accessibility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeddihub.tv.data.prefs.AppPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccessibilityViewModel @Inject constructor(
    private val prefs: AppPrefs,
) : ViewModel() {

    val ccUniversal: StateFlow<Boolean> = prefs.ccUniversalEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val dyslexiaFont: StateFlow<Boolean> = prefs.dyslexiaFontEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun toggleCc() = viewModelScope.launch {
        prefs.setCcUniversalEnabled(!prefs.ccUniversalEnabled.first())
    }

    fun toggleFont() = viewModelScope.launch {
        prefs.setDyslexiaFontEnabled(!prefs.dyslexiaFontEnabled.first())
    }
}
