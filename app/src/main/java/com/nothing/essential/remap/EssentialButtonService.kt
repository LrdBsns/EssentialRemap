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

        val NOTHING_OVERLAY_PACKAGES = setOf(
            "com.nothing.launcher",
            "com.nothing.essential",
            "com.nothing.settings",
            "com.nothing.systemui",
            "com.nothing.ketchum",
            "com.nothing.home"
        )
    }

    private val prefs by lazy { PrefsManager(this) }
    private val handler = Handler(Looper.getMainLooper())
    private var keyDownTime = 0L
    private var shouldSuppressOverlay = false

    override fun onServiceConnected() {
        instance = this
        serviceInfo = serviceInfo?.apply {
            flags = flags or
                AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            // Nur Window-Events – nicht VIEW_FOCUSED (spart viel CPU)
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 0
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Nur auswerten wenn wir aktiv supprimieren
        if (!shouldSuppressOverlay) return
        if (!prefs.isEnabled()) return
        val pkg = event?.packageName?.toString() ?: return
        if (pkg in NOTHING_OVERLAY_PACKAGES) {
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val code = event.keyCode

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
                shouldSuppressOverlay = true
                performGlobalAction(GLOBAL_ACTION_BACK)
                true
            }
            KeyEvent.ACTION_UP -> {
                val duration = System.currentTimeMillis() - keyDownTime
                if (duration < 800L) {
                    handler.postDelayed({
                        shouldSuppressOverlay = false
                        handlePress()
                    }, 150)
                } else {
                    shouldSuppressOverlay = false
                }
                true
            }
            KeyEvent.ACTION_MULTIPLE -> true
            else -> false
        }
    }

    private fun handlePress() {
        when (prefs.getMode()) {
            PrefsManager.MODE_SINGLE -> ActionExecutor.execute(this, prefs.getSingleAction())
            PrefsManager.MODE_PANEL  -> OverlayManager.showQuickPanel(this)
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        instance = null
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
