package com.zeddihub.tv.routine

import com.squareup.moshi.JsonClass

/**
 * A single step in a bedtime routine. Each step has a `kind` discriminator
 * and step-specific parameters; the runner switches on kind.
 *
 * Why a flat shape with optional fields rather than a sealed hierarchy:
 * Moshi's polymorphic adapter infrastructure is more involved than the
 * benefit warrants here — there are 4 kinds total and they all have small
 * param sets.
 */
@JsonClass(generateAdapter = true)
data class RoutineStep(
    val kind: String,                  // "volume_fade" | "start_timer" | "webhook" | "delay"
    val durationSeconds: Int = 0,      // for volume_fade, delay, start_timer (used as target volume for fade)
    val targetVolumePct: Int = 0,      // 0..100 for volume_fade
    val timerMinutes: Int = 0,         // for start_timer
    val webhookUrl: String = "",       // for webhook
    val webhookMethod: String = "GET", // GET or POST
    val webhookBody: String = "",      // for POST
)

object RoutineKinds {
    const val VOLUME_FADE = "volume_fade"
    const val START_TIMER = "start_timer"
    const val WEBHOOK     = "webhook"
    const val DELAY       = "delay"
}

/** A pre-shipped default routine the user can edit; written on first open. */
fun defaultBedtimeRoutine(): List<RoutineStep> = listOf(
    RoutineStep(kind = RoutineKinds.VOLUME_FADE, durationSeconds = 30, targetVolumePct = 30),
    RoutineStep(kind = RoutineKinds.START_TIMER, timerMinutes = 30),
)
