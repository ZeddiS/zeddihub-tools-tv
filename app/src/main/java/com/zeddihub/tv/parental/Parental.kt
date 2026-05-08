package com.zeddihub.tv.parental

import com.squareup.moshi.JsonClass

/**
 * Per-app parental control entry. `dailyQuotaMin` is wall-clock minutes
 * per local-day; `pinRequired = true` blocks launch entirely until PIN
 * is entered; `bedtimeStartMinute`/`bedtimeEndMinute` are minute-of-day
 * (0..1440) — within that window, the app is blocked regardless of quota.
 *
 * Tracking actual usage requires UsageStatsManager (PACKAGE_USAGE_STATS)
 * which v0.4.x doesn't yet request — so the rules surface as "soft"
 * blocks: the launcher tile shows a lock icon, the launch goes through
 * a PIN gate, and we record self-reported usage via our own click
 * counter. Hard usage caps land in v0.5+.
 */
@JsonClass(generateAdapter = true)
data class ParentalRule(
    val packageName: String,
    val pinRequired: Boolean = false,
    val dailyQuotaMin: Int = 0,        // 0 = no quota
    val bedtimeStartMinute: Int = 0,   // 0..1440; if start == end → no bedtime
    val bedtimeEndMinute: Int = 0,
) {
    fun bedtimeText(): String {
        if (bedtimeStartMinute == bedtimeEndMinute) return "—"
        return "${formatMin(bedtimeStartMinute)}–${formatMin(bedtimeEndMinute)}"
    }

    /** Returns true if the rule's bedtime window contains the given minute-of-day. */
    fun isBedtime(currentMinuteOfDay: Int): Boolean {
        if (bedtimeStartMinute == bedtimeEndMinute) return false
        return if (bedtimeStartMinute < bedtimeEndMinute) {
            currentMinuteOfDay in bedtimeStartMinute until bedtimeEndMinute
        } else {
            // wraps midnight (e.g. 22:00 → 07:00)
            currentMinuteOfDay >= bedtimeStartMinute || currentMinuteOfDay < bedtimeEndMinute
        }
    }

    private fun formatMin(m: Int): String {
        val h = (m / 60) % 24
        val mm = m % 60
        return "%02d:%02d".format(h, mm)
    }
}
