package com.st10028058.focusflowv2.ui.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.st10028058.focusflowv2.R
import com.st10028058.focusflowv2.data.BiometricHelper
import com.st10028058.focusflowv2.data.CredentialManager
import com.st10028058.focusflowv2.ui.nav.Routes
import com.st10028058.focusflowv2.viewmodel.SettingsViewModel

@Composable
fun LoginScreen(navController: NavController) {
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
    
    val biometricEnabled by settingsViewModel.biometricEnabled.collectAsState()
    val biometricHelper = remember { BiometricHelper(context) }
    val credentialManager = remember { CredentialManager(context) }
    // Check availability dynamically instead of caching
    val isBiometricAvailable = remember(biometricHelper) { biometricHelper.isBiometricAvailable() }
    
    // Check Firebase session first
    LaunchedEffect(Unit) {
        // If user is already logged in (Firebase session exists), navigate directly to home
        if (auth.currentUser != null) {
            navController.navigate(Routes.Home) {
                popUpTo(Routes.Login) { inclusive = true }
            }
        }
        // Note: We removed automatic biometric prompt - user must click the button manually
    }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .requestProfile()
        .build()
    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        if (task.isSuccessful) {
            val account = task.result
            // Get ID token - handle null case
            account.idToken?.let { idToken ->
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { signInTask ->
                                    if (signInTask.isSuccessful) {
                                        // Save Google login info for current account if biometric is enabled
                                        val currentUser = auth.currentUser
                                        if (biometricEnabled && isBiometricAvailable && currentUser != null) {
                                            account.email?.let { email ->
                                                credentialManager.saveGoogleLogin(email, currentUser.uid)
                                            }
                                        }
                            
                            // Prompt user to enable biometrics if available but not enabled
                            if (isBiometricAvailable && !biometricEnabled && !settingsViewModel.hasBeenAskedAboutBiometrics()) {
                                showBiometricPrompt = true
                            } else {
                                Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                                navController.navigate(Routes.Home) {
                                    popUpTo(Routes.Login) { inclusive = true }
                                }
                            }
                        } else {
                            val errorMessage = signInTask.exception?.message ?: "Google Sign-in failed"
                            Toast.makeText(context, "Google Sign-in failed: $errorMessage", Toast.LENGTH_LONG).show()
                        }
                    }
            } ?: run {
                // No ID token - this shouldn't happen, but handle it
                Toast.makeText(context, "Failed to get authentication token. Please try again.", Toast.LENGTH_SHORT).show()
            }
        } else {
            val exception = task.exception
            if (exception != null) {
                val errorMessage = when {
                    exception.message?.contains("12500") == true -> "Google Sign-in canceled"
                    exception.message?.contains("12501") == true -> "Sign-in was canceled"
                    else -> "Google Sign-in failed: ${exception.message}"
                }
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Google Sign-in canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🔧 High-contrast field colors (black text on white) for BOTH themes
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
                "Productivity App",
                fontSize = 16.sp,
                color = Color(0xFFDFB8FF),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
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
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
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

                    Spacer(Modifier.height(16.dp))

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
                            }
                            
                            isLoading = true
                            auth.signInWithEmailAndPassword(email.trim(), password)
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
                                            Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                                            navController.navigate(Routes.Home) {
                                                popUpTo(Routes.Login) { inclusive = true }
                                            }
                                        }
                                    } else {
                                        // Handle Firebase errors
                                        val errorMessage = when {
                                            task.exception?.message?.contains("user not found") == true -> 
                                                "No account found with this email. Please register first."
                                            task.exception?.message?.contains("wrong password") == true -> 
                                                "Incorrect password. Please try again."
                                            task.exception?.message?.contains("invalid email") == true -> 
                                                "Invalid email address format"
                                            task.exception?.message?.contains("network") == true -> 
                                                "Network error. Please check your connection."
                                            task.exception?.message?.contains("too many requests") == true -> 
                                                "Too many failed attempts. Please try again later."
                                            else -> 
                                                task.exception?.message ?: "Login failed. Please try again."
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
                            Text("Sign In", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    TextButton(onClick = { navController.navigate(Routes.Register) }) {
                        Text("Don’t have an account? ", color = Color(0xFF222222))
                        Text("Create one", color = Color(0xFF6A0DAD), fontWeight = FontWeight.Bold)
                    }

                    Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFB080E0))

                    Button(
                        onClick = {
                            // Launch sign-in intent - Google will show account picker if multiple accounts exist
                            // This allows users to sign in with any Google account
                            val signInIntent = googleSignInClient.signInIntent
                            launcher.launch(signInIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A0DAD))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google Icon",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Continue with Google", color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            TextButton(onClick = { /* Forgot Password Navigation */ }) {
                Text("Forgot Password?", color = Color.White) // stays white on the purple background
            }
        }
        
        // Biometric Enable Prompt Dialog
        if (showBiometricPrompt) {
            AlertDialog(
                onDismissRequest = {
                    settingsViewModel.setBiometricPromptShown(true)
                    showBiometricPrompt = false
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Login) { inclusive = true }
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
                                            if (email.isNotBlank() && password.isNotBlank()) {
                                                credentialManager.saveCredentials(email.trim(), password, currentUser.uid)
                                            } else {
                                                // Try to get saved credentials
                                                val savedEmail = credentialManager.getSavedEmail(currentUser.uid)
                                                if (savedEmail != null) {
                                                    val isGoogle = credentialManager.isGoogleLogin(currentUser.uid)
                                                    if (isGoogle) {
                                                        credentialManager.saveGoogleLogin(savedEmail, currentUser.uid)
                                                    } else {
                                                        val savedPassword = credentialManager.getSavedPassword(currentUser.uid)
                                                        if (savedPassword != null) {
                                                            credentialManager.saveCredentials(savedEmail, savedPassword, currentUser.uid)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        Toast.makeText(context, "Biometric login enabled!", Toast.LENGTH_SHORT).show()
                                        navController.navigate(Routes.Home) {
                                            popUpTo(Routes.Login) { inclusive = true }
                                        }
                                    },
                                    onError = { error ->
                                        if (error != "User canceled") {
                                            Toast.makeText(context, "Biometric setup failed: $error", Toast.LENGTH_SHORT).show()
                                        }
                                        navController.navigate(Routes.Home) {
                                            popUpTo(Routes.Login) { inclusive = true }
                                        }
                                    },
                                    onFailed = {
                                        Toast.makeText(context, "Biometric authentication failed", Toast.LENGTH_SHORT).show()
                                        navController.navigate(Routes.Home) {
                                            popUpTo(Routes.Login) { inclusive = true }
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
                                popUpTo(Routes.Login) { inclusive = true }
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
