package com.nothing.essential.remap

import android.content.Context
import android.content.SharedPreferences

class PrefsManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "essential_remap"

        const val KEY_ENABLED        = "enabled"
        const val KEY_BUTTON_MODE    = "button_mode"
        const val KEY_SINGLE_ACTION  = "single_action"
        const val KEY_DETECTED_CODE  = "detected_keycode"
        const val KEY_QUICK_ACTIONS  = "quick_actions"

        const val MODE_SINGLE = "single"
        const val MODE_PANEL  = "panel"

        const val ACTION_FLASHLIGHT   = "flashlight"
        const val ACTION_GLYPTORCH    = "glyptorch"
        const val ACTION_CAMERA       = "camera"
        const val ACTION_DND          = "dnd"
        const val ACTION_SLEEP        = "sleep"
        const val ACTION_TIMER        = "timer"
        const val ACTION_HOTSPOT      = "hotspot"
        const val ACTION_FLIGHT       = "flight_mode"
        const val ACTION_BATTERY      = "battery_saver"
        const val ACTION_BLUETOOTH    = "bluetooth"
        const val ACTION_MODES        = "modes"
        const val ACTION_QR           = "qr_scanner"

        val ALL_ACTIONS = listOf(
            ACTION_FLASHLIGHT, ACTION_GLYPTORCH, ACTION_CAMERA,
            ACTION_DND, ACTION_SLEEP, ACTION_TIMER,
            ACTION_HOTSPOT, ACTION_FLIGHT, ACTION_BATTERY,
            ACTION_BLUETOOTH, ACTION_MODES, ACTION_QR
        )

        val DEFAULT_QUICK_ACTIONS: Set<String> = setOf(
            ACTION_FLASHLIGHT, ACTION_CAMERA, ACTION_DND, ACTION_TIMER, ACTION_BLUETOOTH
        )

        fun actionLabel(action: String) = when (action) {
            ACTION_FLASHLIGHT  -> "Flashlight"
            ACTION_GLYPTORCH   -> "Glyph Torch"
            ACTION_CAMERA      -> "Camera"
            ACTION_DND         -> "Do Not Disturb"
            ACTION_SLEEP       -> "Sleep / Display"
            ACTION_TIMER       -> "Timer"
            ACTION_HOTSPOT     -> "Hotspot"
            ACTION_FLIGHT      -> "Aeroplane Mode"
            ACTION_BATTERY     -> "Battery Saver"
            ACTION_BLUETOOTH   -> "Bluetooth"
            ACTION_MODES       -> "Nothing Modes"
            ACTION_QR          -> "QR Scanner"
            else               -> action
        }

        fun actionEmoji(action: String) = when (action) {
            ACTION_FLASHLIGHT  -> "◎"
            ACTION_GLYPTORCH   -> "✦"
            ACTION_CAMERA      -> "⊙"
            ACTION_DND         -> "◉"
            ACTION_SLEEP       -> "◐"
            ACTION_TIMER       -> "◷"
            ACTION_HOTSPOT     -> "⊕"
            ACTION_FLIGHT      -> "△"
            ACTION_BATTERY     -> "▣"
            ACTION_BLUETOOTH   -> "◈"
            ACTION_MODES       -> "◆"
            ACTION_QR          -> "▦"
            else               -> "○"
        }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled()        = prefs.getBoolean(KEY_ENABLED, false)
    fun setEnabled(v: Boolean) { prefs.edit().putBoolean(KEY_ENABLED, v).apply() }

    fun getMode()          = prefs.getString(KEY_BUTTON_MODE, MODE_SINGLE) ?: MODE_SINGLE
    fun setMode(v: String) { prefs.edit().putString(KEY_BUTTON_MODE, v).apply() }

    fun getSingleAction()          = prefs.getString(KEY_SINGLE_ACTION, ACTION_FLASHLIGHT) ?: ACTION_FLASHLIGHT
    fun setSingleAction(v: String) { prefs.edit().putString(KEY_SINGLE_ACTION, v).apply() }

    fun getDetectedCode()      = prefs.getInt(KEY_DETECTED_CODE, -1)
    fun setDetectedCode(v: Int){ prefs.edit().putInt(KEY_DETECTED_CODE, v).apply() }
    fun clearDetectedCode()    { prefs.edit().putInt(KEY_DETECTED_CODE, -1).apply() }

    fun getQuickActions(): Set<String> =
        prefs.getStringSet(KEY_QUICK_ACTIONS, DEFAULT_QUICK_ACTIONS) ?: DEFAULT_QUICK_ACTIONS
    fun setQuickActions(v: Set<String>) { prefs.edit().putStringSet(KEY_QUICK_ACTIONS, v).apply() }
}
