package com.zeddihub.tv.diag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionTestViewModel @Inject constructor(
    private val tester: ConnectionTest,
) : ViewModel() {
    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running.asStateFlow()

    private val _report = MutableStateFlow<ConnectionTestReport?>(null)
    val report: StateFlow<ConnectionTestReport?> = _report.asStateFlow()

    fun run() = viewModelScope.launch {
        _running.value = true
        _report.value = tester.run()
        _running.value = false
    }
}
