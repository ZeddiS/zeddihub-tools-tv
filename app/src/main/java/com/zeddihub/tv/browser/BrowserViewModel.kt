package com.zeddihub.tv.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.zeddihub.tv.data.config.TvConfigRepository
import com.zeddihub.tv.data.prefs.AppPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val prefs: AppPrefs,
    moshi: Moshi,
    config: TvConfigRepository,
) : ViewModel() {

    private val adapter = moshi.adapter<List<Bookmark>>(
        Types.newParameterizedType(List::class.java, Bookmark::class.java)
    )

    /**
     * Shown bookmarks = admin-curated remote defaults from /api/tv-config.php,
     * MERGED with the user's locally-added bookmarks. Locally-added entries
     * with a URL that already exists in the remote list are de-duplicated.
     * Falls back to DefaultBookmarks.all if remote and local are both empty.
     */
    val bookmarks: StateFlow<List<Bookmark>> = combine(
        prefs.browserBookmarksJson,
        config.bookmarks,
    ) { json, remote ->
        val saved = runCatching { adapter.fromJson(json) }.getOrNull().orEmpty()
        val merged = (remote + saved).distinctBy { it.url }
        merged.ifEmpty { DefaultBookmarks.all }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DefaultBookmarks.all)

    fun addBookmark(b: Bookmark) = viewModelScope.launch {
        val cur = runCatching { adapter.fromJson(prefs.browserBookmarksJson.first()) }
            .getOrNull().orEmpty()
        val next = (cur + b).distinctBy { it.url }
        prefs.setBrowserBookmarksJson(adapter.toJson(next))
    }

    fun removeBookmark(url: String) = viewModelScope.launch {
        val cur = runCatching { adapter.fromJson(prefs.browserBookmarksJson.first()) }
            .getOrNull().orEmpty()
        prefs.setBrowserBookmarksJson(adapter.toJson(cur.filter { it.url != url }))
    }
}
