package com.example.minimalphone

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // Avoid repeatedly prompting the user each time onResume is called
    private var hasPromptedPermissionsThisSession = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "MainActivity created")

        // 🔍 Check permissions at launch
        checkAndShowPermissions()
    }

    override fun onResume() {
        super.onResume()
        // Re-check permissions when the user returns from system settings
        checkAndShowPermissions()
    }

    // 🔐 Check if Usage Stats permission is granted
    private fun hasUsageStatsPermission(): Boolean {
        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()

        return try {
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                time - 1000 * 60,
                time
            )
            val hasPermission = !stats.isNullOrEmpty()
            Log.d(TAG, "Usage Stats Permission: $hasPermission (found ${stats?.size} stats)")
            hasPermission
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Usage Stats permission", e)
            false
        }
    }

    // 🧾 Show a summary of permissions
    private fun checkAndShowPermissions() {
        Log.d(TAG, "━━━━━ PERMISSION CHECK ━━━━━")

        val hasUsageStats = hasUsageStatsPermission()
        val hasOverlay = Settings.canDrawOverlays(this)
        // DND check removed - we no longer gate behavior on Do Not Disturb permission

        // 🚨 If ANY permissions are missing, prompt user to fix them via a dialog (once per session)
        if (!hasUsageStats || !hasOverlay) {
            if (!hasPromptedPermissionsThisSession) {
                hasPromptedPermissionsThisSession = true
                showPermissionsDialog(hasUsageStats, hasOverlay)
            } else {
                Log.d(TAG, "Permissions missing but already prompted this session")
            }
            return
        }

        // Permissions granted, do nothing. FocusModeService is started from FocusModeActivity only.
    }

    private fun showPermissionsDialog(hasUsage: Boolean, hasOverlay: Boolean) {
        val missing = mutableListOf<String>()
        if (!hasUsage) missing.add("Usage Access")
        if (!hasOverlay) missing.add("Display over apps")

        val builder = AlertDialog.Builder(this)
            .setTitle("Permissions required")
            .setMessage("The app needs the following permissions: ${missing.joinToString(", ")}\nOpen settings to grant them?")
            .setCancelable(true)
            .setPositiveButton("Open Settings") { _, _ ->
                // Open the most relevant settings screen. We don't auto-open all — user can navigate.
                // Prefer opening Usage Access first if missing, then Overlay.
                when {
                    !hasUsage -> startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    !hasOverlay -> startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:$packageName".toUri()))
                    else -> startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        runOnUiThread {
            builder.show()
        }
    }
}
