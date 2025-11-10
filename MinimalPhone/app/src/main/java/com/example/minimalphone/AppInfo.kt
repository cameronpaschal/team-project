package com.example.minimalphone

data class AppInfo(
    val name: String,
    val packageName: String,
    var isBlocked: Boolean = false // Track whether app is blocked
)

