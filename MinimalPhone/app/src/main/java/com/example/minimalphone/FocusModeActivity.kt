package com.example.minimalphone

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity


class FocusModeActivity : AppCompatActivity() {
    private lateinit var circleToggle: ToggleButton
    private lateinit var blockedAppsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_focus_mode)

        circleToggle = findViewById(R.id.circleToggle)
        blockedAppsButton = findViewById(R.id.blockedAppsButton)


        circleToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Toggle ON logic
                FocusModeStateManager.setFocusModeOn(this, true)
                startFocusMode()

                circleToggle.setTextColor(Color.WHITE)
            } else {
                // Toggle OFF logic
                FocusModeStateManager.setFocusModeOn(this, false)
                stopService(Intent(this, FocusModeService::class.java))

                circleToggle.setTextColor(Color.BLACK)
            }
        }
        blockedAppsButton.setOnClickListener {
            startActivity(Intent(this, BlockedAppsActivity::class.java))
        }
    }

    private fun startFocusMode() {
        val intent = Intent(this, FocusModeService::class.java)
        startForegroundService(intent)
    }

}
