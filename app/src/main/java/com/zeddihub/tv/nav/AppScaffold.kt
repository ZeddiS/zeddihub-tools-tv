package com.zeddihub.tv.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.zeddihub.tv.dashboard.DashboardScreen
import com.zeddihub.tv.health.HealthScreen
import com.zeddihub.tv.localsend.LocalSendScreen
import com.zeddihub.tv.media.MediaScreen
import com.zeddihub.tv.network.NetworkScreen
import com.zeddihub.tv.routine.RoutineScreen
import com.zeddihub.tv.servers.ServersScreen
import com.zeddihub.tv.settings.SettingsScreen
import com.zeddihub.tv.smarthome.SmartHomeScreen
import com.zeddihub.tv.timer.TimerScreen
import com.zeddihub.tv.timer.schedule.SchedulesScreen
import com.zeddihub.tv.timer.wakeup.WakeUpScreen
import com.zeddihub.tv.watchlater.WatchLaterScreen

@Composable
fun AppScaffold() {
    val navController = rememberNavController()
    Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        SideRail(navController = navController)
        Box(modifier = Modifier.fillMaxSize().padding(start = 24.dp, end = 48.dp, top = 32.dp, bottom = 32.dp)) {
            NavHost(
                navController = navController,
                startDestination = TopDestination.Dashboard.route,
            ) {
                composable(TopDestination.Dashboard.route) { DashboardScreen() }
                composable(TopDestination.Timer.route) { TimerScreen() }
                composable(TopDestination.Schedule.route) { SchedulesScreen() }
                composable(TopDestination.Routine.route) { RoutineScreen() }
                composable(TopDestination.WakeUp.route) { WakeUpScreen() }
                composable(TopDestination.SmartHome.route) { SmartHomeScreen() }
                composable(TopDestination.WatchLater.route) { WatchLaterScreen() }
                composable(TopDestination.LocalSend.route) { LocalSendScreen() }
                composable(TopDestination.Health.route) { HealthScreen() }
                composable(TopDestination.Network.route) { NetworkScreen() }
                composable(TopDestination.Media.route) { MediaScreen() }
                composable(TopDestination.Servers.route) { ServersScreen() }
                composable(TopDestination.Settings.route) { SettingsScreen() }
            }
        }
    }
}

@Composable
private fun SideRail(navController: NavHostController) {
    val backEntry by navController.currentBackStackEntryAsState()
    val current = backEntry?.destination?.route

    Column(
        modifier = Modifier
            .fillMaxSize()
            .width(240.dp)
            .background(Color(0xFF0A0A14))
            .padding(top = 40.dp, bottom = 24.dp, start = 20.dp, end = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
            Text(
                "ZeddiHub TV",
                modifier = Modifier.padding(start = 12.dp),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
            )
        }
        Box(modifier = Modifier.padding(top = 24.dp))

        TopDestination.values().forEach { dest ->
            RailItem(
                dest = dest,
                selected = current == dest.route,
                onClick = {
                    if (current != dest.route) {
                        navController.navigate(dest.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun RailItem(dest: TopDestination, selected: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
            contentColor = MaterialTheme.colorScheme.onBackground,
            focusedContentColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focused = it.isFocused },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Icon(dest.icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Text(
                stringResource(dest.labelRes),
                modifier = Modifier.padding(start = 16.dp),
                fontSize = 16.sp,
            )
        }
    }
}
