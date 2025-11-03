package com.nhd.news.data.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE
    )

    private val _isDarkMode = MutableStateFlow(
        preferences.getBoolean(KEY_DARK_MODE, false)
    )
    val isDarkMode: Flow<Boolean> = _isDarkMode.asStateFlow()

    fun setDarkMode(isDark: Boolean) {
        preferences.edit().putBoolean(KEY_DARK_MODE, isDark).apply()
        _isDarkMode.value = isDark
    }

    fun toggleDarkMode() {
        val newValue = !_isDarkMode.value
        setDarkMode(newValue)
    }

    companion object {
        private const val PREFERENCES_NAME = "news_preferences"
        private const val KEY_DARK_MODE = "dark_mode"
    }
}
