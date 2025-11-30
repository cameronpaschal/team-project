package com.example.minimalphone

import android.app.ActivityManager
import android.content.Context

object FocusModeServiceIsRunningChecker {
    fun isServiceRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        return manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == FocusModeService::class.java.name }
    }
}

