package com.nhd.news.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhd.news.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = preferencesManager.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun toggleDarkMode() {
        viewModelScope.launch {
            preferencesManager.toggleDarkMode()
        }
    }

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDarkMode(isDark)
        }
    }
}
