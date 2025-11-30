// kotlin
package com.example.minimalphone

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BlockActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.block_screen)

        val blockedPackage = intent.getStringExtra("blocked_package") ?: "Unknown App"
// kotlin
        val appName = BlockedAppsManager.getAppDisplayName(this, blockedPackage)
        val appNameView = findViewById<TextView>(R.id.app_name_blocked)
        appNameView.text = getString(R.string.blocked_app_message, appName)

        val goHomeButton = findViewById<Button>(R.id.go_home_button)
        goHomeButton.setOnClickListener {
            goHome()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun goHome() {
        val serviceIntent = Intent(this, FocusModeService::class.java).apply {
            action = FocusModeService.ACTION_BLOCK_DISMISSED
        }
        startService(serviceIntent)

        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
