package com.zeddihub.tv.media

import androidx.lifecycle.ViewModel
import com.zeddihub.tv.data.config.TvConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    config: TvConfigRepository,
) : ViewModel() {
    /** Visible, ordered streaming apps from /api/tv-config.php (or hardcoded
     *  fallback when network is unreachable). */
    val apps: StateFlow<List<LaunchableApp>> = config.streamingApps
}
