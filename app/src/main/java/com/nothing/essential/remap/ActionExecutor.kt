package com.nothing.essential.remap

import android.accessibilityservice.AccessibilityService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock
import android.provider.Settings

object ActionExecutor {

    private var torchOn = false
    private var torchCameraId: String? = null

    fun execute(context: Context, action: String) {
        Handler(Looper.getMainLooper()).post {
            when (action) {
                PrefsManager.ACTION_FLASHLIGHT -> toggleFlashlight(context)
                PrefsManager.ACTION_GLYPTORCH  -> toggleGlyphTorch(context)
                PrefsManager.ACTION_CAMERA     -> openCamera(context)
                PrefsManager.ACTION_DND        -> toggleDND(context)
                PrefsManager.ACTION_SLEEP      -> openSleep(context)
                PrefsManager.ACTION_TIMER      -> openTimer(context)
                PrefsManager.ACTION_HOTSPOT    -> openHotspot(context)
                PrefsManager.ACTION_FLIGHT     -> openFlightMode(context)
                PrefsManager.ACTION_BATTERY    -> openBatterySaver(context)
                PrefsManager.ACTION_BLUETOOTH  -> openBluetooth(context)
                PrefsManager.ACTION_MODES      -> openNothingModes(context)
                PrefsManager.ACTION_QR         -> openQRScanner(context)
            }
        }
    }

    private fun toggleFlashlight(context: Context) {
        try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            if (torchCameraId == null) torchCameraId = cm.cameraIdList.firstOrNull()
            torchCameraId?.let { id ->
                torchOn = !torchOn
                cm.setTorchMode(id, torchOn)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun toggleGlyphTorch(context: Context) {
        try {
            context.sendBroadcast(Intent("com.nothing.ketchum.action.TOGGLE_GLYPH"))
        } catch (_: Exception) { toggleFlashlight(context) }
    }

    private fun openCamera(context: Context) {
        try {
            context.startActivity(Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } catch (_: Exception) {}
        }
    }

    private fun toggleDND(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
            return
        }
        nm.setInterruptionFilter(
            if (nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL)
                NotificationManager.INTERRUPTION_FILTER_NONE
            else
                NotificationManager.INTERRUPTION_FILTER_ALL
        )
    }

    private fun openSleep(context: Context) {
        EssentialButtonService.instance?.performGlobalAction(
            AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN
        ) ?: context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun openTimer(context: Context) {
        try {
            context.startActivity(Intent(AlarmClock.ACTION_SET_TIMER).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } catch (_: Exception) {}
        }
    }

    private fun openHotspot(context: Context) {
        try {
            context.startActivity(Intent().apply {
                setClassName("com.android.settings", "com.android.settings.TetherSettings")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } catch (_: Exception) {}
        }
    }

    private fun openFlightMode(context: Context) {
        context.startActivity(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun openBatterySaver(context: Context) {
        try {
            context.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } catch (_: Exception) {}
        }
    }

    private fun openBluetooth(context: Context) {
        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun openNothingModes(context: Context) {
        try {
            context.startActivity(Intent().apply {
                setClassName("com.nothing.launcher", "com.nothing.launcher.modes.ModesActivity")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (_: Exception) {
            try {
                context.startActivity(Intent().apply {
                    setClassName("com.nothing.settings", "com.nothing.settings.MainActivity")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } catch (_: Exception) {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        }
    }

    private fun openQRScanner(context: Context) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("zxing://scan/")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (_: Exception) {
            try {
                context.startActivity(Intent("com.google.zxing.client.android.SCAN").apply {
                    putExtra("SCAN_MODE", "QR_CODE_MODE")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } catch (_: Exception) { openCamera(context) }
        }
    }
}
