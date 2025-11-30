package com.example.minimalphone

import android.content.Context
import androidx.core.content.edit

object BlockedAppsManager {
    private const val PREF_NAME = "blocked_apps_prefs"
    private const val KEY_BLOCKED_SET = "blocked_packages"

    private val defaultBlocked = setOf(
        "com.instagram.android",
        "com.google.android.youtube",
        "com.snapchat.android",
        "com.facebook.katana",
        "com.twitter.android"
    )

    // Remove static context reference
    // Always pass context as parameter
    fun getBlockedApps(context: Context): MutableSet<String> {
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_BLOCKED_SET, defaultBlocked)!!.toMutableSet()
    }

    fun saveBlockedApps(context: Context, blocked: Set<String>) {
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putStringSet(KEY_BLOCKED_SET, blocked) }
    }

    fun isBlocked(context: Context, packageName: String): Boolean {
        return FocusModeServiceIsRunningChecker.isServiceRunning(context) && getBlockedApps(context).contains(packageName)
    }

    fun getAppDisplayName(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
}
