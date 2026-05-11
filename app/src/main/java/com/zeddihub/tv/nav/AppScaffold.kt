package com.zeddihub.tv.nav

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.focusGroup
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ClickableSurfaceDefaults
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

// Android TVs commonly overscan ~5%; keep content inside a safe inset so
// nothing important is cropped on older panels without overscan-correction.
private val OVERSCAN_HORIZONTAL = 36.dp
private val OVERSCAN_VERTICAL = 20.dp

/**
 * v0.1.11 — Google TV / PlayStation 5 native layout.
 *
 * **No side rail.** The Dashboard IS the home screen — full-width 5-row
 * horizontal-tile launcher (Quick actions / Streaming / Tools / Home /
 * System). Each tile navigates to its dedicated sub-screen. From any
 * sub-screen the user presses Back on the TV remote (hardware back key
 * on every Android TV remote) to return to Dashboard.
 *
 * Why no rail: 22 alphabetical destinations in a vertical list made the
 * app feel like a settings menu — exactly what the user said they didn't
 * want. PS5 / GTV / Apple TV all use launcher-style homes (no persistent
 * nav chrome) because TVs are leaned-back, not leaned-in like phones.
 *
 * The floating "← Domů" pill in the top-left of every sub-screen is a
 * backup affordance for remotes where Back is hard to find (Xiaomi
 * universal remote sometimes binds Back to a side button users miss).
 * It's a fallback — the primary way home is hardware Back.
 *
 * Navigation back stack:
 *   Dashboard → TimerScreen   (press Back → Dashboard)
 *   Dashboard → SettingsScreen → (deep page) (press Back × 2)
 *
 * `popUpTo(startDestinationId)` with `saveState = true` keeps each
 * sub-screen's scroll position when the user returns there from
 * Dashboard a second time.
 */
@Composable
fun AppScaffold() {
    val navController = rememberNavController()

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
        // Subtle page transitions — fade + a small horizontal slide so
        // sub-screens "appear from the right" instead of hard-cutting.
        val enter = fadeIn(tween(220)) +
            slideInHorizontally(tween(260)) { it / 14 }
        val exit  = fadeOut(tween(180)) +
            slideOutHorizontally(tween(220)) { -it / 18 }

        NavHost(
            navController = navController,
            startDestination = TopDestination.Dashboard.route,
            enterTransition    = { enter },
            exitTransition     = { exit },
            popEnterTransition = { enter },
            popExitTransition  = { exit },
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start  = OVERSCAN_HORIZONTAL,
                    end    = OVERSCAN_HORIZONTAL,
                    top    = OVERSCAN_VERTICAL,
                    bottom = OVERSCAN_VERTICAL,
                ),
        ) {
            // Dashboard = home. Receives onNavigate callback that
            // translates tile clicks into NavController.navigate().
            composable(TopDestination.Dashboard.route) {
                DashboardScreen(onNavigate = { route ->
                    if (navController.currentDestination?.route != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                })
            }
            subScreen(navController, TopDestination.Timer)         { TimerScreen() }
            subScreen(navController, TopDestination.Schedule)      { SchedulesScreen() }
            subScreen(navController, TopDestination.Routine)       { RoutineScreen() }
            subScreen(navController, TopDestination.WakeUp)        { WakeUpScreen() }
            subScreen(navController, TopDestination.SmartHome)     { SmartHomeScreen() }
            subScreen(navController, TopDestination.HomeAssistant) { HomeAssistantScreen() }
            subScreen(navController, TopDestination.WatchLater)    { WatchLaterScreen() }
            subScreen(navController, TopDestination.LocalSend)     { LocalSendScreen() }
            subScreen(navController, TopDestination.Files)         { FilesScreen() }
            subScreen(navController, TopDestination.Browser)       { BrowserScreen() }
            subScreen(navController, TopDestination.Alerts)        { AlertsScreen() }
            subScreen(navController, TopDestination.Audio)         { AudioScreen() }
            subScreen(navController, TopDestination.ConnectionTest){ ConnectionTestScreen() }
            subScreen(navController, TopDestination.Health)        { HealthScreen() }
            subScreen(navController, TopDestination.Network)       { NetworkScreen() }
            subScreen(navController, TopDestination.Media)         { MediaScreen() }
            subScreen(navController, TopDestination.Servers)       { ServersScreen() }
            subScreen(navController, TopDestination.Accessibility) { AccessibilityScreen() }
            subScreen(navController, TopDestination.Parental)      { ParentalScreen() }
            subScreen(navController, TopDestination.Settings)      { SettingsScreen() }
        }
    }
}

/**
 * v0.1.12 — Wraps each sub-screen with:
 *   • "← Domů" floating pill (back nav fallback)
 *   • Auto-focus on the content area when screen opens, so D-pad lands
 *     directly on the first focusable child of the screen (not on the
 *     home pill — that's a fallback, not the primary action)
 *   • Subtle fade-in + slide-up content animation (220ms) for a more
 *     "alive" feel when navigating between screens
 *
 * The content area is wrapped in `focusGroup()` + `focusRequester` so
 * the screen content acts as a single focus target that delegates to
 * its first focusable child. `LaunchedEffect` requests focus once when
 * the screen lands.
 */
private fun androidx.navigation.NavGraphBuilder.subScreen(
    navController: NavHostController,
    dest: TopDestination,
    content: @Composable () -> Unit,
) {
    composable(dest.route) {
        val focusRequester = remember { FocusRequester() }
        var contentVisible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            // Small delay so the slide-in transition completes before
            // we try to grab focus. Without this the focus request can
            // race the NavHost animation and silently no-op.
            delay(140)
            runCatching { focusRequester.requestFocus() }
            contentVisible = true
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(280)) +
                        slideInVertically(tween(320)) { it / 24 },
                exit = fadeOut(tween(140)),
            ) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp)
                    .focusGroup()
                    .focusRequester(focusRequester)
                ) {
                    content()
                }
            }
            HomePill(
                onClick = {
                    if (!navController.popBackStack()) {
                        navController.navigate(TopDestination.Dashboard.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 4.dp),
            )
        }
    }
}

/**
 * v0.1.14 — Back pill, redesigned. Previous version had a tiny circle-icon
 * + Home label that looked like a small chip lost in the corner. New
 * version is a wider full-height pill with arrow + "Zpět" label, no
 * inner icon-circle. Scale-on-focus 1.08 + primary fill + no focus
 * rectangle (NoFocusBorder).
 */
@Composable
private fun HomePill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val pillScale by animateFloatAsState(
        targetValue = if (focused) 1.08f else 1f,
        animationSpec = tween(durationMillis = 160),
        label = "back-pill-scale",
    )
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(24.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onBackground,
            focusedContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        border = com.zeddihub.tv.ui.components.NoFocusBorder,
        modifier = modifier
            .height(44.dp)
            .scale(pillScale)
            .shadow(
                elevation = if (focused) 12.dp else 2.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = MaterialTheme.colorScheme.primary,
            )
            .onFocusChanged { focused = it.isFocused },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 18.dp),
        ) {
            Text(
                "←",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Zpět",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
    }
}
