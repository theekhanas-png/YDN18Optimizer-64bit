package com.yodhan18.ydn18optimizer

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class PermissionSetupActivity : AppCompatActivity() {

    data class PermItem(
        val title: String,
        val desc: String,
        val isCritical: Boolean,           // critical = must grant before entering
        val action: () -> Unit,
        val isGranted: () -> Boolean
    )

    private lateinit var container: LinearLayout
    private lateinit var btnContinue: Button
    private lateinit var tvStatus: TextView
    private val permItems = mutableListOf<PermItem>()

    private val notifLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refreshUI() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_setup)

        container = findViewById(R.id.permissionsContainer)
        btnContinue = findViewById(R.id.btnContinue)
        tvStatus = findViewById(R.id.tvPermStatus)

        buildPermList()
        renderPermissions()

        // FIX 1: Only allow continue when ALL critical permissions are granted
        btnContinue.setOnClickListener {
            val allGranted = permItems
                .filter { it.isCritical }
                .all { it.isGranted() }

            if (!allGranted) {
                // Flash the status message red
                tvStatus.text = "Please grant ALL required permissions first!"
                tvStatus.setTextColor(Color.parseColor("#FF1744"))
                // Shake button to indicate blocked
                btnContinue.animate()
                    .translationX(16f).setDuration(60).withEndAction {
                        btnContinue.animate()
                            .translationX(-16f).setDuration(60).withEndAction {
                                btnContinue.animate()
                                    .translationX(0f).setDuration(60).start()
                            }.start()
                    }.start()
                Toast.makeText(this,
                    "All permissions are required for the app to work properly!",
                    Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // All critical permissions granted — enter app
            getSharedPreferences("ydn18_prefs", MODE_PRIVATE)
                .edit().putBoolean("first_launch", false).apply()
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    private fun buildPermList() {
        permItems.clear()

        // Notifications — critical
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permItems.add(PermItem(
                title = "Notifications",
                desc = "Required: Show optimization alerts and service status",
                isCritical = true,
                action = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                isGranted = { PermissionHelper.hasNotificationPermission(this) }
            ))
        }

        // All Files Access — critical
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permItems.add(PermItem(
                title = "All Files Access",
                desc = "Required: Clean app caches and temporary storage files",
                isCritical = true,
                action = { PermissionHelper.openManageStorageSettings(this) },
                isGranted = { PermissionHelper.hasManageStoragePermission() }
            ))
        }

        // Usage Stats — critical
        permItems.add(PermItem(
            title = "Usage Access",
            desc = "Required: Detect and manage running apps for optimization",
            isCritical = true,
            action = { PermissionHelper.openUsageAccessSettings(this) },
            isGranted = { PermissionHelper.hasUsageStatsPermission(this) }
        ))

        // Battery optimization — critical
        permItems.add(PermItem(
            title = "Battery Optimization",
            desc = "Required: Run optimizer continuously in the background",
            isCritical = true,
            action = { PermissionHelper.openBatteryOptimizationSettings(this) },
            isGranted = { PermissionHelper.hasBatteryOptimizationExemption(this) }
        ))

        // Write Settings — critical
        permItems.add(PermItem(
            title = "Modify System Settings",
            desc = "Required: Apply performance tweaks and display settings",
            isCritical = true,
            action = { PermissionHelper.openWriteSettingsPage(this) },
            isGranted = { PermissionHelper.hasWriteSettingsPermission(this) }
        ))

        // Accessibility — critical (FIX 3: now properly detected)
        permItems.add(PermItem(
            title = "Accessibility Service",
            desc = "Required: Enable 'YDN18 Optimizer' in Accessibility Settings",
            isCritical = true,
            action = { PermissionHelper.openAccessibilitySettings(this) },
            isGranted = { PermissionHelper.hasAccessibilityPermission(this) }
        ))
    }

    private fun renderPermissions() {
        container.removeAllViews()
        val inflater = LayoutInflater.from(this)
        var allGranted = true

        for (item in permItems) {
            val granted = item.isGranted()
            if (item.isCritical && !granted) allGranted = false

            val view = inflater.inflate(R.layout.item_permission, container, false)
            val tvTitle = view.findViewById<TextView>(R.id.tvPermTitle)
            val tvDesc = view.findViewById<TextView>(R.id.tvPermDesc)
            val btn = view.findViewById<Button>(R.id.btnGrant)

            tvTitle.text = if (item.isCritical) "* ${item.title}" else item.title
            tvDesc.text = item.desc

            if (granted) {
                btn.text = "Granted"
                btn.isEnabled = false
                btn.alpha = 0.5f
                tvTitle.setTextColor(Color.parseColor("#00E676")) // green when granted
            } else {
                btn.text = "GRANT"
                btn.isEnabled = true
                btn.alpha = 1f
                tvTitle.setTextColor(Color.parseColor("#FF6D00")) // orange when missing
                btn.setOnClickListener {
                    item.action()
                }
            }
            container.addView(view)
        }

        // Update status text and button
        if (allGranted) {
            tvStatus.text = "All permissions granted! Tap Continue."
            tvStatus.setTextColor(Color.parseColor("#00E676"))
            btnContinue.alpha = 1f
            btnContinue.text = "CONTINUE TO APP"
        } else {
            val remaining = permItems.count { it.isCritical && !it.isGranted() }
            tvStatus.text = "$remaining permission(s) still required"
            tvStatus.setTextColor(Color.parseColor("#FF6D00"))
            btnContinue.alpha = 0.4f
            btnContinue.text = "GRANT ALL PERMISSIONS FIRST"
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check every time user comes back from settings
        renderPermissions()
    }
}
