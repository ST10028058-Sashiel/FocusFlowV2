package com.st10028058.focusflowv2.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.navigation.NavController
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import com.google.firebase.auth.FirebaseAuth
import com.st10028058.focusflowv2.R
import com.st10028058.focusflowv2.data.BiometricHelper
import com.st10028058.focusflowv2.data.CredentialManager
import com.st10028058.focusflowv2.ui.nav.Routes
import com.st10028058.focusflowv2.viewmodel.SettingsViewModel

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val settingsViewModel: SettingsViewModel = viewModel()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showBiometricPrompt by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Validation states
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    val biometricHelper = remember { BiometricHelper(context) }
    val credentialManager = remember { CredentialManager(context) }
    val isBiometricAvailable = remember(biometricHelper) { biometricHelper.isBiometricAvailable() }
    val biometricEnabled by settingsViewModel.biometricEnabled.collectAsState()

    // ✅ High-contrast field colors (black text on white) for BOTH themes
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFF111111),
        unfocusedTextColor = Color(0xFF111111),
        disabledTextColor = Color(0xFF111111),

        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White,

        focusedBorderColor = Color(0xFF6A0DAD),
        unfocusedBorderColor = Color(0xFFB080E0),
        disabledBorderColor = Color(0xFFB080E0),

        cursorColor = Color(0xFF6A0DAD),

        focusedLabelColor = Color(0xFF333333),
        unfocusedLabelColor = Color(0xFF555555),
        disabledLabelColor = Color(0xFF777777),

        focusedPlaceholderColor = Color(0xFF8E8E8E),
        unfocusedPlaceholderColor = Color(0xFF8E8E8E),
        disabledPlaceholderColor = Color(0xFF8E8E8E)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF6A0DAD), Color(0xFF8B2BE2))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "FOCUSFLOW",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                "Create your account",
                fontSize = 16.sp,
                color = Color(0xFFDFB8FF),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(28.dp), spotColor = Color.Black.copy(alpha = 0.1f))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            emailError = null // Clear error when user types
                        },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = fieldColors,
                        isError = emailError != null,
                        supportingText = emailError?.let { 
                            { Text(it, color = MaterialTheme.colorScheme.error) }
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            passwordError = null // Clear error when user types
                        },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = fieldColors,
                        isError = passwordError != null,
                        supportingText = passwordError?.let { 
                            { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = if (passwordError != null) MaterialTheme.colorScheme.error else Color(0xFF6A0DAD)
                                )
                            }
                        }
                    )

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            // Clear previous errors
                            emailError = null
                            passwordError = null
                            
                            // Validate email
                            val emailPattern = android.util.Patterns.EMAIL_ADDRESS
                            if (email.isBlank()) {
                                emailError = "Email is required"
                                return@Button
                            } else if (!emailPattern.matcher(email).matches()) {
                                emailError = "Please enter a valid email address"
                                return@Button
                            }
                            
                            // Validate password
                            if (password.isBlank()) {
                                passwordError = "Password is required"
                                return@Button
                            } else if (password.length < 6) {
                                passwordError = "Password must be at least 6 characters"
                                return@Button
                            }

                            isLoading = true
                            auth.createUserWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        // Save credentials for current account if biometric is enabled
                                        val currentUser = auth.currentUser
                                        if (biometricEnabled && isBiometricAvailable && currentUser != null) {
                                            credentialManager.saveCredentials(email.trim(), password, currentUser.uid)
                                        }
                                        
                                        // Prompt user to enable biometrics if available but not enabled
                                        if (isBiometricAvailable && !biometricEnabled && !settingsViewModel.hasBeenAskedAboutBiometrics()) {
                                            showBiometricPrompt = true
                                        } else {
                                            Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                            navController.navigate(Routes.Home) {
                                                popUpTo(Routes.Register) { inclusive = true }
                                            }
                                        }
                                    } else {
                                        // Handle Firebase errors
                                        val errorMessage = when {
                                            task.exception?.message?.contains("email address is already in use") == true -> 
                                                "This email is already registered. Please sign in instead."
                                            task.exception?.message?.contains("invalid email") == true -> 
                                                "Invalid email address format"
                                            task.exception?.message?.contains("password") == true -> 
                                                "Password is too weak. Please use a stronger password."
                                            else -> 
                                                task.exception?.message ?: "Registration failed. Please try again."
                                        }
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                    }
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        } else {
                            Text("Create Account", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    TextButton(onClick = { navController.navigate(Routes.Login) }) {
                        Text("Already have an account? ", color = Color(0xFF222222))
                        Text("Sign In", color = Color(0xFF6A0DAD), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(25.dp))

            Text(
                "Stay focused. Stay productive.",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
        
        // Biometric Enable Prompt Dialog
        if (showBiometricPrompt) {
            AlertDialog(
                onDismissRequest = {
                    settingsViewModel.setBiometricPromptShown(true)
                    showBiometricPrompt = false
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Register) { inclusive = true }
                    }
                },
                icon = {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        "Enable Biometric Login?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column {
                        Text(
                            "Quick and secure access to your account using your fingerprint or face recognition.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "You can enable this feature anytime in Settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            settingsViewModel.setBiometricPromptShown(true)
                            showBiometricPrompt = false
                            val activity = context as? FragmentActivity
                            if (activity != null) {
                                biometricHelper.authenticate(
                                    activity = activity,
                                    title = "Enable Biometric Login",
                                    subtitle = "Authenticate to enable biometric login for FocusFlow",
                                    negativeButtonText = "Cancel",
                                    onSuccess = {
                                        settingsViewModel.toggleBiometric(true)
                                        // Save credentials for current account
                                        val currentUser = auth.currentUser
                                        if (currentUser != null) {
                                            credentialManager.saveCredentials(email.trim(), password, currentUser.uid)
                                        }
                                        Toast.makeText(context, "Biometric login enabled for this account!", Toast.LENGTH_SHORT).show()
                                        navController.navigate(Routes.Home) {
                                            popUpTo(Routes.Register) { inclusive = true }
                                        }
                                    },
                                    onError = { error ->
                                        if (error != "User canceled") {
                                            Toast.makeText(context, "Biometric setup failed: $error", Toast.LENGTH_SHORT).show()
                                        }
                                        navController.navigate(Routes.Home) {
                                            popUpTo(Routes.Register) { inclusive = true }
                                        }
                                    },
                                    onFailed = {
                                        Toast.makeText(context, "Biometric authentication failed", Toast.LENGTH_SHORT).show()
                                        navController.navigate(Routes.Home) {
                                            popUpTo(Routes.Register) { inclusive = true }
                                        }
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Enable Now")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            settingsViewModel.setBiometricPromptShown(true)
                            showBiometricPrompt = false
                            navController.navigate(Routes.Home) {
                                popUpTo(Routes.Register) { inclusive = true }
                            }
                        }
                    ) {
                        Text("Maybe Later")
                    }
                }
            )
        }
    }
}
