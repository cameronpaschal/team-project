//package com.example.minimalphone
//
//import android.content.Context
//import com.example.minimalphone.LatencyLogger
//// This file does not exist yet
//import com.example.minimalphone.BlockedAppsRepository
//
//class BlockerController(
//    private val context: Context,
////  TODO: Change this to match the naming of the actual app once it's uploaded
//    private val blockedAppsRepo: BlockedAppsRepository,
//    private val latencyLogger: LatencyLogger,
//    private val debounceManager: DebounceManager
//){
//    fun onForegroundAppDetected(packageName: String) {
//        if (!blockedAppsRepo.isBlocked(packageName)) return
//        if (debounceManager.shouldIgnore(packageName)) return
//
//        latencyLogger.markDetected(packageName)
//
//        BlockActivityLauncher.launch(context, packageName)
//
//        latencyLogger.markBlocked(packageName)
//
//
//    }
//}