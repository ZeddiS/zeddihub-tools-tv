package com.zeddihub.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

/**
 * v0.1.14 — Global no-border for clickable Surfaces. The default TV
 * Material3 Surface draws a 2dp border on focus that the user perceived
 * as „divný obdélník při výběru". Removing the border across all clickable
 * tiles/buttons is safe because focus is signalled by:
 *   • Container colour (focusedContainerColor) brightens
 *   • Scale animation (scale 1.04-1.08)
 *   • Glow shadow (Modifier.shadow with ambientColor = accent)
 * — three signals that read at 8ft viewing distance, no 2dp line needed.
 */
internal val NoFocusBorder
    @Composable
    get() = ClickableSurfaceDefaults.border(
        border = Border.None,
        focusedBorder = Border.None,
        pressedBorder = Border.None,
        disabledBorder = Border.None,
        focusedDisabledBorder = Border.None,
    )

/**
 * PlayStation-inspired primitives. The mainstream `ZhCard` is good for
 * dense content; these are for marquee surfaces — wizard steps, hero
 * cards, big-decision pickers — where the user is supposed to feel the
 * focus and the screen breathes.
 *
 * Design choices:
 *   - Generous padding (32dp+) so content doesn't feel cramped on a TV
 *     viewed from the couch.
 *   - Focus = scale 1.04 + outer glow + brighter container. No tiny
 *     border-only treatment that disappears at 8 ft.
 *   - Strong typography ramp: 32-44sp titles, 14-16sp body — keeps
 *     hierarchy readable from across the room.
 *   - Bottom action bar with sticky primary button (orange brand);
 *     secondary actions sit left of it.
 */

/**
 * Full-screen scaffold for hero pages (Setup wizard, onboarding flows,
 * permission gates). Header at top, scrollable content in the middle,
 * fixed action row at the bottom. The action row never floats away —
 * it stays visible regardless of content length.
 */
@Composable
fun PsHeroFrame(
    title: String,
    subtitle: String? = null,
    stepCurrent: Int? = null,
    stepTotal: Int? = null,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(start = 64.dp, end = 64.dp, top = 56.dp, bottom = 32.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        subtitle,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
            if (stepCurrent != null && stepTotal != null) {
                PsStepIndicator(stepCurrent, stepTotal)
            }
        }

        Spacer(Modifier.height(36.dp))

        // Content area takes all remaining vertical space
        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)) { content() }

        // Sticky bottom action bar
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) { actions() }
    }
}

/**
 * Step indicator — a dot per step + "X / N" caption. Brighter dots are
 * completed/current; dim dots are upcoming. Reads at TV distance because
 * the dots are 12dp and lean on contrast (primary vs surface variant).
 */
@Composable
fun PsStepIndicator(current: Int, total: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (i in 0 until total) {
            val active = i <= current
            Box(
                modifier = Modifier
                    .size(if (i == current) 14.dp else 10.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
            )
        }
        Text(
            "${current + 1} / $total",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

/**
 * Big choice tile — meant for "pick one of these" decisions (theme,
 * language, trigger button, room city). Renders as a large rectangle
 * with optional leading icon, big title, and an optional description
 * underneath. Fills available width by default; in grids it'll size
 * to the cell.
 *
 * Focus animation: scale 1.04 + lift (8dp shadow) + brighter container.
 * Selected state: orange container, primary border-tint via colors.
 */
@Composable
fun PsBigChoice(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.04f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "ps-choice-scale",
    )
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(18.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface,
            focusedContainerColor = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 1f),
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onBackground,
            focusedContentColor = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.primary,
        ),
        border = NoFocusBorder,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = if (focused) 18.dp else 0.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = MaterialTheme.colorScheme.primary,
            )
            .onFocusChanged { focused = it.isFocused },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 22.dp),
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (selected) Color.White.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icon, null,
                        tint = if (selected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Spacer(Modifier.width(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (!description.isNullOrBlank()) {
                    Text(
                        description,
                        fontSize = 13.sp,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✓", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

/**
 * Primary action button — large, brand-orange, glows on focus.
 * Use for the "main" action on a screen (Next, Save, Continue).
 */
@Composable
fun PsPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.04f else 1f,
        animationSpec = tween(150),
        label = "ps-primary-scale",
    )
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            focusedContentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        border = NoFocusBorder,
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (focused) 14.dp else 4.dp,
                shape = RoundedCornerShape(50),
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = MaterialTheme.colorScheme.primary,
            )
            .onFocusChanged { focused = it.isFocused },
    ) {
        Text(
            text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 36.dp, vertical = 14.dp),
        )
    }
}

/**
 * Secondary action button — outlined / muted, lifts on focus but stays
 * subordinate to the primary. Use for Back, Skip, Cancel.
 */
@Composable
fun PsSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.04f else 1f,
        animationSpec = tween(150),
        label = "ps-secondary-scale",
    )
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        border = NoFocusBorder,
        modifier = modifier
            .scale(scale)
            .onFocusChanged { focused = it.isFocused },
    ) {
        Text(
            text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 14.dp),
        )
    }
}

/**
 * Tertiary / "ghost" button — visually distinct from PsSecondary so that
 * Skip / Cancel / Close in a row of buttons doesn't look like Back. Muted
 * text, no fill, dashed outline that becomes solid on focus. We use this
 * for the wizard's Skip — previously it shared PsSecondary with Back and
 * the user couldn't tell them apart from across the room.
 */
@Composable
fun PsTertiaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.04f else 1f,
        animationSpec = tween(150),
        label = "ps-tertiary-scale",
    )
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color(0x33EF4444), // red-tinted on focus
            contentColor = Color(0xFF94A3B8),         // muted slate
            focusedContentColor = Color(0xFFFCA5A5),  // soft red on focus
        ),
        border = androidx.tv.material3.ClickableSurfaceDefaults.border(
            border = androidx.tv.material3.Border(
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Color(0xFF94A3B8).copy(alpha = 0.4f),
                ),
                shape = RoundedCornerShape(50),
            ),
            focusedBorder = androidx.tv.material3.Border(
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    Color(0xFFEF4444),
                ),
                shape = RoundedCornerShape(50),
            ),
        ),
        modifier = modifier
            .scale(scale)
            .onFocusChanged { focused = it.isFocused },
    ) {
        Text(
            text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )
    }
}

/**
 * Big tile — square-ish card for app-launcher-style grids (Dashboard
 * media tiles, quick-access). Larger than ZhCard, focus-aware, with
 * optional accent color (e.g. Netflix red, Spotify green).
 */
@Composable
fun PsBigTile(
    title: String,
    icon: ImageVector,
    accent: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.06f else 1f,
        animationSpec = tween(150),
        label = "ps-tile-scale",
    )
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = accent.copy(alpha = 0.30f),
            contentColor = MaterialTheme.colorScheme.onBackground,
            focusedContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        border = NoFocusBorder,
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (focused) 16.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = accent,
                spotColor = accent,
            )
            .onFocusChanged { focused = it.isFocused },
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 22.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
