package com.zeddihub.tv.routine

import android.content.Context
import android.media.AudioManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.zeddihub.tv.data.prefs.AppPrefs
import com.zeddihub.tv.timer.TimerActions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Executes a list of [RoutineStep] sequentially. Steps run inline (one
 * after another) — that's the user mental model: "do A, then B, then C".
 *
 * The runner is fire-and-forget; if the user navigates away or closes
 * the app, ongoing webhook calls finish anyway (CoroutineScope tied to
 * the Application).
 */
@Singleton
class RoutineRunner @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val prefs: AppPrefs,
    private val client: OkHttpClient,
    moshi: Moshi,
) {
    private val adapter = moshi.adapter<List<RoutineStep>>(
        Types.newParameterizedType(List::class.java, RoutineStep::class.java)
    )
    private val scope = CoroutineScope(Dispatchers.Default + kotlinx.coroutines.SupervisorJob())

    suspend fun loadBedtime(): List<RoutineStep> =
        runCatching { adapter.fromJson(prefs.bedtimeRoutineJson.first()) ?: emptyList() }
            .getOrDefault(emptyList())

    suspend fun saveBedtime(steps: List<RoutineStep>) {
        prefs.setBedtimeRoutineJson(adapter.toJson(steps))
    }

    fun runBedtime() {
        scope.launch {
            val steps = loadBedtime().ifEmpty { defaultBedtimeRoutine() }
            steps.forEach { runStep(it) }
        }
    }

    private suspend fun runStep(step: RoutineStep) {
        when (step.kind) {
            RoutineKinds.VOLUME_FADE -> fadeVolume(step.durationSeconds, step.targetVolumePct)
            RoutineKinds.START_TIMER -> TimerActions.start(ctx, step.timerMinutes * 60_000L)
            RoutineKinds.DELAY       -> delay(step.durationSeconds * 1000L)
            RoutineKinds.WEBHOOK     -> callWebhook(step)
        }
    }

    private suspend fun fadeVolume(seconds: Int, targetPct: Int) {
        val am = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val startVol = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        val targetVol = (maxVol * targetPct / 100).coerceIn(0, maxVol)
        if (seconds <= 0 || startVol == targetVol) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0); return
        }
        val steps = (seconds * 2).coerceAtLeast(1) // 500 ms granularity
        val stepMs = (seconds * 1000L) / steps
        for (i in 1..steps) {
            val v = startVol + ((targetVol - startVol) * i / steps)
            am.setStreamVolume(AudioManager.STREAM_MUSIC, v, 0)
            delay(stepMs)
        }
    }

    private suspend fun callWebhook(step: RoutineStep) = withContext(Dispatchers.IO) {
        val url = step.webhookUrl.takeIf { it.isNotBlank() } ?: return@withContext
        val req = Request.Builder().url(url).apply {
            if (step.webhookMethod.equals("POST", ignoreCase = true)) {
                val body = step.webhookBody
                    .toRequestBody("application/json".toMediaTypeOrNull())
                post(body)
            }
        }.build()
        runCatching { client.newCall(req).execute().close() }
    }
}
