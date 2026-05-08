package com.zeddihub.tv.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.NetworkPing
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.SendToMobile
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SpeakerGroup
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
    WakeUp("wakeup", com.zeddihub.tv.R.string.nav_wakeup, Icons.Outlined.Alarm),
    SmartHome("smarthome", com.zeddihub.tv.R.string.nav_smarthome, Icons.Outlined.Lightbulb),
    HomeAssistant("hass", com.zeddihub.tv.R.string.nav_hass, Icons.Outlined.Home),
    WatchLater("watchlater", com.zeddihub.tv.R.string.nav_watchlater, Icons.Outlined.Bookmark),
    LocalSend("localsend", com.zeddihub.tv.R.string.nav_localsend, Icons.Outlined.SendToMobile),
    Files("files", com.zeddihub.tv.R.string.nav_files, Icons.Outlined.Folder),
    Browser("browser", com.zeddihub.tv.R.string.nav_browser, Icons.Outlined.Language),
    Alerts("alerts", com.zeddihub.tv.R.string.nav_alerts, Icons.Outlined.Campaign),
    Audio("audio", com.zeddihub.tv.R.string.nav_audio, Icons.Outlined.SpeakerGroup),
    ConnectionTest("conn_test", com.zeddihub.tv.R.string.nav_conn_test, Icons.Outlined.NetworkPing),
    Health("health", com.zeddihub.tv.R.string.nav_health, Icons.Outlined.MonitorHeart),
    Network("network", com.zeddihub.tv.R.string.nav_network, Icons.Outlined.NetworkCheck),
    Media("media", com.zeddihub.tv.R.string.nav_media, Icons.Outlined.PlayCircle),
    Servers("servers", com.zeddihub.tv.R.string.nav_servers, Icons.Outlined.Dns),
    Accessibility("accessibility", com.zeddihub.tv.R.string.nav_accessibility, Icons.Outlined.Accessibility),
    Parental("parental", com.zeddihub.tv.R.string.nav_parental, Icons.Outlined.FamilyRestroom),
    Settings("settings", com.zeddihub.tv.R.string.nav_settings, Icons.Outlined.Settings),
}
