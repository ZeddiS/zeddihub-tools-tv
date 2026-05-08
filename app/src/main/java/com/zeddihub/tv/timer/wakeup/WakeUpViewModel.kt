package com.zeddihub.tv.timer.wakeup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WakeUpViewModel @Inject constructor(
    private val store: WakeUpStore,
    private val scheduler: WakeUpScheduler,
) : ViewModel() {
    val wakeups: StateFlow<List<WakeUp>> =
        store.wakeups.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun upsert(w: WakeUp) = viewModelScope.launch { store.upsert(w); scheduler.rearm(store.list()) }
    fun delete(id: String) = viewModelScope.launch { scheduler.cancel(id); store.delete(id) }
    fun toggle(id: String) = viewModelScope.launch { store.toggleEnabled(id); scheduler.rearm(store.list()) }
}
