package com.zeddihub.tv.alerts

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Banner overlay for server-down + admin push alerts. Shown at the top
 * of the screen over any app, with a dismiss button. Distinct from the
 * Sleep Timer overlay (different position, different styling) so users
 * never mistake one for the other.
 */
@Singleton
class AlertOverlayManager @Inject constructor(
    @ApplicationContext private val ctx: Context,
) {
    private val wm by lazy { ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val mainHandler = Handler(Looper.getMainLooper())
    private var view: View? = null
    private var currentAlertId: String? = null

    private val overlayType: Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

    fun canDrawOverlays(): Boolean = Settings.canDrawOverlays(ctx)

    fun show(alert: AlertJson) {
        if (!canDrawOverlays()) return
        if (currentAlertId == alert.id) return // already shown
        mainHandler.post {
            hideInternal()
            view = buildView(alert)
            wm.addView(view, layoutParams())
            currentAlertId = alert.id
            // Auto-dismiss after TTL
            mainHandler.postDelayed({ if (currentAlertId == alert.id) hideInternal() }, alert.ttl * 1000L)
        }
    }

    fun hide() = mainHandler.post { hideInternal() }

    private fun hideInternal() {
        view?.let { runCatching { wm.removeView(it) } }
        view = null
        currentAlertId = null
    }

    @SuppressLint("SetTextI18n")
    private fun buildView(alert: AlertJson): View {
        val px = { dp: Int -> (dp * ctx.resources.displayMetrics.density).toInt() }
        val accent = when (alert.severity.lowercase()) {
            "error" -> Color.parseColor("#FFEF4444")
            "warn"  -> Color.parseColor("#FFF59E0B")
            else    -> Color.parseColor("#FF3B82F6")
        }
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(px(20), px(14), px(20), px(14))
            background = GradientDrawable().apply {
                cornerRadius = px(14).toFloat()
                setColor(Color.parseColor("#F5080810"))
                setStroke(px(2), accent)
            }
            elevation = px(8).toFloat()
        }
        val icon = TextView(ctx).apply {
            text = when (alert.severity.lowercase()) { "error" -> "⚠"; "warn" -> "⚠"; else -> "ℹ" }
            textSize = 22f
            setTextColor(accent)
            setPadding(0, 0, px(14), 0)
        }
        val textCol = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        textCol.addView(TextView(ctx).apply {
            text = alert.title
            setTextColor(Color.WHITE)
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        if (alert.message.isNotBlank()) {
            textCol.addView(TextView(ctx).apply {
                text = alert.message
                setTextColor(Color.parseColor("#FFA0A0B8"))
                textSize = 13f
                setPadding(0, px(2), 0, 0)
            })
        }
        val dismiss = Button(ctx).apply {
            text = "OK"
            setTextColor(Color.WHITE)
            isAllCaps = false
            background = GradientDrawable().apply {
                cornerRadius = px(8).toFloat()
                setColor(accent)
            }
            setOnClickListener { hide() }
            isFocusable = true
        }
        container.addView(icon)
        container.addView(textCol)
        container.addView(dismiss)
        return container
    }

    private fun layoutParams(): WindowManager.LayoutParams {
        val px = { dp: Int -> (dp * ctx.resources.displayMetrics.density).toInt() }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = px(40)
        }
    }
}
