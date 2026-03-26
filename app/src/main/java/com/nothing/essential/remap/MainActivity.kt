package com.nothing.essential.remap

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.nothing.essential.remap.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Full black immersive experience
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.BLACK
            navigationBarColor = Color.BLACK
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PrefsManager(this)

        setupUI()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    // ─── UI Setup ────────────────────────────────────────────────────────

    private fun setupUI() {
        // ── Enable toggle ─────────────────────────────────────────────
        binding.switchEnable.isChecked = prefs.isEnabled()
        binding.switchEnable.setOnCheckedChangeListener { _, checked ->
            if (checked && !isAccessibilityEnabled()) {
                binding.switchEnable.isChecked = false
                showAccessibilityDialog()
                return@setOnCheckedChangeListener
            }
            prefs.setEnabled(checked)
            refreshStatus()
        }

        // ── Mode selection ────────────────────────────────────────────
        when (prefs.getMode()) {
            PrefsManager.MODE_SINGLE -> binding.rbSingle.isChecked = true
            PrefsManager.MODE_PANEL  -> binding.rbPanel.isChecked  = true
        }
        binding.rgMode.setOnCheckedChangeListener { _, id ->
            val mode = if (id == R.id.rbSingle) PrefsManager.MODE_SINGLE else PrefsManager.MODE_PANEL
            prefs.setMode(mode)
            updateModeVisibility(mode)
        }

        // ── Single action spinner ─────────────────────────────────────
        val labels  = PrefsManager.ALL_ACTIONS.map { PrefsManager.actionLabel(it) }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerAction.adapter = adapter
        val currentIdx = PrefsManager.ALL_ACTIONS.indexOf(prefs.getSingleAction()).coerceAtLeast(0)
        binding.spinnerAction.setSelection(currentIdx)
        binding.spinnerAction.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                prefs.setSingleAction(PrefsManager.ALL_ACTIONS[pos])
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        // ── Quick panel checkboxes ────────────────────────────────────
        buildQuickPanelGrid()

        // ── Mode visibility ───────────────────────────────────────────
        updateModeVisibility(prefs.getMode())

        // ── Permission buttons ────────────────────────────────────────
        binding.btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        binding.btnOverlay.setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")))
        }
        binding.btnDnd.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        }

        // ── Detect button ─────────────────────────────────────────────
        binding.btnDetect.setOnClickListener { startDetection() }
        binding.btnClearDetect.setOnClickListener {
            prefs.clearDetectedCode()
            updateDetectStatus()
            toast("Detection cleared – using default keycodes")
        }

        refreshStatus()
    }

    private fun buildQuickPanelGrid() {
        val container = binding.layoutQuickActions
        container.removeAllViews()

        val selected = prefs.getQuickActions().toMutableSet()

        for (action in PrefsManager.ALL_ACTIONS) {
            val cb = CheckBox(this).apply {
                text      = "${PrefsManager.actionEmoji(action)}  ${PrefsManager.actionLabel(action)}"
                textSize  = 13f
                setTextColor(Color.WHITE)
                isChecked = action in selected
                setButtonDrawable(R.drawable.cb_nothing)
                setOnCheckedChangeListener { _, checked ->
                    if (checked) selected.add(action) else selected.remove(action)
                    if (selected.isEmpty()) {
                        isChecked = true
                        selected.add(action)
                        toast("At least one action required")
                    }
                    prefs.setQuickActions(selected)
                }
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, dpToPx(4)) }
            container.addView(cb, lp)
        }
    }

    private fun updateModeVisibility(mode: String) {
        binding.layoutSingleAction.visibility = if (mode == PrefsManager.MODE_SINGLE) View.VISIBLE else View.GONE
        binding.layoutPanel.visibility        = if (mode == PrefsManager.MODE_PANEL) View.VISIBLE else View.GONE
    }

    // ─── Status refresh ───────────────────────────────────────────────────

    private fun refreshStatus() {
        val accessOk  = isAccessibilityEnabled()
        val overlayOk = Settings.canDrawOverlays(this)
        val enabled   = prefs.isEnabled()

        // Status dot
        val statusColor = when {
            enabled && accessOk -> Color.parseColor("#00FF88")
            accessOk            -> Color.parseColor("#FFCC00")
            else                -> Color.parseColor("#FF3533")
        }
        binding.viewStatusDot.setBackgroundColor(statusColor)

        binding.tvStatusText.text = when {
            enabled && accessOk -> "ACTIVE"
            !accessOk           -> "NEEDS SETUP"
            else                -> "PAUSED"
        }

        // Permission row indicators
        setPermissionState(binding.viewDotAccessibility, binding.tvAccessibilityState, accessOk)
        setPermissionState(binding.viewDotOverlay, binding.tvOverlayState, overlayOk)

        val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        setPermissionState(binding.viewDotDnd, binding.tvDndState, nm.isNotificationPolicyAccessGranted)

        updateDetectStatus()
    }

    private fun setPermissionState(dot: View, label: TextView, granted: Boolean) {
        dot.setBackgroundColor(if (granted) Color.parseColor("#00FF88") else Color.parseColor("#FF3533"))
        label.text = if (granted) "GRANTED" else "REQUIRED"
        label.setTextColor(if (granted) Color.parseColor("#00FF88") else Color.parseColor("#FF3533"))
    }

    private fun updateDetectStatus() {
        val code = prefs.getDetectedCode()
        binding.tvDetectStatus.text = if (code != -1)
            "KEYCODE $code DETECTED"
        else
            "NOT DETECTED – USING AUTO"
        binding.btnClearDetect.visibility = if (code != -1) View.VISIBLE else View.GONE
    }

    // ─── Button detection ─────────────────────────────────────────────────

    private fun startDetection() {
        if (!isAccessibilityEnabled()) {
            showAccessibilityDialog(); return
        }

        val dialog = AlertDialog.Builder(this, R.style.NothingDialog)
            .setTitle("DETECT BUTTON")
            .setMessage("Press your Essential Key now…\n\nThe app will capture its keycode automatically.")
            .setNegativeButton("CANCEL") { d, _ ->
                EssentialButtonService.detectMode = false
                EssentialButtonService.detectedCallback = null
                d.dismiss()
            }
            .setCancelable(false)
            .create()

        EssentialButtonService.detectMode = true
        EssentialButtonService.detectedCallback = { code ->
            runOnUiThread {
                prefs.setDetectedCode(code)
                updateDetectStatus()
                dialog.dismiss()
                toast("Button detected! Keycode: $code")
            }
        }

        dialog.show()

        // Auto-dismiss after 10 seconds
        binding.root.postDelayed({
            if (dialog.isShowing) {
                EssentialButtonService.detectMode = false
                EssentialButtonService.detectedCallback = null
                dialog.dismiss()
                toast("Detection timed out")
            }
        }, 10_000)
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private fun isAccessibilityEnabled(): Boolean {
        val service = "$packageName/${EssentialButtonService::class.java.canonicalName}"
        val enabled = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return TextUtils.SimpleStringSplitter(':').apply { setString(enabled) }
            .any { it.equals(service, ignoreCase = true) }
    }

    private fun showAccessibilityDialog() {
        AlertDialog.Builder(this, R.style.NothingDialog)
            .setTitle("SETUP REQUIRED")
            .setMessage("Enable the 'Essential Key' accessibility service to allow button interception.\n\nPath: Accessibility → Downloaded Apps → Essential Key")
            .setPositiveButton("OPEN SETTINGS") { _, _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .setNegativeButton("LATER", null)
            .show()
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density + 0.5f).toInt()

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
