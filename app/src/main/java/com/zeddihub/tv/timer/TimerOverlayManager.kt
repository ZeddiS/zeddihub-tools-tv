package com.zeddihub.tv.timer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Floating overlay shown over all apps. Two views:
 *   - chip: small countdown chip (corner-anchored, ~64×40 dp)
 *   - quickActions: expanded panel with Pause/Stop/Shutdown buttons,
 *     shown when user long-presses the configured trigger key.
 */
@Singleton
class TimerOverlayManager @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val timerState: TimerState,
    private val prefs: com.zeddihub.tv.data.prefs.AppPrefs,
) {
    private val wm by lazy { ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.Main)

    private var chipView: View? = null
    private var chipText: TextView? = null
    private var quickActionsView: View? = null
    private var stateJob: Job? = null

    private val overlayType: Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

    fun canDrawOverlays(): Boolean = Settings.canDrawOverlays(ctx)

    fun show() {
        if (!canDrawOverlays()) return
        mainHandler.post {
            if (chipView == null) {
                chipView = buildChip()
                wm.addView(chipView, chipLayoutParams())
            }
            if (stateJob == null) {
                stateJob = scope.launch {
                    timerState.state.collectLatest { snap ->
                        chipText?.text = formatRemaining(snap.remainingMs)
                    }
                }
            }
        }
    }

    fun refresh() {
        mainHandler.post {
            chipText?.text = formatRemaining(timerState.current().remainingMs)
        }
    }

    fun hide() {
        mainHandler.post {
            stateJob?.cancel(); stateJob = null
            chipView?.let { runCatching { wm.removeView(it) } }
            chipView = null; chipText = null
            quickActionsView?.let { runCatching { wm.removeView(it) } }
            quickActionsView = null
        }
    }

    fun showQuickActions() {
        if (!canDrawOverlays()) return
        mainHandler.post {
            if (quickActionsView != null) return@post
            quickActionsView = buildQuickActions()
            wm.addView(quickActionsView, quickActionsLayoutParams())
        }
    }

    fun hideQuickActions() {
        mainHandler.post {
            quickActionsView?.let { runCatching { wm.removeView(it) } }
            quickActionsView = null
        }
    }

    @SuppressLint("SetTextI18n")
    private fun buildChip(): View {
        val px = { dp: Int -> (dp * ctx.resources.displayMetrics.density).toInt() }
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(px(12), px(6), px(12), px(6))
            background = GradientDrawable().apply {
                cornerRadius = px(20).toFloat()
                setColor(Color.parseColor("#CC080810"))
                setStroke(px(1), Color.parseColor("#FF3B82F6"))
            }
        }
        val text = TextView(ctx).apply {
            setTextColor(Color.WHITE)
            textSize = 14f
            text = "0:00"
        }
        container.addView(text)
        chipText = text
        return container
    }

    private fun chipLayoutParams(): WindowManager.LayoutParams {
        val cornerIndex = runBlocking { runCatching { prefs.timerCorner.first() }.getOrDefault(1) }
        val gravity = when (cornerIndex) {
            0 -> Gravity.TOP or Gravity.START
            1 -> Gravity.TOP or Gravity.END
            2 -> Gravity.BOTTOM or Gravity.START
            3 -> Gravity.BOTTOM or Gravity.END
            else -> Gravity.TOP or Gravity.END
        }
        val px = { dp: Int -> (dp * ctx.resources.displayMetrics.density).toInt() }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            this.gravity = gravity
            x = px(24)
            y = px(48)
        }
    }

    private fun buildQuickActions(): View {
        val px = { dp: Int -> (dp * ctx.resources.displayMetrics.density).toInt() }
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(px(20), px(20), px(20), px(20))
            background = GradientDrawable().apply {
                cornerRadius = px(16).toFloat()
                setColor(Color.parseColor("#F2080810"))
                setStroke(px(1), Color.parseColor("#FF3B82F6"))
            }
        }
        container.addView(TextView(ctx).apply {
            text = ctx.getString(com.zeddihub.tv.R.string.timer_quick_actions_title)
            setTextColor(Color.WHITE)
            textSize = 18f
        })
        val current = timerState.current()
        val pauseLabel = if (current.status == TimerStatus.PAUSED)
            ctx.getString(com.zeddihub.tv.R.string.timer_resume)
        else
            ctx.getString(com.zeddihub.tv.R.string.timer_pause)

        container.addView(actionButton(pauseLabel) {
            val act = if (current.status == TimerStatus.PAUSED)
                TimerActions.ACTION_RESUME else TimerActions.ACTION_PAUSE
            TimerActions.send(ctx, act)
            hideQuickActions()
        })
        container.addView(actionButton(ctx.getString(com.zeddihub.tv.R.string.timer_stop)) {
            TimerActions.send(ctx, TimerActions.ACTION_STOP)
            hideQuickActions()
        })
        container.addView(actionButton(ctx.getString(com.zeddihub.tv.R.string.timer_shutdown_now)) {
            TimerActions.send(ctx, TimerActions.ACTION_SHUTDOWN_NOW)
            hideQuickActions()
        })
        return container
    }

    private fun actionButton(text: String, onClick: () -> Unit): Button {
        val px = { dp: Int -> (dp * ctx.resources.displayMetrics.density).toInt() }
        return Button(ctx).apply {
            this.text = text
            setTextColor(Color.WHITE)
            isAllCaps = false
            background = GradientDrawable().apply {
                cornerRadius = px(8).toFloat()
                setColor(Color.parseColor("#143B82F6"))
                setStroke(px(1), Color.parseColor("#FF3B82F6"))
            }
            setOnClickListener { onClick() }
            setOnFocusChangeListener { v, has ->
                (v.background as? GradientDrawable)?.setColor(
                    if (has) Color.parseColor("#553B82F6") else Color.parseColor("#143B82F6")
                )
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = px(12) }
            isFocusable = true
            isFocusableInTouchMode = true
        }
    }

    private fun quickActionsLayoutParams(): WindowManager.LayoutParams =
        WindowManager.LayoutParams(
            (ctx.resources.displayMetrics.density * 320).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_DIM_BEHIND,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.CENTER
            dimAmount = 0.5f
        }

    private fun <T> runBlocking(block: suspend () -> T): T =
        kotlinx.coroutines.runBlocking { block() }
}
