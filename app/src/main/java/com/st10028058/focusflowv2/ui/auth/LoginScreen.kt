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
import androidx.compose.ui.text.input.KeyboardType
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
    
    val biometricEnabled by settingsViewModel.biometricEnabled.collectAsState()
    val biometricHelper = remember { BiometricHelper(context) }
    val credentialManager = remember { CredentialManager(context) }
    // Check availability dynamically instead of caching
    val isBiometricAvailable = remember(biometricHelper) { biometricHelper.isBiometricAvailable() }
    
    // Check Firebase session first, then prompt biometric if needed
    LaunchedEffect(Unit) {
        // If user is already logged in (Firebase session exists), navigate directly to home
        if (auth.currentUser != null) {
            navController.navigate(Routes.Home) {
                popUpTo(Routes.Login) { inclusive = true }
            }
            return@LaunchedEffect
        }
        
        // If not logged in, check if we have saved credentials and biometric is enabled
        var hasPromptedBiometric = false
        if (biometricEnabled && isBiometricAvailable && credentialManager.hasCredentials() && !hasPromptedBiometric) {
            hasPromptedBiometric = true
            val activity = context as? FragmentActivity
            if (activity != null) {
                // Small delay to ensure UI is ready
                kotlinx.coroutines.delay(300)
                biometricHelper.authenticate(
                    activity = activity,
                    title = "Biometric Login",
                    subtitle = "Use your fingerprint or face to log in",
                    negativeButtonText = "Use Password",
                    onSuccess = {
                        // Biometric successful, check login type and authenticate accordingly
                        if (credentialManager.isGoogleLogin()) {
                            // Google SSO user - check if Firebase session still exists
                            if (auth.currentUser != null) {
                                // Firebase session exists, navigate to home
                                navController.navigate(Routes.Home) {
                                    popUpTo(Routes.Login) { inclusive = true }
                                }
                            } else {
                                // Check if Google Sign-In still has an active session
                                val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
                                if (googleSignInAccount != null && googleSignInAccount.idToken != null) {
                                    // Google session exists, re-authenticate with Firebase
                                    isLoading = true
                                    val credential = GoogleAuthProvider.getCredential(googleSignInAccount.idToken, null)
                                    auth.signInWithCredential(credential)
                                        .addOnCompleteListener { task ->
                                            isLoading = false
                                            if (task.isSuccessful) {
                                                navController.navigate(Routes.Home) {
                                                    popUpTo(Routes.Login) { inclusive = true }
                                                }
                                            } else {
                                                Toast.makeText(context, "Session expired. Please sign in with Google again.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    // No active Google session, need to re-authenticate
                                    Toast.makeText(context, "Session expired. Please sign in with Google again.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // Email/password user - use saved credentials to log in
                            val savedEmail = credentialManager.getSavedEmail()
                            val savedPassword = credentialManager.getSavedPassword()
                            
                            if (savedEmail != null && savedPassword != null) {
                                isLoading = true
                                auth.signInWithEmailAndPassword(savedEmail, savedPassword)
                                    .addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) {
                                            navController.navigate(Routes.Home) {
                                                popUpTo(Routes.Login) { inclusive = true }
                                            }
                                        } else {
                                            Toast.makeText(context, "Login failed. Please try again.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(context, "No saved credentials found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onError = { error ->
                        // Biometric failed, show login form - user can use password or Google
                    },
                    onFailed = {
                        // Biometric failed, show login form
                    }
                )
            }
        }
    }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        if (task.isSuccessful) {
            val account = task.result
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { signInTask ->
                    if (signInTask.isSuccessful) {
                        // Save Google login info if biometric is enabled
                        if (biometricEnabled && isBiometricAvailable) {
                            account.email?.let { email ->
                                credentialManager.saveGoogleLogin(email)
                            }
                        }
                        navController.navigate(Routes.Home) {
                            popUpTo(Routes.Login) { inclusive = true }
                        }
                    } else {
                        Toast.makeText(context, "Google Sign-in failed", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(context, "Google Sign-in canceled", Toast.LENGTH_SHORT).show()
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
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = fieldColors
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = fieldColors
                    )

                    Spacer(Modifier.height(16.dp))

                    // Biometric Login Button (if enabled, available, and credentials are saved)
                    if (biometricEnabled && isBiometricAvailable && credentialManager.hasCredentials()) {
                        Button(
                            onClick = {
                                val activity = context as? FragmentActivity
                                if (activity != null) {
                                    isLoading = true
                                    biometricHelper.authenticate(
                                        activity = activity,
                                        title = "Biometric Login",
                                        subtitle = "Use your fingerprint or face to log in",
                                        negativeButtonText = "Cancel",
                                        onSuccess = {
                                            // Check login type and authenticate accordingly
                                            if (credentialManager.isGoogleLogin()) {
                                                // Google SSO user - check if Firebase session still exists
                                                if (auth.currentUser != null) {
                                                    // Firebase session exists, navigate to home
                                                    isLoading = false
                                                    navController.navigate(Routes.Home) {
                                                        popUpTo(Routes.Login) { inclusive = true }
                                                    }
                                                } else {
                                                    // Check if Google Sign-In still has an active session
                                                    val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
                                                    if (googleSignInAccount != null && googleSignInAccount.idToken != null) {
                                                        // Google session exists, re-authenticate with Firebase
                                                        val credential = GoogleAuthProvider.getCredential(googleSignInAccount.idToken, null)
                                                        auth.signInWithCredential(credential)
                                                            .addOnCompleteListener { task ->
                                                                isLoading = false
                                                                if (task.isSuccessful) {
                                                                    navController.navigate(Routes.Home) {
                                                                        popUpTo(Routes.Login) { inclusive = true }
                                                                    }
                                                                } else {
                                                                    Toast.makeText(context, "Session expired. Please sign in with Google again.", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                    } else {
                                                        // No active Google session, need to re-authenticate
                                                        isLoading = false
                                                        Toast.makeText(context, "Session expired. Please sign in with Google again.", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            } else {
                                                // Email/password user - use saved credentials to log in
                                                val savedEmail = credentialManager.getSavedEmail()
                                                val savedPassword = credentialManager.getSavedPassword()
                                                
                                                if (savedEmail != null && savedPassword != null) {
                                                    auth.signInWithEmailAndPassword(savedEmail, savedPassword)
                                                        .addOnCompleteListener { task ->
                                                            isLoading = false
                                                            if (task.isSuccessful) {
                                                                navController.navigate(Routes.Home) {
                                                                    popUpTo(Routes.Login) { inclusive = true }
                                                                }
                                                            } else {
                                                                Toast.makeText(context, "Login failed. Please try again.", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                } else {
                                                    isLoading = false
                                                    Toast.makeText(context, "No saved credentials found", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        onError = { error ->
                                            isLoading = false
                                            if (error != "User canceled") {
                                                Toast.makeText(context, "Biometric authentication failed: $error", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onFailed = {
                                            isLoading = false
                                            Toast.makeText(context, "Biometric authentication failed", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A0DAD))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Fingerprint",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Sign In with Biometric", color = Color.White, fontWeight = FontWeight.Medium)
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(modifier = Modifier.weight(1f), color = Color(0xFFB080E0))
                            Text(
                                "OR",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color(0xFFB080E0),
                                fontSize = 12.sp
                            )
                            Divider(modifier = Modifier.weight(1f), color = Color(0xFFB080E0))
                        }
                        
                        Spacer(Modifier.height(12.dp))
                    }

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        // Save credentials if biometric is enabled
                                        if (biometricEnabled && isBiometricAvailable) {
                                            credentialManager.saveCredentials(email, password)
                                        }
                                        navController.navigate(Routes.Home) {
                                            popUpTo(Routes.Login) { inclusive = true }
                                        }
                                    } else {
                                        Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
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
    }
}
