package com.example.minimalphone

import android.app.AppOpsManager
import android.app.NotificationManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "MainActivity started")

        // Show permissions + request missing ones
        checkAndShowPermissions()
        requestMissingPermissions()
    }

    // --------------------------------------------------------------------
    // ✔ Reliable Usage Stats Permission Check (real AppOps API)
    // --------------------------------------------------------------------
    private fun hasUsageStatsPermission(): Boolean {
        val ops = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = ops.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        val granted = (mode == AppOpsManager.MODE_ALLOWED)

        Log.d(TAG, "Usage Stats Permission: $granted")
        return granted
    }

    // --------------------------------------------------------------------
    // ✔ Show a summary of permissions
    // --------------------------------------------------------------------
    private fun checkAndShowPermissions() {
        val hasUsage = hasUsageStatsPermission()
        val hasOverlay = Settings.canDrawOverlays(this)
//        val dndGranted = isDndPermissionGranted()

        val message = """
            Permissions:
            ${if (hasUsage) "✅" else "❌"} Usage Stats
            ${if (hasOverlay) "✅" else "❌"} Overlay Permission
        
        """.trimIndent()

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // --------------------------------------------------------------------
    // ✔ Check missing permissions & navigate user to settings
    // --------------------------------------------------------------------
    private fun requestMissingPermissions() {
        Log.d(TAG, "Checking + requesting missing permissions...")

        if (!hasUsageStatsPermission()) {
            Toast.makeText(this, "Usage Access Required!", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            return
        }

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Overlay Permission Required!", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            return
        }

//        if (!isDndPermissionGranted()) {
//            Toast.makeText(this, "DND Permission Recommended", Toast.LENGTH_SHORT).show()
//            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
//        }
    }

    // --------------------------------------------------------------------
    // ✔ Check DND Permission
    // --------------------------------------------------------------------
//    private fun isDndPermissionGranted(): Boolean {
//        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        return nm.isNotificationPolicyAccessGranted
//    }

    // --------------------------------------------------------------------
    // ✔ Enable or Disable DND
    // --------------------------------------------------------------------
//    private fun setDoNotDisturb(enabled: Boolean) {
//        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        if (!nm.isNotificationPolicyAccessGranted) {
//            Log.w(TAG, "Cannot change DND — permission not granted")
//            return
//        }
//
//        nm.setInterruptionFilter(
//            if (enabled) NotificationManager.INTERRUPTION_FILTER_PRIORITY
//            else NotificationManager.INTERRUPTION_FILTER_ALL
//        )
//
//        Log.d(TAG, "DND ${if (enabled) "enabled" else "disabled"}")
//    }

    // --------------------------------------------------------------------
    // ✔ Start Focus Mode (Foreground Service)
    // --------------------------------------------------------------------
    private fun startFocusMode() {
        Log.d(TAG, "Starting Focus Mode")

        if (!allPermissionsGood()) {
            Toast.makeText(this, "Missing permissions!", Toast.LENGTH_SHORT).show()
            return
        }

//        setDoNotDisturb(true)

        val intent = Intent(this, ForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(intent)
        else
            startService(intent)

        Toast.makeText(this, "Focus Mode Active!", Toast.LENGTH_LONG).show()
    }

    // --------------------------------------------------------------------
    // ✔ Stop Focus Mode
    // --------------------------------------------------------------------
    private fun stopFocusMode() {
        Log.d(TAG, "Stopping Focus Mode")

//        setDoNotDisturb(false)
        stopService(Intent(this, ForegroundService::class.java))

        Toast.makeText(this, "Focus Mode Stopped", Toast.LENGTH_SHORT).show()
    }

    // --------------------------------------------------------------------
    // ✔ Helper: Check all needed permissions at once
    // --------------------------------------------------------------------
    private fun allPermissionsGood(): Boolean {
        return hasUsageStatsPermission() &&
                Settings.canDrawOverlays(this)
    }
}
