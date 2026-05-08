package com.zeddihub.tv.watchlater

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class WatchLaterItem(
    val id: String,
    val url: String,
    val title: String,
    val source: String? = null,   // "youtube", "plex", "netflix", "twitch", or null = unknown
    val added_at: Long = 0,       // epoch millis
    val watched: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class WatchLaterResp(val items: List<WatchLaterItem> = emptyList())

@JsonClass(generateAdapter = true)
data class SimpleAck(val ok: Boolean = true)

interface WatchLaterApi {
    @GET("watchlater.php")
    suspend fun list(): WatchLaterResp

    @POST("watchlater.php?action=mark_watched")
    suspend fun markWatched(@Query("id") id: String): SimpleAck
}
