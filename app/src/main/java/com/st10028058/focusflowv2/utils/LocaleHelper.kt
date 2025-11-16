package com.st10028058.focusflowv2.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {
    private const val PREFS_NAME = "app_prefs"
    private const val SELECTED_LANGUAGE = "selected_language"

    fun setLocale(context: Context, languageCode: String): Context {
        persist(context, languageCode)
        return updateResources(context, languageCode)
    }

    fun getLocale(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(SELECTED_LANGUAGE, "en") ?: "en"
    }

    private fun persist(context: Context, languageCode: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(SELECTED_LANGUAGE, languageCode)
        editor.apply()
    }

    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config: Configuration = context.resources.configuration
        config.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }

    fun attachBaseContext(context: Context): Context {
        val language = getLocale(context)
        return setLocale(context, language)
    }
}

