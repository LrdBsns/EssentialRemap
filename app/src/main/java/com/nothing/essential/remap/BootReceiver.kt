package com.nothing.essential.remap

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            Log.d("EssentialRemap", "Boot completed – accessibility service will auto-restart if enabled")
            // Accessibility services are automatically restarted by the OS after reboot
            // if the user had them enabled. Nothing extra needed here.
        }
    }
}
