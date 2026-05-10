package com.zeddihub.tv.nav

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.zeddihub.tv.accessibility.AccessibilityScreen
import com.zeddihub.tv.alerts.AlertsScreen
import com.zeddihub.tv.audio.AudioScreen
import com.zeddihub.tv.browser.BrowserScreen
import com.zeddihub.tv.dashboard.DashboardScreen
import com.zeddihub.tv.diag.ConnectionTestScreen
import com.zeddihub.tv.files.FilesScreen
import com.zeddihub.tv.health.HealthScreen
import com.zeddihub.tv.localsend.LocalSendScreen
import com.zeddihub.tv.media.MediaScreen
import com.zeddihub.tv.network.NetworkScreen
import com.zeddihub.tv.parental.ParentalScreen
import com.zeddihub.tv.routine.RoutineScreen
import com.zeddihub.tv.servers.ServersScreen
import com.zeddihub.tv.settings.SettingsScreen
import com.zeddihub.tv.smarthome.SmartHomeScreen
import com.zeddihub.tv.smarthome.hass.HomeAssistantScreen
import com.zeddihub.tv.timer.TimerScreen
import com.zeddihub.tv.timer.schedule.SchedulesScreen
import com.zeddihub.tv.timer.wakeup.WakeUpScreen
import com.zeddihub.tv.watchlater.WatchLaterScreen

private val RAIL_COLLAPSED_WIDTH = 88.dp
private val RAIL_EXPANDED_WIDTH = 280.dp
// Android TVs commonly overscan ~5%; keep content inside a safe inset.
private val OVERSCAN_HORIZONTAL = 32.dp
private val OVERSCAN_VERTICAL = 16.dp

/**
 * Top-level scaffold. Side rail + content area with overscan-safe insets.
 *
 * Why a custom collapsing rail instead of `androidx.tv.material3.NavigationDrawer`:
 * the official drawer doesn't support sectioned/grouped content with headers
 * and forces a fixed item style; we need 7 group headers and ~20 items,
 * all D-pad navigable and scrollable. This implementation:
 *   - Animates between 88dp (icons only) and 280dp (icons + labels +
 *     group headers) when any rail item gains focus.
 *   - Uses LazyColumn so D-pad up/down auto-scrolls when the focused
 *     item is below the visible window.
 *   - Restores focus to the previously-selected item after navigation
 *     (because Compose Navigation re-creates the rail on each route change).
 */
@Composable
fun AppScaffold() {
    val navController = rememberNavController()
    var railFocused by remember { mutableStateOf(false) }
    val railWidth by animateDpAsState(
        targetValue = if (railFocused) RAIL_EXPANDED_WIDTH else RAIL_COLLAPSED_WIDTH,
        label = "rail-width",
    )

    Row(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
        SideRail(
            navController = navController,
            railWidth = railWidth,
            expanded = railFocused,
            onFocusChange = { railFocused = it },
        )
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = OVERSCAN_HORIZONTAL,
                top = OVERSCAN_VERTICAL,
                bottom = OVERSCAN_VERTICAL,
            )
        ) {
            // Subtle page transitions — fade + horizontal slide so screens
            // glide in from the rail side and the user perceives a sense of
            // momentum, not a hard cut. 250 ms is on the brink of "feels
            // instant" while still being readable. We avoid translation on
            // the sub-content (just the page wrapper) to keep focus stable.
            val enter = fadeIn(tween(250)) +
                slideInHorizontally(tween(280)) { it / 12 }
            val exit  = fadeOut(tween(180)) +
                slideOutHorizontally(tween(220)) { -it / 18 }

            NavHost(
                navController = navController,
                startDestination = TopDestination.Dashboard.route,
                enterTransition = { enter },
                exitTransition  = { exit },
                popEnterTransition = { enter },
                popExitTransition  = { exit },
            ) {
                composable(TopDestination.Dashboard.route) { DashboardScreen() }
                composable(TopDestination.Timer.route) { TimerScreen() }
                composable(TopDestination.Schedule.route) { SchedulesScreen() }
                composable(TopDestination.Routine.route) { RoutineScreen() }
                composable(TopDestination.WakeUp.route) { WakeUpScreen() }
                composable(TopDestination.SmartHome.route) { SmartHomeScreen() }
                composable(TopDestination.HomeAssistant.route) { HomeAssistantScreen() }
                composable(TopDestination.WatchLater.route) { WatchLaterScreen() }
                composable(TopDestination.LocalSend.route) { LocalSendScreen() }
                composable(TopDestination.Files.route) { FilesScreen() }
                composable(TopDestination.Browser.route) { BrowserScreen() }
                composable(TopDestination.Alerts.route) { AlertsScreen() }
                composable(TopDestination.Audio.route) { AudioScreen() }
                composable(TopDestination.ConnectionTest.route) { ConnectionTestScreen() }
                composable(TopDestination.Health.route) { HealthScreen() }
                composable(TopDestination.Network.route) { NetworkScreen() }
                composable(TopDestination.Media.route) { MediaScreen() }
                composable(TopDestination.Servers.route) { ServersScreen() }
                composable(TopDestination.Accessibility.route) { AccessibilityScreen() }
                composable(TopDestination.Parental.route) { ParentalScreen() }
                composable(TopDestination.Settings.route) { SettingsScreen() }
            }
        }
    }
}

@Composable
private fun SideRail(
    navController: NavHostController,
    railWidth: androidx.compose.ui.unit.Dp,
    expanded: Boolean,
    onFocusChange: (Boolean) -> Unit,
) {
    val backEntry by navController.currentBackStackEntryAsState()
    val current = backEntry?.destination?.route
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .width(railWidth)
            .background(Color(0xFF0A0A14))
            .onFocusChanged { onFocusChange(it.hasFocus) }
            .padding(top = OVERSCAN_VERTICAL + 16.dp, bottom = OVERSCAN_VERTICAL),
    ) {
        Header(expanded)

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            NavLayout.groups.forEachIndexed { groupIdx, group ->
                if (groupIdx > 0) {
                    item("sep-$groupIdx") { Spacer(Modifier.height(8.dp)) }
                }
                if (expanded) {
                    item("hdr-$groupIdx") {
                        Text(
                            stringResource(group.titleRes),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                        )
                    }
                }
                items(group.items.size) { idx ->
                    val dest = group.items[idx]
                    RailItem(
                        dest = dest,
                        selected = current == dest.route,
                        expanded = expanded,
                        onClick = {
                            if (current != dest.route) {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(expanded: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 18.dp, end = 12.dp),
    ) {
        // Square ZeddiHub logo as the rail header — replaces the previous
        // generic primary-colored circle, ties the chrome to the brand.
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp)),
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(com.zeddihub.tv.R.drawable.zh_logo_square),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (expanded) {
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text("ZeddiHub", color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("TV", color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.items(count: Int, content: @Composable (Int) -> Unit) {
    items(count = count, key = null, contentType = { 0 }, itemContent = { content(it) })
}

@Composable
private fun RailItem(
    dest: TopDestination,
    selected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val baseContainer = if (selected)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    else
        Color.Transparent
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 1.dp)
    ) {
        // Active-item left-edge bar — 3dp orange. Shows where you are at
        // a glance even when the rail is collapsed and labels are hidden.
        if (selected) {
            Box(modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 2.dp)
                .height(28.dp)
                .width(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary))
        }
        Surface(
            onClick = onClick,
            shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(10.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = baseContainer,
                focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                contentColor = MaterialTheme.colorScheme.onBackground,
                focusedContentColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 14.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            ) {
                Icon(dest.icon, contentDescription = null, modifier = Modifier.size(22.dp))
                if (expanded) {
                    Text(
                        stringResource(dest.labelRes),
                        modifier = Modifier.padding(start = 14.dp),
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }
    }
}
