package com.st10028058.focusflowv2.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _darkMode = MutableStateFlow(prefs.getBoolean("darkMode", false))
    val darkMode: StateFlow<Boolean> = _darkMode

    fun toggleDarkMode(enabled: Boolean) {
        _darkMode.value = enabled
        prefs.edit().putBoolean("darkMode", enabled).apply()
    }
}
