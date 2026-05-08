package com.zeddihub.tv.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    Dashboard("dashboard", com.zeddihub.tv.R.string.nav_dashboard, Icons.Outlined.Dashboard),
    Timer("timer", com.zeddihub.tv.R.string.nav_timer, Icons.Outlined.Bedtime),
    Schedule("schedule", com.zeddihub.tv.R.string.nav_schedule, Icons.Outlined.CalendarMonth),
    Routine("routine", com.zeddihub.tv.R.string.nav_routine, Icons.Outlined.NightsStay),
    Health("health", com.zeddihub.tv.R.string.nav_health, Icons.Outlined.MonitorHeart),
    Network("network", com.zeddihub.tv.R.string.nav_network, Icons.Outlined.NetworkCheck),
    Media("media", com.zeddihub.tv.R.string.nav_media, Icons.Outlined.PlayCircle),
    Servers("servers", com.zeddihub.tv.R.string.nav_servers, Icons.Outlined.Dns),
    Settings("settings", com.zeddihub.tv.R.string.nav_settings, Icons.Outlined.Settings),
}
