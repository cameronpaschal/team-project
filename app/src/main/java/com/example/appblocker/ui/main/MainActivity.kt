package com.example.appblocker.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appblocker.databinding.ActivityMainBinding
import com.example.appblocker.ui.blocked.BlockedAppsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        setSupportActionBar(b.toolbar)

        b.btnManageBlocked.setOnClickListener {
            startActivity(Intent(this, BlockedAppsActivity::class.java))
        }
    }
}