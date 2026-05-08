package com.zeddihub.tv.timer.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SchedulesViewModel @Inject constructor(
    @ApplicationContext private val appCtx: Context,
    private val store: ScheduleStore,
    private val scheduler: ScheduleScheduler,
) : ViewModel() {

    val schedules: StateFlow<List<Schedule>> =
        store.schedules.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun upsert(s: Schedule) = viewModelScope.launch {
        store.upsert(s)
        scheduler.rearm(store.list())
    }

    fun delete(id: String) = viewModelScope.launch {
        scheduler.cancel(id)
        store.delete(id)
    }

    fun toggle(id: String) = viewModelScope.launch {
        store.toggleEnabled(id)
        scheduler.rearm(store.list())
    }
}
