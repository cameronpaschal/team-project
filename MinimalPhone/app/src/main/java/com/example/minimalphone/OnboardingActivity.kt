package com.example.minimalphone

import androidx.appcompat.app.AppCompatActivity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.view.accessibility.AccessibilityManager

class OnboardingActivity : AppCompatActivity() {

    private lateinit var usageStatus: TextView
    private lateinit var overlayStatus: TextView
    private lateinit var continueButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        usageStatus = findViewById(R.id.usageStatus)
        overlayStatus = findViewById(R.id.overlayStatus)
        continueButton = findViewById(R.id.continueButton)

        val usageButton: Button = findViewById(R.id.usageSettingsButton)
        val overlayButton: Button = findViewById(R.id.overlaySettingsButton)
        val accessibilityButton: Button = findViewById(R.id.accessibilitySettingsButton)


        usageButton.setOnClickListener { openUsageAccessSettings() }
        overlayButton.setOnClickListener { openOverlaySettings() }
        accessibilityButton.setOnClickListener { openAccessibilitySettings() }


        updatepermissionsStatus()
    }

    override fun onResume() {
        super.onResume()
        updatepermissionsStatus()
    }

    private fun updatepermissionsStatus() {
        val usageGranted = hasUsageAccess()
        val overlayGranted = Settings.canDrawOverlays(this)
        val accessibilityGranted = isAccessibilityServiceEnabled()


        usageStatus.text = "Usage Access: " + if (usageGranted) "Granted" else "Not granted"
        overlayStatus.text = "Overlay Permission: " + if (overlayGranted) "Granted" else "Not granted"
        findViewById<TextView>(R.id.accessibilityStatus).text = "Accessibility: " + if (accessibilityGranted) "Granted" else "Not granted"

        continueButton.isEnabled = usageGranted && overlayGranted

        continueButton.setOnClickListener {
            if (usageGranted && overlayGranted) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun openUsageAccessSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun hasUsageAccess(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow( // ✅ for Android 10+
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow( // ✅ for older Androids (7–9)
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return am.isEnabled
    }


    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }


}
