package com.example.minimalphone

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import android.accessibilityservice.AccessibilityServiceInfo



// MainActivity.kt
class MainActivity : AppCompatActivity() {

    // This is the starting point when the screen launches
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Placeholder: Future app list / RecyclerView logic goes here


        // Foreground Service
        val button: Button = findViewById(R.id.startServiceButton)
        button.setOnClickListener {
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

        // Open Accessibility Settings button
        val enableAccessibilityButton: Button = findViewById(R.id.enableAccessibilityButton)
        enableAccessibilityButton.setOnClickListener {
            val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }

    // Helper: Check if service is enabled
    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_GENERIC
        )
        return enabledServices.any {
            it.resolveInfo.serviceInfo.name.contains("AppDetectionService")
        }
    }
}