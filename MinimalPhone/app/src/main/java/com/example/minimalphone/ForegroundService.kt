package com.example.minimalphone

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat

class ForegroundService : Service() {

    private lateinit var usageStatsManager: UsageStatsManager
    private val handler = Handler(Looper.getMainLooper())

    // Receiver for updates (optional, if your activity sends broadcasts)
    private val topAppReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val topApp = intent?.getStringExtra("topApp") ?: "Unknown"
            Log.d("ForegroundService", "Top app broadcast received: $topApp")
        }
    }

    override fun onCreate() {
        super.onCreate()

        usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager

        // Register broadcast receiver safely for Android 13+
        val filter = IntentFilter("com.example.minimalphone.TOP_APP_UPDATE")
        // Fixed broadcast receiver registration for API levels below 33
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(topAppReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(topAppReceiver, filter, RECEIVER_EXPORTED)
        }

        Log.d("ForegroundService", "Broadcast receiver registered successfully.")
    }

    // Added permission check and prompt for PACKAGE_USAGE_STATS
    private fun checkUsageStatsPermission() {
        val appOps = getSystemService(APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        if (mode != android.app.AppOpsManager.MODE_ALLOWED) {
            val intent = Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            Toast.makeText(this, "Please grant Usage Access permission.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        checkUsageStatsPermission()

        Toast.makeText(this, "Foreground service started!", Toast.LENGTH_SHORT).show()

        // --- Create notification channel for the service ---
        val channel = NotificationChannel(
            "foreground_service_channel",
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        // --- Build persistent notification ---
        val notification = NotificationCompat.Builder(this, "foreground_service_channel")
            .setContentTitle("MinimalPhone Service Active")
            .setContentText("Monitoring foreground apps…")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        // --- Start the service in the foreground ---
        startForeground(1, notification)

        // --- Begin polling for top app every 2 seconds ---
        handler.post(object : Runnable {
            override fun run() {
                val topApp = getTopAppPackageName()
                Log.d("AppDetection", "Top app: $topApp")
                handler.postDelayed(this, 2000)
            }
        })

        return START_STICKY
    }

    // Function to get currently active (foreground) app
    private fun getTopAppPackageName(): String {
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 60000 // last 60 seconds
        val events = usageStatsManager.queryEvents(beginTime, endTime)
        val event = UsageEvents.Event()
        var lastApp = "Unknown"

        if (!events.hasNextEvent()) {
            Log.d("AppDetection", "No usage events found in the last 60 seconds")
            return lastApp
        }

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            Log.d("AppDetection", "Event: ${event.packageName}, Type: ${event.eventType}, Time: ${event.timeStamp}")
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastApp = event.packageName
            }
        }

        if (lastApp == "Unknown") {
            Log.d("AppDetection", "No foreground app detected in the last 60 seconds")
        }

        return lastApp
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        try {
            unregisterReceiver(topAppReceiver)
            Log.d("ForegroundService", "Broadcast receiver unregistered.")
        } catch (_: Exception) {
            Log.w("ForegroundService", "Receiver already unregistered or not found.")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
