package com.zeddihub.tv.alerts

import retrofit2.http.GET
import retrofit2.http.Query

interface AlertsApi {
    @GET("alerts.php")
    suspend fun list(@Query("kind") kind: String = "tv"): AlertsResp
}
