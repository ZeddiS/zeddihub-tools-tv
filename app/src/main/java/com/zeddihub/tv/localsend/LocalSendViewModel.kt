package com.zeddihub.tv.localsend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LocalSendViewModel @Inject constructor(
    private val server: LocalSendServer,
) : ViewModel() {

    val running: StateFlow<Boolean> = server.running
    val received: StateFlow<List<LocalSendServer.ReceivedFile>> = server.received
    val mdnsRegistered: StateFlow<Boolean> = server.mdnsRegistered

    private val _address = MutableStateFlow<String?>(null)
    val address: StateFlow<String?> = _address.asStateFlow()

    fun start() = viewModelScope.launch {
        withContext(Dispatchers.IO) { server.start() }
        refreshAddress()
    }

    fun stop() = viewModelScope.launch {
        withContext(Dispatchers.IO) { server.stop() }
    }

    fun refreshAddress() = viewModelScope.launch(Dispatchers.IO) {
        _address.value = server.listenAddress()
    }
}
