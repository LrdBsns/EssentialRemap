package com.nothing.essential.remap

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView

object OverlayManager {

    private var wm: WindowManager? = null
    private var root: View?         = null

    fun showQuickPanel(context: Context) {
        if (root != null) { dismiss(); return }

        Handler(Looper.getMainLooper()).post {
            try {
                wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val prefs   = PrefsManager(context)
                val actions = prefs.getQuickActions().toList()

                root = buildPanelView(context, actions)

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    y = 120
                }

                wm!!.addView(root, params)

            } catch (e: Exception) {
                e.printStackTrace()
                root = null
                wm   = null
            }
        }
    }

    fun dismiss() {
        Handler(Looper.getMainLooper()).post {
            try {
                root?.let { wm?.removeView(it) }
            } catch (_: Exception) {
            } finally {
                root = null
                wm   = null
            }
        }
    }

    private fun buildPanelView(context: Context, actions: List<String>): View {
        val dp = { n: Int -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, n.toFloat(), context.resources.displayMetrics).toInt() }

        // ── Outer container (dim scrim) ───────────────────────────────
        val scrim = object : LinearLayout(context) {
            override fun onTouchEvent(e: MotionEvent?): Boolean {
                dismiss(); return true
            }
        }.apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.argb(160, 0, 0, 0))
            gravity = Gravity.BOTTOM
        }

        // ── Card container ────────────────────────────────────────────
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0D0D0D"))
            setPadding(dp(20), dp(20), dp(20), dp(28))
        }

        // Top row: title + close button
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val title = nothingText(context, "ESSENTIAL KEY", 11f, Color.parseColor("#888888")).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            letterSpacing = 0.2f
        }
        val closeBtn = nothingText(context, "✕", 16f, Color.WHITE).apply {
            setPadding(dp(12), dp(4), dp(4), dp(4))
            setOnClickListener { dismiss() }
        }
        topRow.addView(title)
        topRow.addView(closeBtn)
        card.addView(topRow)

        // Divider
        card.addView(View(context).apply {
            setBackgroundColor(Color.parseColor("#222222"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
            ).apply { setMargins(0, dp(12), 0, dp(16)) }
        })

        // ── Grid of action buttons ─────────────────────────────────────
        val cols = if (actions.size <= 4) 2 else 3
        val grid = GridLayout(context).apply {
            columnCount = cols
            rowCount    = (actions.size + cols - 1) / cols
        }

        actions.forEachIndexed { _, action ->
            val cell = buildActionCell(context, action, dp)
            cell.setOnClickListener {
                dismiss()
                ActionExecutor.execute(context, action)
            }
            val spec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            cell.layoutParams = GridLayout.LayoutParams(spec, spec).apply {
                width  = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                setMargins(dp(4), dp(4), dp(4), dp(4))
            }
            grid.addView(cell)
        }

        card.addView(grid)
        scrim.addView(card)
        return scrim
    }

    private fun buildActionCell(context: Context, action: String, dp: (Int) -> Int): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity     = Gravity.CENTER
            setPadding(dp(8), dp(16), dp(8), dp(16))
            setBackgroundColor(Color.parseColor("#161616"))

            // Icon (unicode dot-style glyph)
            val icon = nothingText(context, PrefsManager.actionEmoji(action), 22f, Color.WHITE).apply {
                gravity = Gravity.CENTER
            }
            addView(icon)

            // Label
            val label = nothingText(context, PrefsManager.actionLabel(action).uppercase(), 9f, Color.parseColor("#AAAAAA")).apply {
                gravity     = Gravity.CENTER
                letterSpacing = 0.1f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(6) }
            }
            addView(label)

            // Pressed state
            setOnTouchListener { v, e ->
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> v.setBackgroundColor(Color.parseColor("#2A2A2A"))
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                        v.setBackgroundColor(Color.parseColor("#161616"))
                }
                false
            }
        }
    }

    private fun nothingText(context: Context, text: String, sizeSp: Float, color: Int): TextView =
        TextView(context).apply {
            this.text      = text
            this.textSize  = sizeSp
            setTextColor(color)
            typeface       = Typeface.MONOSPACE
        }
}
