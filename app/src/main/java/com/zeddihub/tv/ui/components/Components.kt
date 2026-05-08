package com.zeddihub.tv.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text

/**
 * Shared design primitives for ZeddiHub TV. All screens are gradually
 * migrated to use these so spacing, typography, and color usage stay
 * consistent — the per-screen ad-hoc styles drifted across sessions.
 *
 * Design tokens (informal):
 *   - Page horizontal padding: 0.dp (the side rail already insets the
 *     content area; use ZhPageScaffold to apply only vertical padding).
 *   - Header title: 26.sp Bold; subtitle 14.sp.
 *   - Card corner: 14.dp; padding 18.dp.
 *   - Section title: 13.sp Bold UPPERCASE letterSpacing 0.06em onSurfaceVariant.
 *   - Tone colors: green #22C55E (good), yellow #F59E0B (warn),
 *     red #EF4444 (bad), purple #A78BFA (info accent).
 */
object Tone {
    val Success = Color(0xFF22C55E)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    val Info = Color(0xFFA78BFA)
    val Muted = Color(0xFF6B7280)
}

/**
 * Page-level scaffold. Replaces the ad-hoc `Column { padding(8.dp) }` at
 * the top of each screen. Adds vertical breathing room and standardises
 * the gap between header and body.
 */
@Composable
fun ZhPageScaffold(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(end = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        content()
    }
}

/**
 * Big page header. Replaces the variants of "Title 28sp + subtitle 14sp"
 * sprinkled across every screen. Optional leading icon (rendered in a
 * primary-tinted circle) and optional trailing slot for an action button.
 */
@Composable
fun PageHeader(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .padding(),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = CircleShape,
                    colors = SurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    ),
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
        Column(modifier = Modifier
            .padding(start = if (icon != null) 14.dp else 0.dp)
            .weight(1f)) {
            Text(
                title,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        if (trailing != null) trailing()
    }
}

/**
 * Standardised card surface. Wraps content in 14.dp rounded corners with
 * 18.dp internal padding. Use everywhere instead of inlining Surface +
 * SurfaceDefaults.colors per screen.
 */
@Composable
fun ZhCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    container: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        colors = SurfaceDefaults.colors(containerColor = container),
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.padding(contentPadding)) { content() }
    }
}

/** Section heading for groups inside a screen (e.g. "Top zařízení"). */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = androidx.compose.ui.unit.TextUnit(0.6f, androidx.compose.ui.unit.TextUnitType.Sp),
        modifier = modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

/** Empty-state card with icon, title, and optional hint. */
@Composable
fun EmptyState(
    title: String,
    hint: String? = null,
    icon: ImageVector? = null,
) {
    ZhCard(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 28.dp),
        container = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (icon != null) {
                Icon(
                    icon, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp),
                )
            }
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (!hint.isNullOrBlank()) {
                Text(
                    hint,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Status badge — small colored pill with label. */
@Composable
fun StatusPill(
    label: String,
    tone: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(50),
        colors = SurfaceDefaults.colors(
            containerColor = tone.copy(alpha = 0.15f),
        ),
        modifier = modifier,
    ) {
        Text(
            label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = tone,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
        )
    }
}

/** Simple labelled metric chip used in stat strips. */
@Composable
fun KpiTile(
    icon: ImageVector,
    label: String,
    value: String,
    tone: Color? = null,
    modifier: Modifier = Modifier,
) {
    val accent = tone ?: MaterialTheme.colorScheme.primary
    ZhCard(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
                Text(
                    label,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Text(
                value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
