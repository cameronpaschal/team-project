package com.example.minimalphone

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button

class BlockedAppsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked_apps)

        val recyclerView = findViewById<RecyclerView>(R.id.blockedAppsRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Use queryIntentActivities to get launchable apps
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolvedApps = packageManager.queryIntentActivities(intent, 0)
        val installedApps = resolvedApps.map { it.activityInfo.applicationInfo }

        val blocked = BlockedAppsManager.getBlockedApps(this)

        val items = installedApps.map {
            AppInfo(
                name = it.loadLabel(packageManager).toString(),
                packageName = it.packageName,
                isBlocked = blocked.contains(it.packageName)
            )
        }

        recyclerView.adapter = BlockedAppsAdapter(items) { updated ->
            val newBlocked = updated.filter { it.isBlocked }.map { it.packageName }.toSet()
            BlockedAppsManager.saveBlockedApps(this, newBlocked)
        }

        val backButton = findViewById<Button>(R.id.backToMainButton)
        backButton.setOnClickListener {
            finish()
        }
    }
}
