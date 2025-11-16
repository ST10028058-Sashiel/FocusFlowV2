package com.st10028058.focusflowv2.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class CredentialManager(private val context: Context) {

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val sharedPreferences: SharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                "biometric_credentials",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to regular SharedPreferences if encryption fails
            context.getSharedPreferences("biometric_credentials_fallback", Context.MODE_PRIVATE)
        }
    }

    companion object {
        private const val KEY_EMAIL = "saved_email"
        private const val KEY_PASSWORD = "saved_password"
        private const val KEY_HAS_CREDENTIALS = "has_credentials"
        private const val KEY_LOGIN_TYPE = "login_type"
        
        const val LOGIN_TYPE_EMAIL = "email"
        const val LOGIN_TYPE_GOOGLE = "google"
    }

    /**
     * Save user credentials securely (for email/password login)
     */
    fun saveCredentials(email: String, password: String) {
        sharedPreferences.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, password)
            .putString(KEY_LOGIN_TYPE, LOGIN_TYPE_EMAIL)
            .putBoolean(KEY_HAS_CREDENTIALS, true)
            .apply()
    }

    /**
     * Save Google SSO login info (just email, Firebase handles the session)
     */
    fun saveGoogleLogin(email: String) {
        sharedPreferences.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_LOGIN_TYPE, LOGIN_TYPE_GOOGLE)
            .putBoolean(KEY_HAS_CREDENTIALS, true)
            .apply()
    }

    /**
     * Get saved email
     */
    fun getSavedEmail(): String? {
        return if (hasCredentials()) {
            sharedPreferences.getString(KEY_EMAIL, null)
        } else {
            null
        }
    }

    /**
     * Get saved password
     */
    fun getSavedPassword(): String? {
        return if (hasCredentials()) {
            sharedPreferences.getString(KEY_PASSWORD, null)
        } else {
            null
        }
    }

    /**
     * Check if credentials are saved
     */
    fun hasCredentials(): Boolean {
        return sharedPreferences.getBoolean(KEY_HAS_CREDENTIALS, false)
    }

    /**
     * Get login type (email or google)
     */
    fun getLoginType(): String? {
        return sharedPreferences.getString(KEY_LOGIN_TYPE, null)
    }

    /**
     * Check if user logged in with Google SSO
     */
    fun isGoogleLogin(): Boolean {
        return getLoginType() == LOGIN_TYPE_GOOGLE
    }

    /**
     * Clear saved credentials
     */
    fun clearCredentials() {
        sharedPreferences.edit()
            .remove(KEY_EMAIL)
            .remove(KEY_PASSWORD)
            .remove(KEY_LOGIN_TYPE)
            .putBoolean(KEY_HAS_CREDENTIALS, false)
            .apply()
    }
}

