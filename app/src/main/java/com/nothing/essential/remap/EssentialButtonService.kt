package com.nothing.essential.remap

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class EssentialButtonService : AccessibilityService() {

    companion object {
        var instance: EssentialButtonService? = null

        // Possible keycodes used by Nothing's Essential Key across firmware versions
        // KEYCODE_STEM_PRIMARY (265) is the standard Wear OS stem button, reused by some OEMs
        // KEYCODE_PROG1-4 are commonly assigned to OEM custom buttons
        val CANDIDATE_KEYCODES = setOf(
            KeyEvent.KEYCODE_STEM_PRIMARY,  // 265 – most likely
            KeyEvent.KEYCODE_STEM_1,        // 266
            KeyEvent.KEYCODE_STEM_2,        // 267
            KeyEvent.KEYCODE_PROG1,         // 249
            KeyEvent.KEYCODE_PROG2,         // 250
            KeyEvent.KEYCODE_PROG3,         // 251
            KeyEvent.KEYCODE_PROG4,         // 252
            KeyEvent.KEYCODE_F17,           // 131 – used by some OEMs
            KeyEvent.KEYCODE_F18,           // 132
            KeyEvent.KEYCODE_ASSIST,        // 219 – long-press assistant button fallback
            KeyEvent.KEYCODE_SETTINGS,      // 176
        )

        var detectMode = false
        var detectedCallback: ((Int) -> Unit)? = null
    }

    private val prefs by lazy { PrefsManager(this) }
    private var keyDownTime = 0L

    override fun onServiceConnected() {
        instance = this
        // Ensure key-event filtering is enabled at runtime
        serviceInfo = serviceInfo?.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val code = event.keyCode

        // ── Detection mode: capture any key and report back ──────────────
        if (detectMode) {
            if (event.action == KeyEvent.ACTION_UP) {
                detectMode = false
                detectedCallback?.invoke(code)
                detectedCallback = null
            }
            return true
        }

        // ── Normal mode ──────────────────────────────────────────────────
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
                true
            }
            KeyEvent.ACTION_UP -> {
                val duration = System.currentTimeMillis() - keyDownTime
                if (duration < 800L) handlePress()
                true
            }
            else -> false
        }
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
        super.onDestroy()
    }
}
