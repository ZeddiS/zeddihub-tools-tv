package com.zeddihub.tv.data.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.zeddihub.tv.BuildConfig
import com.zeddihub.tv.ui.components.PsPrimaryButton
import com.zeddihub.tv.ui.components.PsSecondaryButton
import com.zeddihub.tv.ui.components.StatusPill
import com.zeddihub.tv.ui.components.Tone

/**
 * Cold-start update dialog. Always appears when a newer version_code is
 * served by /api/app-version.php?kind=tv — even if the user dismissed an
 * earlier prompt for the same version. The user explicitly asked for the
 * prompt to show on every app start; we honour that by NOT writing
 * `updateDismissedCode` from the dismiss button.
 *
 * Force updates remove the "Později" button entirely so the user can't
 * skip them.
 */
@Composable
fun StartupUpdateDialog(
    result: UpdateCheckResult,
    onInstall: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = { if (!result.force) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !result.force,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            colors = SurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .fillMaxHeight(0.85f),
        ) {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(36.dp)) {

                // Header — gradient-tinted icon badge + version banner
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary,
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Outlined.NewReleases, null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                    Spacer(Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Nová verze je k dispozici",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                result.versionName,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.width(12.dp))
                            StatusPill(
                                label = "code ${result.versionCode}",
                                tone = Tone.Info,
                            )
                            if (result.force) {
                                Spacer(Modifier.width(8.dp))
                                StatusPill(
                                    label = "POVINNÁ",
                                    tone = Tone.Error,
                                )
                            }
                        }
                        Text(
                            "Aktuálně máš ${BuildConfig.VERSION_NAME} (code ${BuildConfig.VERSION_CODE}).",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Release notes — scrollable, fills space
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    colors = SurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Text(
                            "Co je nového",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = androidx.compose.ui.unit.TextUnit(
                                0.6f,
                                androidx.compose.ui.unit.TextUnitType.Sp,
                            ),
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            result.notes.ifBlank { "Bez popisu změn." },
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 22.sp,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Footer actions
                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (!result.force) {
                        PsSecondaryButton(text = "Později", onClick = onDismiss)
                    }
                    Box(modifier = Modifier.weight(1f))
                    PsPrimaryButton(
                        text = "⬇ Stáhnout a nainstalovat",
                        onClick = onInstall,
                    )
                }
            }
        }
    }
}
