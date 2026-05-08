package com.zeddihub.tv.alerts

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AlertJson(
    val id: String,
    val severity: String = "info",   // "info" | "warn" | "error"
    val title: String,
    val message: String = "",
    val ts: Long = 0L,                // server-issued epoch seconds
    val ttl: Long = 600L,             // how long to keep showing (seconds)
    val source: String = "",          // "server-down" | "push" | "admin"
)

@JsonClass(generateAdapter = true)
data class AlertsResp(
    val alerts: List<AlertJson> = emptyList(),
    val server_time: Long = 0L,
)
