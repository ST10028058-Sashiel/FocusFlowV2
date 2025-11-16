package com.st10028058.focusflowv2.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // Theme mode: SYSTEM, LIGHT, or DARK
    private val themeModeString = prefs.getString("themeMode", "SYSTEM") ?: "SYSTEM"
    private val _themeMode = MutableStateFlow(
        try {
            ThemeMode.valueOf(themeModeString)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode

    // Legacy support - computed from theme mode
    val darkMode: StateFlow<Boolean> = MutableStateFlow(
        when (_themeMode.value) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> {
                // Check system theme
                val nightModeFlags = application.resources.configuration.uiMode and
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }
    )

    private val _biometricEnabled = MutableStateFlow(prefs.getBoolean("biometricEnabled", false))
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled

    private val _language = MutableStateFlow(prefs.getString("language", "en") ?: "en")
    val language: StateFlow<String> = _language

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("themeMode", mode.name).apply()
    }

    // Legacy method for backward compatibility
    fun toggleDarkMode(enabled: Boolean) {
        setThemeMode(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
    }

    fun toggleBiometric(enabled: Boolean) {
        _biometricEnabled.value = enabled
        prefs.edit().putBoolean("biometricEnabled", enabled).apply()
    }

    fun setLanguage(languageCode: String) {
        _language.value = languageCode
        prefs.edit().putString("language", languageCode).apply()
    }
}
