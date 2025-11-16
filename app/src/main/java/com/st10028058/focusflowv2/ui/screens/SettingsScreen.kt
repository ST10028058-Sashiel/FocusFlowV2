package com.st10028058.focusflowv2.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import com.st10028058.focusflowv2.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.st10028058.focusflowv2.data.BiometricHelper
import com.st10028058.focusflowv2.data.CredentialManager
import com.st10028058.focusflowv2.ui.nav.Routes
import com.st10028058.focusflowv2.utils.LocaleHelper
import com.st10028058.focusflowv2.viewmodel.SettingsViewModel
import com.st10028058.focusflowv2.viewmodel.TaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    taskViewModel: TaskViewModel,
    settingsViewModel: SettingsViewModel
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Get activity for biometric authentication
    val activity = remember {
        when (context) {
            is FragmentActivity -> context
            is android.app.Activity -> context as? FragmentActivity
            else -> null
        }
    }

    val themeMode by settingsViewModel.themeMode.collectAsState()
    val biometricEnabled by settingsViewModel.biometricEnabled.collectAsState()
    val selectedLanguage by settingsViewModel.language.collectAsState()
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val biometricHelper = remember { BiometricHelper(context) }
    val credentialManager = remember { CredentialManager(context) }
    // Check availability - use state so we can refresh it
    var isBiometricAvailable by remember { mutableStateOf(biometricHelper.isBiometricAvailable()) }
    var biometricStatusMessage by remember { mutableStateOf(biometricHelper.getBiometricStatus()) }

    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // 👤 Profile Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = colors.primary,
                    modifier = Modifier.size(90.dp)
                )
                Text(
                    text = user?.displayName ?: "FocusFlow User",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onBackground
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = colors.primary)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = user?.email ?: "No email",
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 🌗 App Preferences
            SettingsCard(title = "App Preferences") {
                // Enhanced Theme Mode Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = colors.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Theme",
                                fontWeight = FontWeight.Medium,
                                color = colors.onSurface,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                when (themeMode) {
                                    com.st10028058.focusflowv2.viewmodel.ThemeMode.SYSTEM -> "Follow system"
                                    com.st10028058.focusflowv2.viewmodel.ThemeMode.LIGHT -> "Light mode"
                                    com.st10028058.focusflowv2.viewmodel.ThemeMode.DARK -> "Dark mode"
                                },
                                color = colors.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    IconButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Change theme",
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Theme Selection Dialog
                if (showThemeDialog) {
                    AlertDialog(
                        onDismissRequest = { showThemeDialog = false },
                        title = {
                            Text(
                                "Choose Theme",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        text = {
                            Column {
                                ThemeOption(
                                    title = "Follow System",
                                    description = "Match device theme",
                                    icon = Icons.Default.Settings,
                                    selected = themeMode == com.st10028058.focusflowv2.viewmodel.ThemeMode.SYSTEM,
                                    onClick = {
                                        settingsViewModel.setThemeMode(com.st10028058.focusflowv2.viewmodel.ThemeMode.SYSTEM)
                                        showThemeDialog = false
                                    }
                                )
                                Spacer(Modifier.height(12.dp))
                                ThemeOption(
                                    title = "Light Mode",
                                    description = "Always use light theme",
                                    icon = Icons.Default.WbSunny,
                                    selected = themeMode == com.st10028058.focusflowv2.viewmodel.ThemeMode.LIGHT,
                                    onClick = {
                                        settingsViewModel.setThemeMode(com.st10028058.focusflowv2.viewmodel.ThemeMode.LIGHT)
                                        showThemeDialog = false
                                    }
                                )
                                Spacer(Modifier.height(12.dp))
                                ThemeOption(
                                    title = "Dark Mode",
                                    description = "Always use dark theme",
                                    icon = Icons.Default.DarkMode,
                                    selected = themeMode == com.st10028058.focusflowv2.viewmodel.ThemeMode.DARK,
                                    onClick = {
                                        settingsViewModel.setThemeMode(com.st10028058.focusflowv2.viewmodel.ThemeMode.DARK)
                                        showThemeDialog = false
                                    }
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showThemeDialog = false }) {
                                Text("Close")
                            }
                        }
                    )
                }
                SettingRow(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                // Enhanced Biometric Login with prompt card
                if (!isBiometricAvailable) {
                    // Show status message if biometrics not available
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = colors.onSurface.copy(alpha = 0.38f)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Biometric Login",
                                fontWeight = FontWeight.Medium,
                                color = colors.onSurface.copy(alpha = 0.38f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                biometricStatusMessage,
                                color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else if (!biometricEnabled && !settingsViewModel.hasBeenAskedAboutBiometrics()) {
                    // Show prompt card if biometrics available but not enabled
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Enable Biometric Login?",
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Quick and secure access using your fingerprint or face recognition.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        // Try multiple ways to get FragmentActivity
                                        val fragmentActivity: FragmentActivity? = when {
                                            activity != null -> activity
                                            context is FragmentActivity -> context
                                            else -> {
                                                // Try to get from context
                                                var act: FragmentActivity? = null
                                                try {
                                                    if (context is android.app.Activity) {
                                                        act = context as? FragmentActivity
                                                    }
                                                } catch (e: Exception) {
                                                    // Ignore
                                                }
                                                act
                                            }
                                        }
                                        
                                        if (fragmentActivity != null) {
                                            try {
                                                biometricHelper.authenticate(
                                                    activity = fragmentActivity,
                                                    title = "Enable Biometric Login",
                                                    subtitle = "Authenticate to enable biometric login for FocusFlow",
                                                    negativeButtonText = "Cancel",
                                                    onSuccess = {
                                                        settingsViewModel.toggleBiometric(true)
                                                        settingsViewModel.setBiometricPromptShown(true)
                                                        // Save credentials if user is logged in
                                                        val currentUser = FirebaseAuth.getInstance().currentUser
                                                        currentUser?.email?.let { email ->
                                                            if (credentialManager.isGoogleLogin()) {
                                                                credentialManager.saveGoogleLogin(email)
                                                            }
                                                        }
                                                        Toast.makeText(context, "Biometric login enabled!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    onError = { error ->
                                                        if (error != "User canceled") {
                                                            Toast.makeText(context, "Biometric setup failed: $error", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    onFailed = {
                                                        Toast.makeText(context, "Biometric authentication failed", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "Unable to access biometric authentication. Please try again.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.primary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.defaultMinSize(minWidth = 100.dp)
                                ) {
                                    Text("Enable", color = Color.White)
                                }
                                Spacer(Modifier.width(8.dp))
                                TextButton(
                                    onClick = {
                                        settingsViewModel.setBiometricPromptShown(true)
                                    }
                                ) {
                                    Text("Dismiss", color = colors.onSurfaceVariant)
                                }
                            }
                        }
                    }
                } else {
                    // Regular toggle if already asked or enabled
                    SettingRow(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric Login",
                        checked = biometricEnabled,
                        enabled = isBiometricAvailable,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                // User wants to enable biometric - prompt for authentication
                                if (activity != null && isBiometricAvailable) {
                                    biometricHelper.authenticate(
                                        activity = activity,
                                        title = "Enable Biometric Login",
                                        subtitle = "Authenticate to enable biometric login for FocusFlow",
                                        negativeButtonText = "Cancel",
                                        onSuccess = {
                                            settingsViewModel.toggleBiometric(true)
                                            Toast.makeText(context, "Biometric login enabled", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { error ->
                                            Toast.makeText(context, "Biometric authentication failed: $error", Toast.LENGTH_SHORT).show()
                                        },
                                        onFailed = {
                                            Toast.makeText(context, "Biometric authentication failed", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } else {
                                    Toast.makeText(context, "Biometric authentication not available", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // User wants to disable biometric - clear saved credentials
                                settingsViewModel.toggleBiometric(false)
                                credentialManager.clearCredentials()
                                Toast.makeText(context, "Biometric login disabled", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
                // Status message and refresh button for when biometrics are not available
                if (!isBiometricAvailable) {
                    Column(
                        modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                    ) {
                        TextButton(
                            onClick = {
                                // Refresh biometric status
                                isBiometricAvailable = biometricHelper.isBiometricAvailable()
                                biometricStatusMessage = biometricHelper.getBiometricStatus()
                            },
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = context.getString(R.string.refresh_status),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.primary
                            )
                        }
                    }
                }
                
                // Language Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = context.getString(R.string.language),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurface
                        )
                        Text(
                            text = when (selectedLanguage) {
                                "af" -> context.getString(R.string.language_afrikaans)
                                "zu" -> context.getString(R.string.language_zulu)
                                else -> context.getString(R.string.language_english)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { showLanguageDialog = true }) {
                        Text(
                            text = context.getString(R.string.select_language),
                            color = colors.primary
                        )
                    }
                }
            }
            
            // Language Selection Dialog
            if (showLanguageDialog) {
                AlertDialog(
                    onDismissRequest = { showLanguageDialog = false },
                    title = { Text(context.getString(R.string.select_language)) },
                    text = {
                        Column {
                            val languages = listOf(
                                "en" to context.getString(R.string.language_english),
                                "af" to context.getString(R.string.language_afrikaans),
                                "zu" to context.getString(R.string.language_zulu)
                            )
                            languages.forEach { (code, name) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedLanguage == code,
                                        onClick = {
                                            settingsViewModel.setLanguage(code)
                                            LocaleHelper.setLocale(context, code)
                                            showLanguageDialog = false
                                            (context as? android.app.Activity)?.recreate()
                                        }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                            ) {
                                                settingsViewModel.setLanguage(code)
                                                LocaleHelper.setLocale(context, code)
                                                showLanguageDialog = false
                                                (context as? android.app.Activity)?.recreate()
                                            }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showLanguageDialog = false }) {
                            Text(context.getString(R.string.cancel))
                        }
                    }
                )
            }

            // 🔄 Sync Settings
            SettingsCard(title = "Sync Settings") {
                TextButton(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            taskViewModel.syncTasks()
                            scope.launch(Dispatchers.Main) {
                                Toast.makeText(context, "Sync completed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Sync Tasks Now")
                }
                Text(
                    text = "Syncs pending offline changes with server",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            // 🧾 Data Settings
            SettingsCard(title = "Data Settings") {
                TextButton(onClick = {
                    scope.launch(Dispatchers.IO) {
                        val file = exportTasksToCSV(context, taskViewModel)
                        scope.launch(Dispatchers.Main) {
                            if (file != null) {
                                Toast.makeText(context, "Tasks exported successfully!", Toast.LENGTH_SHORT).show()
                                shareCSVFile(context, file)
                            } else {
                                Toast.makeText(context, "Failed to export tasks", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = colors.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Export & Share Tasks", color = colors.primary)
                }
            }

            // 🔒 Account Settings
            SettingsCard(title = "Account Settings") {
                TextButton(onClick = {
                    user?.email?.let {
                        auth.sendPasswordResetEmail(it)
                        Toast.makeText(context, "Password reset email sent to $it", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = colors.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Change Password", color = colors.primary)
                }

                TextButton(onClick = {
                    user?.delete()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                            navController.navigate(Routes.Login) {
                                popUpTo(Routes.Login) { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "Failed to delete account", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Account", color = MaterialTheme.colorScheme.error)
                }

                TextButton(onClick = {
                    auth.signOut()
                    // Clear saved credentials on logout
                    credentialManager.clearCredentials()
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = colors.secondary)
                    Spacer(Modifier.width(8.dp))
                    Text("Logout", color = colors.secondary)
                }
            }
        }
    }
}

// 🧱 Reusable Components
@Composable
fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = colors.primary.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = colors.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) colors.primary else colors.onSurface.copy(alpha = 0.38f)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                title,
                color = if (enabled) colors.onSurface else colors.onSurface.copy(alpha = 0.38f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.primary,
                checkedTrackColor = colors.primary.copy(alpha = 0.54f),
                uncheckedThumbColor = colors.outline,
                uncheckedTrackColor = colors.surfaceVariant
            )
        )
    }
}

@Composable
fun ThemeOption(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val backgroundColor = if (selected) {
        colors.primaryContainer
    } else {
        colors.surfaceVariant.copy(alpha = 0.3f)
    }
    val interactionSource = remember { MutableInteractionSource() }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = colors.onSurface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 4.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick,
                    indication = null,
                    interactionSource = interactionSource
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) colors.primary else colors.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) colors.primary else colors.onSurface,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    description,
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (selected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// 🧾 Export Tasks to CSV (unchanged)
fun exportTasksToCSV(context: Context, taskViewModel: TaskViewModel): File? {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "FocusFlow_Tasks_${System.currentTimeMillis()}.csv"
        )

        FileWriter(file).use { writer ->
            writer.append("Title,Location,Priority,Completed,Start Date,End Date,Reminder (mins)\n")

            for (task in taskViewModel.tasks.value) {
                val startDate = task.startTime?.let { dateFormat.format(Date(it)) } ?: ""
                val endDate = task.endTime?.let { dateFormat.format(Date(it)) } ?: ""
                val reminder = task.reminderOffsetMinutes ?: 0

                writer.append(
                    "${sanitize(task.title)}," +
                            "${sanitize(task.location)}," +
                            "${sanitize(task.priority)}," +
                            "${task.completed ?: false}," +
                            "$startDate," +
                            "$endDate," +
                            "$reminder\n"
                )
            }
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun sanitize(text: String?): String =
    text?.replace(",", ";")?.replace("\n", " ") ?: ""

fun shareCSVFile(context: Context, file: File) {
    val uri: Uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".provider",
        file
    )
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Tasks CSV via"))
}
