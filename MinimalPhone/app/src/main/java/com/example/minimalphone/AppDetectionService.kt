package com.example.minimalphone
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log

/**
 * AppDetectionService
 *
 * This service listens for app/window state changes using Android's AccessibilityService.
 * It detects when the user switches between apps and broadcasts the active app’s package name.
 *
 * Goal:
 *  - Detect app switches with <200ms latency (faster than UsageStatsManager)
 *  - Minimal performance overhead (lightweight + efficient)
 */


class AppDetectionService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            if (!packageName.isNullOrEmpty()) {
                Log.d("AppDetection", "Current app: $packageName")
            }
        }
    }

    override fun onInterrupt() {
        // Required method, leave empty unless you need cleanup logic
    }
}
