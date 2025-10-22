package com.example.minimalphone

import android.app.Service
import android.content.Intent
import android.os.IBinder

// This is the foreground service that will run in the background, right now it is just blank and does nothing

class ForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}