package com.st10028058.focusflowv2.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.auth.FirebaseAuth

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
        private const val KEY_EMAIL = "saved_email_"
        private const val KEY_PASSWORD = "saved_password_"
        private const val KEY_HAS_CREDENTIALS = "has_credentials_"
        private const val KEY_LOGIN_TYPE = "login_type_"
        
        const val LOGIN_TYPE_EMAIL = "email"
        const val LOGIN_TYPE_GOOGLE = "google"
    }

    /**
     * Get current user ID for account-specific storage
     */
    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    /**
     * Save user credentials securely (for email/password login) - account-specific
     */
    fun saveCredentials(email: String, password: String, userId: String? = null) {
        val uid = userId ?: getCurrentUserId()
        if (uid.isBlank()) return
        
        sharedPreferences.edit()
            .putString(KEY_EMAIL + uid, email)
            .putString(KEY_PASSWORD + uid, password)
            .putString(KEY_LOGIN_TYPE + uid, LOGIN_TYPE_EMAIL)
            .putBoolean(KEY_HAS_CREDENTIALS + uid, true)
            .apply()
    }

    /**
     * Save Google SSO login info (just email, Firebase handles the session) - account-specific
     */
    fun saveGoogleLogin(email: String, userId: String? = null) {
        val uid = userId ?: getCurrentUserId()
        if (uid.isBlank()) return
        
        sharedPreferences.edit()
            .putString(KEY_EMAIL + uid, email)
            .putString(KEY_LOGIN_TYPE + uid, LOGIN_TYPE_GOOGLE)
            .putBoolean(KEY_HAS_CREDENTIALS + uid, true)
            .apply()
    }

    /**
     * Get saved email for current user or specified user
     */
    fun getSavedEmail(userId: String? = null): String? {
        val uid = userId ?: getCurrentUserId()
        if (uid.isBlank()) return null
        
        return if (hasCredentials(uid)) {
            sharedPreferences.getString(KEY_EMAIL + uid, null)
        } else {
            null
        }
    }

    /**
     * Get saved password for current user or specified user
     */
    fun getSavedPassword(userId: String? = null): String? {
        val uid = userId ?: getCurrentUserId()
        if (uid.isBlank()) return null
        
        return if (hasCredentials(uid)) {
            sharedPreferences.getString(KEY_PASSWORD + uid, null)
        } else {
            null
        }
    }

    /**
     * Check if credentials are saved for current user or specified user
     */
    fun hasCredentials(userId: String? = null): Boolean {
        val uid = userId ?: getCurrentUserId()
        if (uid.isBlank()) return false
        
        return sharedPreferences.getBoolean(KEY_HAS_CREDENTIALS + uid, false)
    }

    /**
     * Get login type (email or google) for current user or specified user
     */
    fun getLoginType(userId: String? = null): String? {
        val uid = userId ?: getCurrentUserId()
        if (uid.isBlank()) return null
        
        return sharedPreferences.getString(KEY_LOGIN_TYPE + uid, null)
    }

    /**
     * Check if user logged in with Google SSO (for current user or specified user)
     */
    fun isGoogleLogin(userId: String? = null): Boolean {
        return getLoginType(userId) == LOGIN_TYPE_GOOGLE
    }

    /**
     * Clear saved credentials for current user or specified user
     */
    fun clearCredentials(userId: String? = null) {
        val uid = userId ?: getCurrentUserId()
        if (uid.isBlank()) return
        
        sharedPreferences.edit()
            .remove(KEY_EMAIL + uid)
            .remove(KEY_PASSWORD + uid)
            .remove(KEY_LOGIN_TYPE + uid)
            .putBoolean(KEY_HAS_CREDENTIALS + uid, false)
            .apply()
    }
    
    /**
     * Check if any account has biometric credentials saved
     */
    fun hasAnyCredentials(): Boolean {
        val allPrefs = sharedPreferences.all
        return allPrefs.keys.any { it.startsWith(KEY_HAS_CREDENTIALS) && allPrefs[it] == true }
    }
}

