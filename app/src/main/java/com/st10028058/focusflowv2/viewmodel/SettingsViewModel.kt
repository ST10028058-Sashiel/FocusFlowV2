// SettingsViewModel.kt
package com.st10028058.focusflowv2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.st10028058.focusflowv2.data.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = UserPreferences(app)

    val darkMode = prefs.darkMode.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val notificationsEnabled = prefs.notificationsEnabled.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val displayName = prefs.displayName.stateIn(viewModelScope, SharingStarted.Lazily, "")

    fun setDarkMode(enabled: Boolean) = viewModelScope.launch {
        prefs.setDarkMode(enabled)
    }

    fun setNotifications(enabled: Boolean) = viewModelScope.launch {
        prefs.setNotifications(enabled)
    }

    fun setDisplayName(name: String) = viewModelScope.launch {
        prefs.setDisplayName(name)
    }
}
