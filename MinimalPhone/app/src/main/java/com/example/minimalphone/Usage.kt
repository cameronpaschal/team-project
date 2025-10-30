package com.example.minimalphone

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class Usage : AppCompatActivity() {
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var handler: Handler
    private lateinit var topAppText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usage)

        topAppText = findViewById(R.id.topAppText)
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        handler = Handler(Looper.getMainLooper())

        startPolling()
    }

    private fun startPolling() {
        handler.post(object : Runnable {
            override fun run() {
                val topPackage = getTopAppPackageName()
                topAppText.text = "Top App: $topPackage"
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun getTopAppPackageName(): String {
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 1000 // look at the last 60 seconds
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            beginTime,
            endTime
        )

        if (stats.isNullOrEmpty()) {
            return "No data (waiting for events...)"
        }

        val recent = stats.maxByOrNull { it.lastTimeUsed }
        return recent?.packageName ?: "Unknown"
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}