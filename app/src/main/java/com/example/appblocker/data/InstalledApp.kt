package com.example.appblocker.data

import android.graphics.drawable.Drawable

data class InstalledApp(
    val label: String,
    val packageName: String,
    val icon: Drawable?,
    val isSystem: Boolean = false,
    var isBlocked: Boolean = false
)