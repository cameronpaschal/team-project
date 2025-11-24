package com.example.appblocker.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import com.example.appblocker.data.InstalledApp

object AppLoader {
    fun loadLaunchableApps(context: Context, blocked: Set<String>): List<InstalledApp> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)

        val apps = resolveInfos.mapNotNull { ri ->
            val ai = ri.activityInfo?.applicationInfo ?: return@mapNotNull null
            val label = ai.loadLabel(pm)?.toString() ?: ai.packageName
            InstalledApp(
                label = label,
                packageName = ai.packageName,
                icon = ai.loadIcon(pm),
                isSystem = (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                isBlocked = blocked.contains(ai.packageName)
            )
        }
        return apps.distinctBy { it.packageName }
            .sortedWith(compareBy<InstalledApp> { it.isSystem }.thenBy { it.label.lowercase() })
    }
}