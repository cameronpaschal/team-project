package com.example.Prototype

data class AppInfo(
    val name: String,
    val packageName: String,
    var isBlocked: Boolean = false // Track blockage
)
