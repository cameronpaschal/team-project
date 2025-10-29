package com.example.myapplication

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.appsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get installed apps
        val pm: PackageManager = packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .map { AppInfo(it.loadLabel(pm).toString(), it.packageName) }

        recyclerView.adapter = AppsAdapter(this, installedApps)
    }
}

