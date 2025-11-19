package com.example.minimalphone

import android.app.NotificationManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

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
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
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
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val hasDND = notificationManager.isNotificationPolicyAccessGranted

        val message = """
            Permissions Status:
            ${if (hasUsageStats) "✅" else "❌"} Usage Stats
            ${if (hasOverlay) "✅" else "❌"} Display Over Apps
            ${if (hasDND) "✅" else "❌"} Do Not Disturb
        """.trimIndent()

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // 🚨 If ANY permissions are missing, prompt user to fix them via a dialog (once per session)
        if (!hasUsageStats || !hasOverlay || !hasDND) {
            if (!hasPromptedPermissionsThisSession) {
                hasPromptedPermissionsThisSession = true
                showPermissionsDialog(hasUsageStats, hasOverlay, hasDND)
            } else {
                Log.d(TAG, "Permissions missing but already prompted this session")
            }
            return
        }

        // 🎉 All permissions granted → Start FocusModeService
        Log.d(TAG, "All permissions granted → starting FocusModeService")
        startFocusMode()
    }

    private fun showPermissionsDialog(hasUsage: Boolean, hasOverlay: Boolean, hasDnd: Boolean) {
        val missing = mutableListOf<String>()
        if (!hasUsage) missing.add("Usage Access")
        if (!hasOverlay) missing.add("Display over apps")
        if (!hasDnd) missing.add("Do Not Disturb")

        val builder = AlertDialog.Builder(this)
            .setTitle("Permissions required")
            .setMessage("The app needs the following permissions: ${missing.joinToString(", ")}.\nOpen settings to grant them?")
            .setCancelable(true)
            .setPositiveButton("Open Settings") { _, _ ->
                // Open the most relevant settings screen. We don't auto-open all — user can navigate.
                // Prefer opening Usage Access first if missing, then Overlay, then DND.
                when {
                    !hasUsage -> startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    !hasOverlay -> startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$packageName")))
                    !hasDnd -> startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                    else -> startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        runOnUiThread {
            builder.show()
        }
    }

    // 🧘 Start focus mode (foreground detection)
    private fun startFocusMode() {
        Log.d(TAG, "━━━━━ START FOCUS MODE ━━━━━")

        // Double-check permissions before launching service
        if (!hasUsageStatsPermission()) {
            Log.e(TAG, "❌ Missing Usage Stats Permission")
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            return
        }

        if (!Settings.canDrawOverlays(this)) {
            Log.e(TAG, "❌ Missing Overlay Permission")
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:$packageName")
            )
            startActivity(intent)
            return
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            Log.e(TAG, "❌ Missing DND Permission")
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            return
        }

        // 🔥 Start the FocusModeService (this is where detection + blocking runs)
        val serviceIntent = Intent(this, FocusModeService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        Log.d(TAG, "✅ Focus Mode Started")
        Toast.makeText(
            this,
            "🔴 FOCUS MODE ACTIVE!\nTry opening Instagram or YouTube",
            Toast.LENGTH_LONG
        ).show()
    }
}
