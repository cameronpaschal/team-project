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
import android.accessibilityservice.AccessibilityServiceInfo

class OnboardingActivity : AppCompatActivity() {

    private lateinit var usageStatus: TextView
    private lateinit var overlayStatus: TextView
    private lateinit var accessibilityStatus: TextView
    private lateinit var continueButton: Button

    //  Code that runs on the start up of the onboarding screen
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

//        Creation and linking of buttons for the permissions
        usageStatus = findViewById(R.id.usageStatus)
        overlayStatus = findViewById(R.id.overlayStatus)
        accessibilityStatus = findViewById(R.id.statusText)
        continueButton = findViewById(R.id.continueButton)

        val usageButton: Button = findViewById(R.id.usageSettingsButton)
        val overlayButton: Button = findViewById(R.id.overlaySettingsButton)
        val accessibilityButton: Button = findViewById(R.id.enableAccessibilityButton)

        usageButton.setOnClickListener { openUsageAccessSettings() }
        overlayButton.setOnClickListener { openOverlaySettings() }
        accessibilityButton.setOnClickListener { openAccessibilitySettings() }

//    Checks permisisons status on creation
        updatepermissionsStatus()
    }

    override fun onResume() {
        super.onResume()
        updatepermissionsStatus()
    }

    private fun updatepermissionsStatus() {
//      Variables to check if the permissions have been granted
        val usageGranted = hasUsageAccess()
        val overlayGranted = Settings.canDrawOverlays(this)
        val accessibilityGranted = isAccessibilityServiceEnabled()

//      Display text based on permission status
        usageStatus.text = "Usage Access: " + if (usageGranted) "Granted" else "Not granted"
        overlayStatus.text = "Overlay Permission: " + if (overlayGranted) "Granted" else "Not granted"
        accessibilityStatus.text = "Accessibility Permission: " + if (accessibilityGranted) "Granted" else "Not granted"

//      Enable continue button only if all permissions are granted
        continueButton.isEnabled = usageGranted && overlayGranted && accessibilityGranted

//        If all permissions are granted, continue to main activity
        continueButton.setOnClickListener {
            if (usageGranted && overlayGranted && accessibilityGranted) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    //    Function to open usage access settings
    private fun openUsageAccessSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    //    Function to open overlay settings
    private fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    //    Function to open accessibility settings
    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    //    Function to check if usage access permission is granted
    private fun hasUsageAccess(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow( // for Android 10+
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow( // for older Androids (7–9)
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    //    Function to check if accessibility service is enabled
    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_GENERIC
        )
        return enabledServices.any {
            it.resolveInfo.serviceInfo.name.contains("AppDetectionService")
        }
    }
}
