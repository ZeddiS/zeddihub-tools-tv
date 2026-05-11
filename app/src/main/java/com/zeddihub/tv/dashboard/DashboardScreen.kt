package com.zeddihub.tv.dashboard

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.NetworkPing
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.SendToMobile
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SpeakerGroup
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.zeddihub.tv.nav.TopDestination
import com.zeddihub.tv.ui.components.NoFocusBorder

/**
 * Google-TV / PlayStation-style home screen.
 *
 * Why this layout: a side rail of 22 alphabetical destinations made the
 * app feel like a settings menu. TVs are consumed leaning back; the eye
 * scans LEFT-to-RIGHT, not top-to-bottom, and tile-based home screens
 * (Google TV, PS5, Apple TV) all use horizontal rows for that reason.
 *
 * Structure (top-to-bottom, all rows are LazyRow horizontally scrolled):
 *   1. HERO clock card with breathing gradient + weather chip
 *   2. SYSTEM strip (RAM / storage / Wi-Fi)
 *   3. ⚡ Rychlé akce — sleep timer, schedule, bedtime, health (4)
 *   4. 🎬 Streamování — Netflix / YouTube / Disney+ / Plex / … (8 admin-curated)
 *   5. 🛠 Nástroje — network / media / browser / files / audio / wol (6)
 *   6. 🏠 Domácnost — smart home / HA / watch later / localsend (4)
 *   7. ⚙️ Systém — alerts / accessibility / parental / settings (4)
 *
 * D-pad: vertical between rows, horizontal within a row. Each row uses a
 * LazyRow so the user can keep going past the visible window. The first
 * focusable target is row 3 (rychlé akce) — hero is decorative.
 *
 * Click → onNavigate(route). The hosting AppScaffold passes a callback
 * that maps to navController.navigate() with state-saving back stack.
 */
@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit = {},
    vm: DashboardViewModel = hiltViewModel(),
) {
    val now by vm.now.collectAsState()
    val sysInfo by vm.sysInfo.collectAsState()
    val weather by vm.weather.collectAsState()
    val favorites by vm.favoriteRoutes.collectAsState()
    val ctx = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    LaunchedEffect(Unit) { vm.refresh(ctx) }

    // Build the master tile catalog so the favorites row can look up
    // tile data (icon, accent, label) from a route string.
    val tileCatalog = remember {
        listOf(
            HomeTile("Sleep Timer", Icons.Outlined.Bedtime,    Color(0xFFF59E0B), TopDestination.Timer.route),
            HomeTile("Plán",        Icons.Outlined.CalendarMonth, Color(0xFFA78BFA), TopDestination.Schedule.route),
            HomeTile("Bedtime",     Icons.Outlined.NightsStay, Color(0xFF22C55E), TopDestination.Routine.route),
            HomeTile("Budík",       Icons.Outlined.Alarm,      Color(0xFF06B6D4), TopDestination.WakeUp.route),
            HomeTile("Stav TV",     Icons.Outlined.MonitorHeart, Color(0xFFEF4444), TopDestination.Health.route),
            HomeTile("Síť",         Icons.Outlined.NetworkCheck, Color(0xFF06B6D4), TopDestination.Network.route),
            HomeTile("Diagnostika", Icons.Outlined.NetworkPing,  Color(0xFFA78BFA), TopDestination.ConnectionTest.route),
            HomeTile("Prohlížeč",   Icons.Outlined.Language,     Color(0xFF22C55E), TopDestination.Browser.route),
            HomeTile("Soubory",     Icons.Outlined.Folder,       Color(0xFFF59E0B), TopDestination.Files.route),
            HomeTile("Audio",       Icons.Outlined.SpeakerGroup, Color(0xFFEC4899), TopDestination.Audio.route),
            HomeTile("Smart Home",      Icons.Outlined.Lightbulb,    Color(0xFFF59E0B), TopDestination.SmartHome.route),
            HomeTile("Home Assistant",  Icons.Outlined.Home,         Color(0xFF06B6D4), TopDestination.HomeAssistant.route),
            HomeTile("Watch later",     Icons.Outlined.Bookmark,     Color(0xFFA78BFA), TopDestination.WatchLater.route),
            HomeTile("LocalSend",       Icons.Outlined.SendToMobile, Color(0xFF22C55E), TopDestination.LocalSend.route),
            HomeTile("Servery",         Icons.Outlined.Dns,          Color(0xFFEF4444), TopDestination.Servers.route),
            HomeTile("Upozornění",     Icons.Outlined.Campaign,       Color(0xFFEF4444), TopDestination.Alerts.route),
            HomeTile("Přístupnost",    Icons.Outlined.Accessibility,  Color(0xFFA78BFA), TopDestination.Accessibility.route),
            HomeTile("Rodičovská",     Icons.Outlined.FamilyRestroom, Color(0xFFEC4899), TopDestination.Parental.route),
            HomeTile("Nastavení",      Icons.Outlined.Settings,       Color(0xFFFF8A1A), TopDestination.Settings.route),
        )
    }

    val onToggleFavorite: (String) -> Unit = { route -> scope.launch { vm.toggleFavorite(route) } }

    // v0.1.12 — Staggered entrance for Dashboard rows. Each row appears
    // ~80ms after the previous, creating a "cascade in" effect that
    // makes the home screen feel alive when first opened. The visible
    // counter increments via LaunchedEffect and is consumed by each
    // StaggeredRow wrapper as a delay multiplier.
    var rowsVisible by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        // Hero + sysinfo show immediately (decorative)
        rowsVisible = 2
        // Then 4 content rows cascade in (streaming row removed in v0.1.14
        // per user request — the launcher tiles for Netflix / YT / etc.
        // duplicated functionality the user gets from their TV's native
        // launcher, and the apps weren't usable on devices that didn't
        // have them installed anyway).
        repeat(4) {
            delay(90)
            rowsVisible++
        }
    }

    // Vertical scroll for the entire home — overflow goes off-screen
    // bottom on smaller TVs / 720p panels. The user just D-pad-downs to
    // scroll the next row into view.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(end = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Stagger(visible = rowsVisible >= 1) { HeroClockCard(now, weather) }
        Stagger(visible = rowsVisible >= 2) { SysInfoStrip(sysInfo) }

        // ⭐ Oblíbené — visible only when user has marked at least one tile.
        // Always renders FIRST so it's the focus target after the hero,
        // making the user's curated picks the easiest things to reach.
        if (favorites.isNotEmpty()) {
            Stagger(visible = rowsVisible >= 2) { HomeRow(
                title = "⭐ Oblíbené",
                tiles = favorites.mapNotNull { route -> tileCatalog.firstOrNull { it.route == route } },
                favorites = favorites,
                onClick = { tile -> onNavigate(tile.route) },
                onToggleFavorite = onToggleFavorite,
            ) }
        }

        Stagger(visible = rowsVisible >= 3) { HomeRow(
            title = "⚡ Rychlé akce",
            tiles = listOf(
                HomeTile("Sleep Timer", Icons.Outlined.Bedtime,    Color(0xFFF59E0B), TopDestination.Timer.route),
                HomeTile("Plán",        Icons.Outlined.CalendarMonth, Color(0xFFA78BFA), TopDestination.Schedule.route),
                HomeTile("Bedtime",     Icons.Outlined.NightsStay, Color(0xFF22C55E), TopDestination.Routine.route),
                HomeTile("Budík",       Icons.Outlined.Alarm,      Color(0xFF06B6D4), TopDestination.WakeUp.route),
                HomeTile("Stav TV",     Icons.Outlined.MonitorHeart, Color(0xFFEF4444), TopDestination.Health.route),
            ),
            favorites = favorites,
            onClick = { tile -> onNavigate(tile.route) },
            onToggleFavorite = onToggleFavorite,
        ) }

        Stagger(visible = rowsVisible >= 4) { HomeRow(
            title = "🛠 Nástroje",
            tiles = listOf(
                HomeTile("Síť",         Icons.Outlined.NetworkCheck, Color(0xFF06B6D4), TopDestination.Network.route),
                HomeTile("Diagnostika", Icons.Outlined.NetworkPing,  Color(0xFFA78BFA), TopDestination.ConnectionTest.route),
                HomeTile("Prohlížeč",   Icons.Outlined.Language,     Color(0xFF22C55E), TopDestination.Browser.route),
                HomeTile("Soubory",     Icons.Outlined.Folder,       Color(0xFFF59E0B), TopDestination.Files.route),
                HomeTile("Audio",       Icons.Outlined.SpeakerGroup, Color(0xFFEC4899), TopDestination.Audio.route),
            ),
            favorites = favorites,
            onClick = { tile -> onNavigate(tile.route) },
            onToggleFavorite = onToggleFavorite,
        ) }

        Stagger(visible = rowsVisible >= 5) { HomeRow(
            title = "🏠 Domácnost & sdílení",
            tiles = listOf(
                HomeTile("Smart Home",      Icons.Outlined.Lightbulb,    Color(0xFFF59E0B), TopDestination.SmartHome.route),
                HomeTile("Home Assistant",  Icons.Outlined.Home,         Color(0xFF06B6D4), TopDestination.HomeAssistant.route),
                HomeTile("Watch later",     Icons.Outlined.Bookmark,     Color(0xFFA78BFA), TopDestination.WatchLater.route),
                HomeTile("LocalSend",       Icons.Outlined.SendToMobile, Color(0xFF22C55E), TopDestination.LocalSend.route),
                HomeTile("Servery",         Icons.Outlined.Dns,          Color(0xFFEF4444), TopDestination.Servers.route),
            ),
            favorites = favorites,
            onClick = { tile -> onNavigate(tile.route) },
            onToggleFavorite = onToggleFavorite,
        ) }

        Stagger(visible = rowsVisible >= 6) { HomeRow(
            title = "⚙️ Systém",
            tiles = listOf(
                HomeTile("Upozornění",     Icons.Outlined.Campaign,       Color(0xFFEF4444), TopDestination.Alerts.route),
                HomeTile("Přístupnost",    Icons.Outlined.Accessibility,  Color(0xFFA78BFA), TopDestination.Accessibility.route),
                HomeTile("Rodičovská",     Icons.Outlined.FamilyRestroom, Color(0xFFEC4899), TopDestination.Parental.route),
                HomeTile("Nastavení",      Icons.Outlined.Settings,       Color(0xFFFF8A1A), TopDestination.Settings.route),
            ),
            favorites = favorites,
            onClick = { tile -> onNavigate(tile.route) },
            onToggleFavorite = onToggleFavorite,
        ) }
    }
}

/**
 * Stagger wrapper — fades + slides each row in from the bottom. Used by
 * the Dashboard to cascade rows when the home screen first opens.
 */
@Composable
private fun Stagger(visible: Boolean, content: @Composable () -> Unit) {
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = androidx.compose.animation.fadeIn(tween(300)) +
                androidx.compose.animation.slideInVertically(tween(340)) { it / 6 },
        exit = androidx.compose.animation.fadeOut(tween(120)),
    ) {
        content()
    }
}

// ─── Tile model ──────────────────────────────────────────────────────
private data class HomeTile(
    val title: String,
    val icon: ImageVector,
    val accent: Color,
    val route: String,
)

// ─── Hero clock card ─────────────────────────────────────────────────
@Composable
private fun HeroClockCard(now: NowText, weather: WeatherInfo) {
    val infinite = rememberInfiniteTransition(label = "hero-pulse")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "hero-phase",
    )
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    // Use the brand orange as both gradient stops at varying alphas;
    // tertiary in TV Material3 darkColorScheme defaults to a pinkish
    // shade that fights with the brand orange. Two-tone is more
    // recognisably ZeddiHub.
    val brush = Brush.linearGradient(
        colorStops = arrayOf(
            0f to surface,
            (0.55f + phase * 0.10f) to primary.copy(alpha = 0.28f),
            1f to Color(0xFFFF5722).copy(alpha = 0.22f),
        ),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end   = androidx.compose.ui.geometry.Offset(1500f * (1f - phase * 0.2f), 600f),
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(brush)
            // Make the hero focusable but no-op on click — focus animation
            // lifts the gradient slightly; that gives the screen entrance
            // a sense of life when the user lands on the home.
            .focusable(),
    ) {
        // Logo top-left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp)),
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(com.zeddihub.tv.R.drawable.zh_logo_square),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Weather chip (top-right) inside a frosted card
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Icon(
                Icons.Outlined.WbSunny, null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(28.dp),
            )
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(
                    weather.tempText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    weather.label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Clock + date (bottom-left)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 32.dp, bottom = 28.dp, end = 32.dp),
        ) {
            Text(
                now.timeStr,
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 100.sp,
                letterSpacing = androidx.compose.ui.unit.TextUnit(-2f, androidx.compose.ui.unit.TextUnitType.Sp),
            )
            Text(
                now.dateStr,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

// ─── System info strip (RAM / Storage / Wi-Fi) ───────────────────────
@Composable
private fun SysInfoStrip(sys: SysInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InfoTile(Icons.Outlined.Memory,  "RAM",      sys.ramText,     Modifier.weight(1f))
        InfoTile(Icons.Outlined.Storage, "Úložiště", sys.storageText, Modifier.weight(1f))
        InfoTile(Icons.Outlined.Wifi,    "Síť",      sys.networkText, Modifier.weight(1f))
    }
}

@Composable
private fun InfoTile(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier
        .clip(RoundedCornerShape(14.dp))
        .background(MaterialTheme.colorScheme.surface)
        .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(label,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp))
            }
            Text(value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 6.dp))
        }
    }
}

// ─── Generic horizontal row of tiles ─────────────────────────────────
@Composable
private fun HomeRow(
    title: String,
    tiles: List<HomeTile>,
    favorites: List<String>,
    onClick: (HomeTile) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 4.dp),
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp),
        ) {
            items(tiles) { tile ->
                HomeRowTile(
                    tile = tile,
                    isFavorite = favorites.contains(tile.route),
                    onClick = onClick,
                    onToggleFavorite = { onToggleFavorite(tile.route) },
                )
            }
        }
    }
}

// ─── Tile widgets — large rectangles with focus glow ─────────────────
@Composable
private fun HomeRowTile(
    tile: HomeTile,
    isFavorite: Boolean,
    onClick: (HomeTile) -> Unit,
    onToggleFavorite: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.08f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "tile-scale",
    )
    var menuOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .width(180.dp)
        .height(120.dp)
    ) {
        Surface(
            onClick = { onClick(tile) },
            // v0.1.14 — long-press OK triggers the contextual menu so user
            // can add/remove this tile from oblíbené. onLongClick is part
            // of tv-material3 Surface (also fired by DPAD_CENTER held
            // 500ms by the system). On TV the system handles the timing.
            onLongClick = { menuOpen = true },
            shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(18.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = tile.accent.copy(alpha = 0.32f),
                contentColor = MaterialTheme.colorScheme.onBackground,
                focusedContentColor = MaterialTheme.colorScheme.onBackground,
            ),
            border = NoFocusBorder,
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
                .shadow(
                    elevation = if (focused) 18.dp else 0.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = tile.accent,
                    spotColor = tile.accent,
                )
                .onFocusChanged { focused = it.isFocused },
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tile.accent.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(tile.icon, null, tint = tile.accent, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.weight(1f))
                Text(
                    tile.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        // Favorite star indicator — top-right corner. Visible always when
        // tile is in favorites; subtle gold-tinted; gives the user a visual
        // confirmation of which tiles are in their oblíbené row.
        if (isFavorite) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(24.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color(0xFFFFD54F).copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("⭐", fontSize = 12.sp)
            }
        }
    }

    // Long-press context menu — Přidat do oblíbených / Odebrat
    if (menuOpen) {
        TileContextMenu(
            tile = tile,
            isFavorite = isFavorite,
            onOpen = { menuOpen = false; onClick(tile) },
            onToggleFavorite = { menuOpen = false; onToggleFavorite() },
            onDismiss = { menuOpen = false },
        )
    }
}

/**
 * Bottom-sheet-style menu shown when user long-presses a tile.
 * Action: Otevřít · Přidat/Odebrat z oblíbených · Zrušit.
 */
@Composable
private fun TileContextMenu(
    tile: HomeTile,
    isFavorite: Boolean,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true, dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            colors = androidx.tv.material3.SurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier.width(420.dp),
        ) {
            Column(modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tile.accent.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(tile.icon, null, tint = tile.accent, modifier = Modifier.size(28.dp))
                    }
                    Text(tile.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 18.dp))
                }
                com.zeddihub.tv.ui.components.PsBigChoice(
                    title = "Otevřít",
                    description = "Naviguje na ${tile.title.lowercase()}",
                    icon = tile.icon,
                    selected = false,
                    onClick = onOpen,
                )
                com.zeddihub.tv.ui.components.PsBigChoice(
                    title = if (isFavorite) "Odebrat z oblíbených" else "Přidat do oblíbených",
                    description = if (isFavorite)
                        "Zmizí z řady Oblíbené nahoře."
                    else
                        "Zobrazí se v řadě Oblíbené nahoře pro rychlý přístup.",
                    icon = androidx.compose.material.icons.Icons.Outlined.Bookmark,
                    selected = isFavorite,
                    onClick = onToggleFavorite,
                )
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End) {
                    com.zeddihub.tv.ui.components.PsSecondaryButton(text = "Zrušit", onClick = onDismiss)
                }
            }
        }
    }
}

