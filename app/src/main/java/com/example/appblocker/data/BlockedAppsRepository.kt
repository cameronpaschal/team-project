package com.example.appblocker.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BlockedAppsRepository private constructor(ctx: Context) {

    private val prefs: SharedPreferences =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // Backing store: a Set of packageNames
    private val _blocked = MutableStateFlow(load())
    val blockedFlow: StateFlow<Set<String>> = _blocked.asStateFlow()

    fun isBlocked(pkg: String): Boolean = _blocked.value.contains(pkg)

    fun getBlocked(): Set<String> = _blocked.value

    fun setBlocked(pkgs: Set<String>) {
        save(pkgs)
        _blocked.value = pkgs
    }

    fun toggle(pkg: String, block: Boolean) {
        val newSet = _blocked.value.toMutableSet()
        if (block) newSet.add(pkg) else newSet.remove(pkg)
        setBlocked(newSet)
    }

    private fun load(): Set<String> =
        prefs.getStringSet(KEY_BLOCKED, emptySet())?.toSet() ?: emptySet()

    private fun save(s: Set<String>) {
        prefs.edit().putStringSet(KEY_BLOCKED, s).apply()
    }

    companion object {
        private const val PREFS = "blocked_apps_prefs"
        private const val KEY_BLOCKED = "blocked_apps"

        @Volatile private var INSTANCE: BlockedAppsRepository? = null

        fun get(context: Context): BlockedAppsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: BlockedAppsRepository(context).also { INSTANCE = it }
            }
    }
}