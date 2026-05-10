package com.zeddihub.tv.accessibility

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.PsBigChoice
import com.zeddihub.tv.ui.components.PsSecondaryButton
import com.zeddihub.tv.ui.components.SectionTitle
import com.zeddihub.tv.ui.components.ZhPageScaffold

@Composable
fun AccessibilityScreen(vm: AccessibilityViewModel = hiltViewModel()) {
    val cc by vm.ccUniversal.collectAsState()
    val font by vm.dyslexiaFont.collectAsState()
    val ctx = LocalContext.current

    ZhPageScaffold {
        PageHeader(
            title = "Přístupnost",
            subtitle = "Universal closed captions, dyslektický font a systémové zkratky.",
            icon = Icons.Outlined.Accessibility,
        )

        SectionTitle("Universal toggles")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PsBigChoice(
                title = "Universal closed captions",
                description = if (cc) "ZAPNUTO — vynucujeme titulky napříč podporovanými přehrávači."
                              else "VYPNUTO — titulky se zapínají per-aplikace.",
                icon = Icons.Outlined.ClosedCaption,
                selected = cc,
                onClick = { vm.toggleCc() },
            )
            PsBigChoice(
                title = "Dyslektický font",
                description = if (font) "ZAPNUTO — OpenDyslexic-style font na nadpisech v appce."
                              else "VYPNUTO — používá se výchozí font.",
                icon = Icons.Outlined.FontDownload,
                selected = font,
                onClick = { vm.toggleFont() },
            )
        }

        SectionTitle("Systémové zkratky")
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PsSecondaryButton(
                text = "🅰 Captioning",
                onClick = {
                    ctx.startActivity(Intent(Settings.ACTION_CAPTIONING_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                },
            )
            PsSecondaryButton(
                text = "♿ Accessibility",
                onClick = {
                    ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                },
            )
            PsSecondaryButton(
                text = "🖥 Display",
                onClick = {
                    ctx.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                },
            )
        }
    }
}
