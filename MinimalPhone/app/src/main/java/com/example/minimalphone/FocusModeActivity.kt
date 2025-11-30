package com.example.minimalphone

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FocusModeActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var blockedAppsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_focus_mode)

        statusText = findViewById(R.id.statusText)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        blockedAppsButton = findViewById(R.id.blockedAppsButton)

        updateStatus()

        startButton.setOnClickListener {
            FocusModeStateManager.setFocusModeOn(this, true)
            startFocusMode()
            updateStatus()
        }
        stopButton.setOnClickListener {
            FocusModeStateManager.setFocusModeOn(this, false)
            stopService(Intent(this, FocusModeService::class.java))
            updateStatus()
        }

        blockedAppsButton.setOnClickListener {
            startActivity(Intent(this, BlockedAppsActivity::class.java))
        }
    }

    private fun startFocusMode() {
        val intent = Intent(this, FocusModeService::class.java)
        startForegroundService(intent)
        updateStatus()
    }

    private fun updateStatus() {
        val running = FocusModeServiceIsRunningChecker.isServiceRunning(this)
        statusText.text = if (running) "Focus Mode: ON" else "Focus Mode: OFF"
    }
}
