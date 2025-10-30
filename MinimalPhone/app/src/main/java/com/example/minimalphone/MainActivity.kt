package com.example.minimalphone

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import android.accessibilityservice.AccessibilityServiceInfo



// MainActivity.kt
class MainActivity : AppCompatActivity() {

    private lateinit var startServiceButton: Button
    private lateinit var enableAccessibilityButton: Button
    private lateinit var statusText: TextView


    // This is the starting point when the screen launches
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Placeholder: Future app list / RecyclerView logic goes here


        startServiceButton = findViewById(R.id.startServiceButton)
        enableAccessibilityButton = findViewById(R.id.enableAccessibilityButton)
        statusText = findViewById(R.id.statusText)


        // Show initial status
        updateAccessibilityStatus()

        // Button: Open Accessibility Settings
        enableAccessibilityButton.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
                Toast.makeText(this, "Opening Accessibility Settings...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to open Accessibility Settings.", Toast.LENGTH_SHORT).show()
            }
        }

        // Foreground Service
        startServiceButton.setOnClickListener {
            if (isAccessibilityServiceEnabled()) {
                val intent = Intent(this, ForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
                Toast.makeText(this, "Service started!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enable Accessibility Service first!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Re-check status when returning to the app
    override fun onResume() {
        super.onResume()
        updateAccessibilityStatus()
    }


    // Check if AppDetectionService is active
    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_GENERIC
        )
        return enabledServices.any {
            it.resolveInfo.serviceInfo.name.contains("AppDetectionService")
        }
    }

    // Update UI status and button states
    private fun updateAccessibilityStatus() {
        if (isAccessibilityServiceEnabled()) {
            statusText.text = "Accessibility Service Enabled"
            startServiceButton.isEnabled = true
    }
    }
}