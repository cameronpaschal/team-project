package com.example.minimalphone

data class AppInfo(
    val name: String,
    val packageName: String,

//   Carried over from the prototype for the open/block app buttons. Keep this commented unless you want to use those buttons
//   var isBlocked: Boolean = false // Track whether app is blocked
)
