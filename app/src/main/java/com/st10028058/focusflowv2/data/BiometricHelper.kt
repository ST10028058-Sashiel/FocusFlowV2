package com.st10028058.focusflowv2.data

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricHelper(private val context: Context) {

    /**
     * Check if biometric authentication is available on the device
     * Tries multiple authenticator types for better compatibility
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        
        // First try with BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        val strongResult = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        
        if (strongResult == BiometricManager.BIOMETRIC_SUCCESS) {
            return true
        }
        
        // If that fails, try with BIOMETRIC_WEAK (for older devices)
        val weakResult = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        
        return weakResult == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Get the biometric status message
     */
    fun getBiometricStatus(): String {
        val biometricManager = BiometricManager.from(context)
        
        // Check strong first
        val strongResult = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        
        if (strongResult == BiometricManager.BIOMETRIC_SUCCESS) {
            return "Available"
        }
        
        // Check weak
        val weakResult = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        
        if (weakResult == BiometricManager.BIOMETRIC_SUCCESS) {
            return "Available (Weak)"
        }
        
        // Return the error from strong check
        return when (strongResult) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No biometric hardware available"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Biometric hardware unavailable"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No biometrics enrolled. Please set up fingerprint or face unlock in device settings"
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "Security update required"
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> "Biometrics not supported"
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> "Unknown status"
            else -> "Not available (Error code: $strongResult)"
        }
    }

    /**
     * Show biometric prompt for authentication
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Biometric Authentication",
        subtitle: String = "Use your fingerprint or face to authenticate",
        negativeButtonText: String = "Cancel",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }
        )

        // Try to use strong authenticators first, fall back to weak if needed
        val biometricManager = BiometricManager.from(context)
        val canUseStrong = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
        
        val authenticators = if (canUseStrong) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        }
        
        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)
        
        // Only set negative button text if device credentials are NOT allowed
        // When DEVICE_CREDENTIAL is allowed, the system provides its own cancel button
        val hasDeviceCredential = (authenticators and BiometricManager.Authenticators.DEVICE_CREDENTIAL) != 0
        if (!hasDeviceCredential && negativeButtonText.isNotEmpty()) {
            promptInfoBuilder.setNegativeButtonText(negativeButtonText)
        }
        
        val promptInfo = promptInfoBuilder.build()

        biometricPrompt.authenticate(promptInfo)
    }
}

