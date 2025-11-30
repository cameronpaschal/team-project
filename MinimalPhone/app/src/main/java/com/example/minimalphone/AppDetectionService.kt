package com.example.minimalphone

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import android.content.Intent
import android.app.usage.UsageStatsManager
import android.content.Context
import android.app.usage.UsageStats
import java.util.TreeMap

/**
 * AppDetectionService
 *
 * This service listens for app/window state changes using Android's AccessibilityService.
 * It detects when the user switches between apps and broadcasts the active app’s package name.
 */

class AppDetectionService : AccessibilityService() {

    private var lastBlockedPackage: String? = null
    private var lastBlockTime: Long = 0L
    private val BLOCK_COOLDOWN = 500L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            if (!packageName.isNullOrEmpty()) {
                Log.d("AppDetection", "Current app: $packageName")

                // Broadcast the detected app's package name (targeted to our app only)
                val intent = Intent("com.example.minimalphone.TOP_APP_UPDATE").apply {
                    putExtra("topApp", packageName)
                    setPackage(this@AppDetectionService.packageName)
                }
                sendBroadcast(intent)

                // Fix: Pass context to isBlocked
                if (packageName != this.packageName && BlockedAppsManager.isBlocked(this, packageName)) {
                    val now = System.currentTimeMillis()
                    if (packageName != lastBlockedPackage || now - lastBlockTime > BLOCK_COOLDOWN) {
                        lastBlockedPackage = packageName
                        lastBlockTime = now
                        try {
                            Log.d("AppDetection", "Blocked app detected -> launching BlockActivity for $packageName (fallback)")
                            val blockIntent = Intent(this, BlockActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                putExtra("blocked_package", packageName)
                            }
                            startActivity(blockIntent)
                        } catch (e: Exception) {
                            Log.w("AppDetection", "Failed to launch BlockActivity from AccessibilityService", e)
                        }
                    }
                }

                // If all permissions are present, ensure FocusModeService is running
                try {
                    // AccessibilityService is active — start FocusModeService so detection can run.
                    // FocusModeService will enforce any further gating as needed.
                    Log.d("AppDetection", "Accessibility event received — ensuring FocusModeService is running")
                    if (FocusModeStateManager.isFocusModeOn(this)) {
                        val svcIntent = Intent(this, FocusModeService::class.java).apply {
                            action = "com.example.minimalphone.action.START_MONITOR"
                            putExtra("initial_top_app", packageName)
                        }
                        startForegroundService(svcIntent)
                    }
                } catch (e: Exception) {
                    Log.w("AppDetection", "Failed to start FocusModeService", e)
                }

                return
            }
        }

        // Fallback to UsageStatsManager if AccessibilityService fails
        val fallbackPackageName = getTopAppUsingUsageStats()
        if (!fallbackPackageName.isNullOrEmpty()) {
            Log.d("AppDetection", "Fallback to UsageStatsManager: $fallbackPackageName")

            val intent = Intent("com.example.minimalphone.TOP_APP_UPDATE").apply {
                putExtra("topApp", fallbackPackageName)
                setPackage(this@AppDetectionService.packageName)
            }
            sendBroadcast(intent)
        } else {
            Log.d("AppDetection", "Unable to detect top app")
        }
    }

    private fun getTopAppUsingUsageStats(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            time - 1000 * 60,
            time
        )

        if (stats.isNullOrEmpty()) {
            Log.d("AppDetection", "No usage events found in the last 60 seconds")
            return null
        }

        val sortedMap = TreeMap<Long, UsageStats>()
        for (usageStats in stats) {
            sortedMap[usageStats.lastTimeUsed] = usageStats
        }

        return sortedMap[sortedMap.lastKey()]?.packageName
    }

    override fun onInterrupt() {
        // Required method, leave empty unless you need cleanup logic
    }
}
