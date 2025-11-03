package com.nhd.news.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    
    const val THEME_LIGHT = 0
    const val THEME_DARK = 1
    const val THEME_SYSTEM = 2
    
    fun applyTheme(themeMode: Int) {
        when (themeMode) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
    
    fun saveThemeMode(context: Context, themeMode: Int) {
        val prefs = getPreferences(context)
        prefs.edit().putInt(KEY_THEME_MODE, themeMode).apply()
    }
    
    fun getThemeMode(context: Context): Int {
        val prefs = getPreferences(context)
        return prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM)
    }
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun initializeTheme(context: Context) {
        val themeMode = getThemeMode(context)
        applyTheme(themeMode)
    }
}
