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

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        // Open Debug Activity button
        val debugButton: Button = findViewById(R.id.openDebugButton)
        debugButton.setOnClickListener {
            val intent = Intent(this, Usage::class.java)
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
