// kotlin
package com.example.minimalphone

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class FocusModeService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var lastBlockedPackage: String? = null
    private var lastBlockTime: Long = 0L

    companion object {
        private const val CHECK_INTERVAL = 100L
        private const val BLOCK_COOLDOWN = 500L
        const val ACTION_BLOCK_DISMISSED = "com.example.minimalphone.action.BLOCK_DISMISSED"
    }

    private val monitorRunnable = object : Runnable {
        override fun run() {
            checkForegroundApp()
            handler.postDelayed(this, CHECK_INTERVAL)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_BLOCK_DISMISSED) {
            lastBlockedPackage = null
        } else {
            handler.removeCallbacks(monitorRunnable)
            handler.post(monitorRunnable)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(monitorRunnable)
        lastBlockedPackage = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun checkForegroundApp() {
        val foreground = getForegroundApp() ?: return
        if (foreground == packageName) return
        if (!BlockedAppsManager.isBlocked(foreground)) return

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
            val events = usageStatsManager.queryEvents(end - 1000, end)
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
        } catch (se: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun triggerBlockActivity(blockedPackage: String) {
        val intent = Intent(this, BlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            putExtra("blocked_package", blockedPackage)
        }
        startActivity(intent)
    }
}
