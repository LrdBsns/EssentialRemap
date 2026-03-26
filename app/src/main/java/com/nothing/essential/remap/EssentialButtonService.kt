package com.nothing.essential.remap

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class EssentialButtonService : AccessibilityService() {

    companion object {
        var instance: EssentialButtonService? = null

        val CANDIDATE_KEYCODES = setOf(
            KeyEvent.KEYCODE_STEM_PRIMARY,
            KeyEvent.KEYCODE_STEM_1,
            KeyEvent.KEYCODE_STEM_2,
            KeyEvent.KEYCODE_ASSIST,
            KeyEvent.KEYCODE_SETTINGS,
            249, 250, 251, 252
        )

        var detectMode = false
        var detectedCallback: ((Int) -> Unit)? = null
    }

    private val prefs by lazy { PrefsManager(this) }
    private val handler = Handler(Looper.getMainLooper())
    private var keyDownTime = 0L

    override fun onServiceConnected() {
        instance = this
        serviceInfo = serviceInfo?.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val code = event.keyCode

        // ── Detection mode ───────────────────────────────────────────────
        if (detectMode) {
            if (event.action == KeyEvent.ACTION_UP) {
                detectMode = false
                detectedCallback?.invoke(code)
                detectedCallback = null
            }
            return true
        }

        if (!prefs.isEnabled()) return false

        val detectedCode = prefs.getDetectedCode()
        val isEssentialButton = if (detectedCode != -1) {
            code == detectedCode
        } else {
            code in CANDIDATE_KEYCODES
        }
        if (!isEssentialButton) return false

        return when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                keyDownTime = System.currentTimeMillis()
                // Dismiss any Nothing overlay that might appear on DOWN
                dismissNothingOverlay()
                true
            }
            KeyEvent.ACTION_UP -> {
                val duration = System.currentTimeMillis() - keyDownTime
                // Dismiss again on UP in case it appeared slightly later
                dismissNothingOverlay()
                if (duration < 800L) {
                    // Small delay so our dismiss fires AFTER Nothing's overlay appears
                    handler.postDelayed({ handlePress() }, 80)
                }
                true
            }
            KeyEvent.ACTION_MULTIPLE -> true
            else -> false
        }
    }

    private fun dismissNothingOverlay() {
        // Fire BACK immediately and again after 150ms to catch the overlay
        // regardless of when Nothing's system renders it
        performGlobalAction(GLOBAL_ACTION_BACK)
        handler.postDelayed({
            performGlobalAction(GLOBAL_ACTION_BACK)
        }, 150)
    }

    private fun handlePress() {
        when (prefs.getMode()) {
            PrefsManager.MODE_SINGLE -> ActionExecutor.execute(this, prefs.getSingleAction())
            PrefsManager.MODE_PANEL  -> OverlayManager.showQuickPanel(this)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        instance = null
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
