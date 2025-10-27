package com.example.minimalphone

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat

class ForegroundService : Service() {

    // Called when the service is started
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "Foreground service started!", Toast.LENGTH_SHORT).show()

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "foreground_service_channel",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Build persistent notification
        val notification = NotificationCompat.Builder(this, "foreground_service_channel")
            .setContentTitle("Foreground Service Running")
            .setContentText("The service is active and running in the background.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        // Start service in foreground
        startForeground(1, notification)

        // Keep it alive with a periodic log message every 5 seconds
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                Log.d("ForegroundService", "Service still running at ${System.currentTimeMillis()}")
                handler.postDelayed(this, 5000)
            }
        }
        handler.post(runnable)

        // Return flag so Android restarts it if killed
        return START_STICKY
    }

    // We’re not using binding here
    override fun onBind(intent: Intent?): IBinder? = null
}
