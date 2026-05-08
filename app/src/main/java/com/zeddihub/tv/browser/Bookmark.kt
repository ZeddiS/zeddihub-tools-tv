package com.zeddihub.tv.browser

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Bookmark(
    val title: String,
    val url: String,
)

object DefaultBookmarks {
    /** Pre-shipped bookmarks the user can edit/remove. Picked to be useful
     *  on a TV (mostly news + ZeddiHub family + a search engine). */
    val all: List<Bookmark> = listOf(
        Bookmark("ZeddiHub", "https://zeddihub.eu"),
        Bookmark("ZeddiHub Tools", "https://zeddihub.eu/tools/"),
        Bookmark("DuckDuckGo", "https://duckduckgo.com"),
        Bookmark("YouTube", "https://www.youtube.com/tv"),
        Bookmark("Wikipedia", "https://cs.wikipedia.org"),
        Bookmark("Counter-Strike", "https://www.counter-strike.net"),
    )
}
