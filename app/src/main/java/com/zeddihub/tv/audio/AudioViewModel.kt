package com.zeddihub.tv.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val audio: AudioOutputs,
) : ViewModel() {

    private val _outputs = MutableStateFlow<List<AudioOutput>>(emptyList())
    val outputs: StateFlow<List<AudioOutput>> = _outputs.asStateFlow()

    private val _volume = MutableStateFlow(50)
    val volumePct: StateFlow<Int> = _volume.asStateFlow()

    private val _muted = MutableStateFlow(false)
    val muted: StateFlow<Boolean> = _muted.asStateFlow()

    init {
        // Light polling to react when Bluetooth speaker connects/disconnects
        viewModelScope.launch {
            while (true) {
                refresh()
                delay(2_000)
            }
        }
    }

    fun refresh() {
        _outputs.value = audio.list()
        _volume.value = audio.streamVolumePct()
        _muted.value = audio.isMuted()
    }

    fun setVolume(pct: Int) {
        audio.setStreamVolumePct(pct)
        refresh()
    }

    fun toggleMute() {
        audio.toggleMute()
        refresh()
    }
}
