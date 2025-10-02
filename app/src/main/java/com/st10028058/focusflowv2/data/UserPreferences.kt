package com.st10028058.focusflowv2.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Define the extension property OUTSIDE the class
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications")
        val DISPLAY_NAME_KEY = stringPreferencesKey("display_name")
    }

    val darkMode: Flow<Boolean> = context.dataStore.data
        .map { it[DARK_MODE_KEY] ?: false }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[NOTIFICATIONS_KEY] ?: true }

    val displayName: Flow<String> = context.dataStore.data
        .map { it[DISPLAY_NAME_KEY] ?: "" }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE_KEY] = enabled }
    }

    suspend fun setNotifications(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_KEY] = enabled }
    }

    suspend fun setDisplayName(name: String) {
        context.dataStore.edit { it[DISPLAY_NAME_KEY] = name }
    }
}
