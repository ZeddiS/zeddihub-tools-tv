package com.zeddihub.tv.data.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.zeddihub.tv.BuildConfig
import com.zeddihub.tv.data.prefs.AppPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@JsonClass(generateAdapter = true)
data class UpdateApiResp(val live: UpdateApiLive?)

@JsonClass(generateAdapter = true)
data class UpdateApiLive(
    val version_code: Int,
    val version_name: String,
    val apk_url: String,
    val min_sdk: Int = 26,
    val release_notes_cs: String? = null,
    val release_notes_en: String? = null,
    val force: Boolean = false,
)

data class UpdateCheckResult(
    val isUpdateAvailable: Boolean,
    val versionName: String,
    val versionCode: Int,
    val apkUrl: String,
    val notes: String,
    val force: Boolean,
)

/**
 * Polls the ZeddiHub release-gating endpoint and offers a one-tap install
 * via DownloadManager + system installer. Compatible with the same
 * API used by the mobile and desktop apps — see /api/app-version.php on
 * the website (kind=tv).
 */
@Singleton
class UpdateChecker @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val client: OkHttpClient,
    private val moshi: Moshi,
    private val prefs: AppPrefs,
) {
    private val endpoint =
        "${BuildConfig.API_BASE_URL}app-version.php?kind=${BuildConfig.CLIENT_KIND}&version_code=${BuildConfig.VERSION_CODE}"

    fun checkOnStartup(callerCtx: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(3_000)
            val r = checkNow() ?: return@launch
            if (!r.isUpdateAvailable) return@launch
            val dismissedAt = prefs.updateDismissedCode.first()
            if (!r.force && dismissedAt >= r.versionCode) return@launch
            withContext(Dispatchers.Main) {
                Toast.makeText(callerCtx, "Nová verze ${r.versionName} — otevřete Nastavení pro instalaci.",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    suspend fun checkNow(): UpdateCheckResult? = withContext(Dispatchers.IO) {
        runCatching {
            val resp = client.newCall(Request.Builder().url(endpoint).build()).execute()
            val body = resp.body?.string() ?: return@runCatching null
            val parsed = moshi.adapter(UpdateApiResp::class.java).fromJson(body) ?: return@runCatching null
            val live = parsed.live ?: return@runCatching null
            UpdateCheckResult(
                isUpdateAvailable = live.version_code > BuildConfig.VERSION_CODE,
                versionName = live.version_name,
                versionCode = live.version_code,
                apkUrl = live.apk_url,
                notes = (live.release_notes_cs ?: live.release_notes_en ?: ""),
                force = live.force,
            )
        }.getOrNull()
    }

    fun startInstall(ctx: Context, result: UpdateCheckResult) {
        val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val targetFile = File(ctx.cacheDir, "updates/ZeddiHub-TV-${result.versionName}.apk")
        targetFile.parentFile?.mkdirs()
        if (targetFile.exists()) targetFile.delete()

        val req = DownloadManager.Request(Uri.parse(result.apkUrl))
            .setTitle("ZeddiHub TV ${result.versionName}")
            .setDescription("Aktualizace aplikace")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(targetFile))
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val id = dm.enqueue(req)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val completedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                if (completedId != id) return
                ctx.unregisterReceiver(this)
                openInstaller(ctx, targetFile)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ctx.registerReceiver(receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            ctx.registerReceiver(receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    private fun openInstaller(ctx: Context, file: File) {
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching { ctx.startActivity(intent) }
    }
}
