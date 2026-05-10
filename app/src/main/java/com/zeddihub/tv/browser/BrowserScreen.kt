package com.zeddihub.tv.browser

import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Public
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.zeddihub.tv.ui.components.PageHeader
import com.zeddihub.tv.ui.components.PsSecondaryButton
import com.zeddihub.tv.ui.components.SectionTitle
import com.zeddihub.tv.ui.components.StatusPill
import com.zeddihub.tv.ui.components.Tone
import com.zeddihub.tv.ui.components.ZhCard

/**
 * Embedded browser. Uses Android's WebView with an in-memory cookie jar
 * (no persistent cookies — we don't want a TV to remember login state for
 * sensitive sites unless the user explicitly opts in). Bookmarks are
 * persisted in DataStore.
 *
 * D-pad navigation: WebView handles arrow keys natively for focusable
 * elements (links, inputs); we don't try to override that. The address
 * bar is at the top; bookmarks are pinned to a horizontal row above it
 * so the user can pick one quickly.
 */
@Composable
fun BrowserScreen(vm: BrowserViewModel = hiltViewModel()) {
    val bookmarks by vm.bookmarks.collectAsState()
    var url by remember { mutableStateOf("") }
    var pendingLoad by remember { mutableStateOf<String?>(null) }
    var pageTitle by remember { mutableStateOf("") }
    var loadingProgress by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(end = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {

        PageHeader(
            title = "Prohlížeč",
            subtitle = if (pageTitle.isNotBlank()) "$pageTitle  ·  $url"
                       else "Vyber záložku níže — TV-friendly UA bez ukládání cookies.",
            icon = Icons.Outlined.Public,
            trailing = {
                PsSecondaryButton(text = "🏠 Domů", onClick = {
                    url = ""; pageTitle = ""; pendingLoad = "about:blank"
                })
            },
        )

        // Status strip
        ZhCard(container = MaterialTheme.colorScheme.surfaceVariant) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusPill(
                    label = if (loadingProgress in 1..99) "Načítám ${loadingProgress}%"
                            else if (loadingProgress == 100) "Načteno" else "—",
                    tone = if (loadingProgress in 1..99) Tone.Info
                           else if (loadingProgress == 100) Tone.Success else Tone.Muted,
                )
                StatusPill(label = "TV UA", tone = Tone.Info)
                StatusPill(label = "Bez cookies", tone = Tone.Warning)
            }
        }

        SectionTitle("Záložky")

        // Bookmarks tile row (D-pad scrollable horizontally)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            bookmarks.take(8).forEach { b ->
                BookmarkPill(
                    title = b.title,
                    onClick = {
                        pendingLoad = b.url
                        url = b.url
                    },
                )
            }
        }

        // WebView
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .weight(1f)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        // TV viewing: most sites assume desktop UA on a 1080p
                        // panel; mobile UA breaks layouts and YouTube routes
                        // to the m.youtube.com page that's hostile to D-pad.
                        settings.userAgentString = settings.userAgentString
                            .replace("; wv", "")
                            .replace("Mobile", "")
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        // No persistent cookies — TV is shared, leak risk.
                        // Users who want logins use the streaming apps instead.
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, loadedUrl: String?) {
                                url = loadedUrl ?: ""
                                pageTitle = view?.title.orEmpty()
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                loadingProgress = newProgress
                            }
                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                pageTitle = title.orEmpty()
                            }
                        }
                        loadUrl(DefaultBookmarks.all.first().url)
                    }
                },
                update = { webView ->
                    pendingLoad?.let {
                        webView.loadUrl(it)
                        pendingLoad = null
                    }
                },
            )
            if (loadingProgress in 1..99) {
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { loadingProgress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * Pill-shaped bookmark tile — primary border on focus.
 */
@Composable
private fun BookmarkPill(title: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onBackground,
            focusedContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(
            title,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
