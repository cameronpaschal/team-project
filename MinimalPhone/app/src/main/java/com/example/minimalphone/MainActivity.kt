package com.example.minimalphone

import android.app.NotificationManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "MainActivity created")

        // 🔍 Check permissions at launch
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
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val hasDND = notificationManager.isNotificationPolicyAccessGranted

        val message = """
            Permissions Status:
            ${if (hasUsageStats) "✅" else "❌"} Usage Stats
            ${if (hasOverlay) "✅" else "❌"} Display Over Apps
            ${if (hasDND) "✅" else "❌"} Do Not Disturb
        """.trimIndent()

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        if (!hasUsageStats || !hasOverlay || !hasDND) {
            Toast.makeText(this, "Some permissions missing! Check logs.", Toast.LENGTH_SHORT).show()
        }
    }

    // 🛡️ Check permissions and redirect if missing
    private fun checkPermissions(): Boolean {
        Log.d(TAG, "Checking permissions...")

        // 1️⃣ Usage Stats
        if (!hasUsageStatsPermission()) {
            Log.e(TAG, "❌ Missing Usage Stats Permission")
            Toast.makeText(
                this,
                "⚠️ Usage Access is REQUIRED!\nGo to Settings now.",
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
            return false
        }

        // 2️⃣ Overlay
        if (!Settings.canDrawOverlays(this)) {
            Log.e(TAG, "❌ Missing Overlay Permission")
            Toast.makeText(
                this,
                "⚠️ Display over apps is REQUIRED!",
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:$packageName")
            )
            startActivity(intent)
            return false
        }

        // 3️⃣ Do Not Disturb
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            Log.w(TAG, "⚠️ Missing DND Permission (optional)")
            Toast.makeText(
                this,
                "⚠️ Do Not Disturb permission recommended",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
            return false
        }

        Log.d(TAG, "✅ All permissions granted!")
        return true
    }

    // 🔕 DND toggle
    private fun enableDoNotDisturb(enable: Boolean) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            val filter = if (enable)
                NotificationManager.INTERRUPTION_FILTER_PRIORITY
            else
                NotificationManager.INTERRUPTION_FILTER_ALL
            notificationManager.setInterruptionFilter(filter)
            Log.d(TAG, "DND ${if (enable) "enabled" else "disabled"}")
        }
    }

    // 🧘 Start focus mode (foreground service)
    private fun startFocusMode() {
        Log.d(TAG, "━━━━━ START FOCUS MODE ━━━━━")

        if (checkPermissions()) {
            enableDoNotDisturb(true)

            val serviceIntent = Intent(this, ForegroundService::class.java)
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
        } else {
            Log.e(TAG, "❌ Cannot start - missing permissions")
        }
    }

    // 💤 Stop focus mode
    private fun stopFocusMode() {
        Log.d(TAG, "━━━━━ STOP FOCUS MODE ━━━━━")

        enableDoNotDisturb(false)
        stopService(Intent(this, ForegroundService::class.java))
        Toast.makeText(this, "Focus Mode Stopped", Toast.LENGTH_SHORT).show()
    }
}
