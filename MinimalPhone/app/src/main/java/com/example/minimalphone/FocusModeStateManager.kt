package com.example.minimalphone

import android.content.Context
import androidx.core.content.edit

object FocusModeStateManager {
    private const val PREF_NAME = "focus_mode_state"
    private const val KEY_FOCUS_MODE_ON = "focus_mode_on"

    fun setFocusModeOn(context: Context, on: Boolean) {
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_FOCUS_MODE_ON, on) }
    }

    fun isFocusModeOn(context: Context): Boolean {
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_FOCUS_MODE_ON, false)
    }
}

