package com.example.minimalphone

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class FocusModeService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var lastBlockedPackage: String? = null
    private var lastBlockTime: Long = 0L
    private var receiverRegistered = false
    private var accessibilityManager: AccessibilityManager? = null
    private val accessibilityStateChangeListener = AccessibilityManager.AccessibilityStateChangeListener { _ ->
        // Re-evaluate receiver registration whenever accessibility state changes
        Log.d(TAG, "Accessibility state changed -> updating receiver registration")
        updateReceiverRegistration()
    }

    companion object {
        private const val CHECK_INTERVAL = 1000L // 1 second
        private const val BLOCK_COOLDOWN = 500L
        const val ACTION_BLOCK_DISMISSED = "com.example.minimalphone.action.BLOCK_DISMISSED"
        const val ACTION_START_MONITOR = "com.example.minimalphone.action.START_MONITOR"
        private const val TOP_APP_ACTION = "com.example.minimalphone.TOP_APP_UPDATE"
        private const val TAG = "FocusModeService"

        // Foreground notification constants
        private const val NOTIF_CHANNEL_ID = "focus_mode_channel"
        private const val NOTIF_ID = 1001
    }

    // Receiver to prefer AccessibilityService-driven top-app updates
    private val topAppReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val pkg = intent?.getStringExtra("topApp")
            Log.d(TAG, "Received TOP_APP_UPDATE -> $pkg (receiverRegistered=$receiverRegistered)")
            handleTopAppUpdate(pkg)
        }
    }

    private val monitorRunnable = object : Runnable {
        override fun run() {
            // If Accessibility is enabled, prefer broadcasts; otherwise poll via UsageStats
            val accessibility = isAccessibilityServiceEnabled()
            if (!accessibility) {
                Log.d(TAG, "Polling fallback: checking foreground app via UsageStats")
                checkForegroundApp()
            } else {
                Log.v(TAG, "Accessibility enabled — relying on broadcast updates")
            }
            handler.postDelayed(this, CHECK_INTERVAL)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate() called")
        // Initialize AccessibilityManager and listen for state changes so we can register/unregister the
        // TOP_APP_UPDATE receiver dynamically. This avoids polling when Accessibility is available.
        try {
            accessibilityManager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
            accessibilityManager?.addAccessibilityStateChangeListener(accessibilityStateChangeListener)
        } catch (e: Exception) {
            Log.w(TAG, "Could not initialize AccessibilityManager", e)
            accessibilityManager = null
        }

        // Ensure receiver is registered only when needed
        updateReceiverRegistration()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand() called with action=${intent?.action}")

        // Only run if Focus Mode is ON
        if (!FocusModeStateManager.isFocusModeOn(this)) {
            Log.d(TAG, "Focus Mode is OFF, stopping service.")
            stopSelf()
            return START_NOT_STICKY
        }

        // Ensure we enter foreground quickly when started via startForegroundService
        ensureForegroundNotification()

        // ACTION_BLOCK_DISMISSED should not trigger permission gating — it just resets the last blocked
        if (intent?.action == ACTION_BLOCK_DISMISSED) {
            Log.d(TAG, "ACTION_BLOCK_DISMISSED received — resetting lastBlockedPackage")
            lastBlockedPackage = null
            return START_STICKY
        }

        // If started with an initial package (fast-start from Accessibility), process it immediately
        if (intent?.action == ACTION_START_MONITOR) {
            val initial = intent.getStringExtra("initial_top_app")
            Log.d(TAG, "ACTION_START_MONITOR received with initial_top_app=$initial")
            // If Accessibility is enabled or usage stats are present, handle immediately
            if (!initial.isNullOrBlank()) {
                handleTopAppUpdate(initial)
            }
            // continue to gate/monitor below
        }

        // Gate the monitoring: ensure required permissions are still present
        if (!allRequiredPermissionsPresent()) {
            Log.e(TAG, "Missing required permissions — stopping service")
            stopSelf()
            return START_NOT_STICKY
        }

        // Start monitoring (either via Accessibility broadcasts or polling fallback)
        handler.removeCallbacks(monitorRunnable)
        handler.post(monitorRunnable)
        Log.d(TAG, "Monitor runnable posted (polling enabled=${!isAccessibilityServiceEnabled()})")

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        FocusModeStateManager.setFocusModeOn(this, false)
        handler.removeCallbacks(monitorRunnable)
        lastBlockedPackage = null
        // Clean up receiver and accessibility listener
        if (receiverRegistered) {
            try {
                unregisterReceiver(topAppReceiver)
                Log.d(TAG, "topAppReceiver unregistered")
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering topAppReceiver", e)
            }
            receiverRegistered = false
        }

        try {
            accessibilityManager?.removeAccessibilityStateChangeListener(accessibilityStateChangeListener)
        } catch (e: Exception) {
            Log.w(TAG, "Error removing accessibility listener", e)
        }

        // Ensure we leave foreground state when destroyed
        try {
            stopForeground(true)
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping foreground", e)
        }
    }

    // Manage registration/unregistration of the TOP_APP_UPDATE receiver according to Accessibility availability
    private fun updateReceiverRegistration() {
        val shouldBeRegistered = isAccessibilityServiceEnabled()
        if (shouldBeRegistered && !receiverRegistered) {
            try {
                val filter = IntentFilter(TOP_APP_ACTION)
                val receiverFlag = if (android.os.Build.VERSION.SDK_INT >= 33) {
                    ContextCompat.RECEIVER_EXPORTED
                } else {
                    ContextCompat.RECEIVER_NOT_EXPORTED
                }
                ContextCompat.registerReceiver(this, topAppReceiver, filter, receiverFlag)
                receiverRegistered = true
                Log.d(TAG, "topAppReceiver registered (dynamic)")
            } catch (e: Exception) {
                Log.w(TAG, "Could not register topAppReceiver dynamically", e)
                receiverRegistered = false
            }
        } else if (!shouldBeRegistered && receiverRegistered) {
            try {
                unregisterReceiver(topAppReceiver)
                receiverRegistered = false
                Log.d(TAG, "topAppReceiver unregistered (dynamic)")
            } catch (e: Exception) {
                Log.w(TAG, "Could not unregister topAppReceiver dynamically", e)
            }
        }
        Log.d(TAG, "updateReceiverRegistration -> shouldBeRegistered=$shouldBeRegistered receiverRegistered=$receiverRegistered")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // New: handle top-app updates coming from Accessibility (fast path)
    private fun handleTopAppUpdate(topPkg: String?) {
        if (topPkg.isNullOrBlank()) {
            Log.d(TAG, "handleTopAppUpdate: empty package — ignoring")
            return
        }

        Log.d(TAG, "handleTopAppUpdate -> $topPkg")

        // Don't block our own package
        if (topPkg == this.packageName) return

        // Only block if in blocked list
        if (!BlockedAppsManager.isBlocked(this, topPkg)) return

        val now = System.currentTimeMillis()
        if (topPkg != lastBlockedPackage || now - lastBlockTime > BLOCK_COOLDOWN) {
            lastBlockedPackage = topPkg
            lastBlockTime = now
            triggerBlockActivity(topPkg)
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        return try {
            val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
            val enabled = am.getEnabledAccessibilityServiceList(0).any { info ->
                // Match by package name (our AppDetectionService runs in this package)
                info.resolveInfo.serviceInfo.packageName == packageName
            }
            Log.d(TAG, "Accessibility enabled: $enabled")
            enabled
        } catch (e: Exception) {
            Log.w(TAG, "Error checking Accessibility state", e)
            false
        }
    }

    // Existing polling fallback — robustified with try/catch
    private fun checkForegroundApp() {
        val foreground = getForegroundApp() ?: return

        Log.d("TopApp", "Foreground app: $foreground")

        if (foreground == packageName) return
        if (!BlockedAppsManager.isBlocked(this, foreground)) return

        val now = System.currentTimeMillis()
        if (foreground != lastBlockedPackage || now - lastBlockTime > BLOCK_COOLDOWN) {
            lastBlockedPackage = foreground
            lastBlockTime = now
            triggerBlockActivity(foreground)
        }
    }

    private fun getForegroundApp(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null

        return try {
            val end = System.currentTimeMillis()
            val events = usageStatsManager.queryEvents(end - 2000, end)
            val ev = UsageEvents.Event()

            var mostRecentPackage: String? = null
            var mostRecentTime = 0L

            while (events.hasNextEvent()) {
                events.getNextEvent(ev)
                if (ev.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND && ev.timeStamp > mostRecentTime) {
                    mostRecentTime = ev.timeStamp
                    mostRecentPackage = ev.packageName
                }
            }

            mostRecentPackage
        } catch (e: Exception) {
            Log.e("TopApp", "Error reading foreground app", e)
            null
        }
    }

    private fun triggerBlockActivity(blockedPackage: String) {
        Log.d(TAG, "Triggering BlockActivity for $blockedPackage")
        val intent = Intent(this, BlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            putExtra("blocked_package", blockedPackage)
        }
        startActivity(intent)
    }

    // New: permission checks required to run detection
    private fun hasUsageStatsPermission(): Boolean {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return false
        return try {
            val time = System.currentTimeMillis()
            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 60, time)
            !stats.isNullOrEmpty()
        } catch (e: Exception) {
            Log.w(TAG, "Error checking usage stats permission", e)
            false
        }
    }

    private fun hasOverlayPermission(): Boolean {
        return try {
            Settings.canDrawOverlays(this)
        } catch (e: Exception) {
            Log.w(TAG, "Error checking overlay permission", e)
            false
        }
    }

    private fun allRequiredPermissionsPresent(): Boolean {
        val usage = hasUsageStatsPermission()
        val overlay = hasOverlayPermission()
        Log.d(TAG, "Permissions -> Usage:$usage Overlay:$overlay")
        // Relax gate: start service if we have UsageStats OR Accessibility is enabled.
        // Accessibility provides fast detection and doesn't need UsageStats.
        val accessibility = try { isAccessibilityServiceEnabled() } catch (_: Exception) { false }
        Log.d(TAG, "Accessibility available: $accessibility")
        val canStart = usage || accessibility
        Log.d(TAG, "Service start allowed: $canStart (usage=$usage, accessibility=$accessibility)")
        return canStart
    }

    // Creates notification channel (if needed) and calls startForeground with a persistent low-importance notification.
    private fun ensureForegroundNotification() {
        try {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                NOTIF_CHANNEL_ID,
                "Focus Mode",
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(channel)

            val notification: Notification = NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
                .setContentTitle("MinimalPhone — Focus Mode")
                .setContentText("Monitoring foreground apps")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .build()

            startForeground(NOTIF_ID, notification)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to create/start foreground notification", e)
        }
    }
}
