package com.st10028058.focusflowv2.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
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
import com.google.firebase.auth.FirebaseAuth
import com.st10028058.focusflowv2.ui.nav.Routes
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

    val darkModeEnabled by settingsViewModel.darkMode.collectAsState()
    var notificationsEnabled by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Color(0xFFF9F6FF)
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
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = Color(0xFF6A0DAD),
                    modifier = Modifier.size(90.dp)
                )
                Text(
                    text = user?.displayName ?: "FocusFlow User",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF6A0DAD))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = user?.email ?: "No email",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 🌗 App Preferences
            SettingsCard(title = "App Preferences") {
                SettingRow(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    checked = darkModeEnabled,
                    onCheckedChange = {
                        settingsViewModel.toggleDarkMode(it)
                    }
                )
                SettingRow(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
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
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF6A0DAD))
                    Spacer(Modifier.width(8.dp))
                    Text("Export & Share Tasks", color = Color(0xFF6A0DAD))
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
                    Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF6A0DAD))
                    Spacer(Modifier.width(8.dp))
                    Text("Change Password", color = Color(0xFF6A0DAD))
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
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Account", color = Color.Red)
                }

                // 🚪 Logout inside Account Settings
                TextButton(onClick = {
                    auth.signOut()
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color(0xFFE91E63))
                    Spacer(Modifier.width(8.dp))
                    Text("Logout", color = Color(0xFFE91E63))
                }
            }
        }
    }
}

// 🧱 Reusable Components
@Composable
fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6A0DAD),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFF6A0DAD))
            Spacer(Modifier.width(8.dp))
            Text(title, color = Color.Black)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF6A0DAD))
        )
    }
}

// 🧾 Export Tasks to CSV
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
