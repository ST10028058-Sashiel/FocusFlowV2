package com.st10028058.focusflowv2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.st10028058.focusflowv2.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel()
) {
    val darkMode by viewModel.darkMode.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val displayName by viewModel.displayName.collectAsState()

    var nameState by remember { mutableStateOf(TextFieldValue(displayName)) }

    // 🌈 Background that adapts to light/dark mode
    val backgroundColor = if (darkMode) Color(0xFF121212) else Color(0xFFF7F5FA)
    val textColor = if (darkMode) Color.White else Color.Black

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 🟣 Header
        Text(
            "⚙ Settings",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        )

        // 👤 Profile Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (darkMode) Color(0xFF1E1E1E) else Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Profile Settings",
                    color = if (darkMode) Color(0xFFBB86FC) else Color(0xFF6A1B9A),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )

                OutlinedTextField(
                    value = nameState,
                    onValueChange = { nameState = it },
                    label = { Text("Display Name") },
                    leadingIcon = {
                        Icon(Icons.Default.AccountCircle, contentDescription = "User")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { viewModel.setDisplayName(nameState.text) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (darkMode) Color(0xFFBB86FC) else Color(0xFF8E24AA)
                    )
                ) {
                    Text("Save Name", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ⚙ Preferences
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (darkMode) Color(0xFF1E1E1E) else Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Preferences",
                    color = if (darkMode) Color(0xFFBB86FC) else Color(0xFF6A1B9A),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )

                SettingToggleRow(
                    title = "Dark Mode",
                    icon = Icons.Default.DarkMode,
                    isChecked = darkMode,
                    onToggle = { viewModel.setDarkMode(it) },
                    darkMode = darkMode
                )

                SettingToggleRow(
                    title = "Notifications",
                    icon = Icons.Default.Notifications,
                    isChecked = notificationsEnabled,
                    onToggle = { viewModel.setNotifications(it) },
                    darkMode = darkMode
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🚪 Logout Button
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (darkMode) Color(0xFFCF6679) else Color(0xFFD81B60)
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// 💡 Reusable toggle component for consistency
@Composable
fun SettingToggleRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
    darkMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (darkMode) Color(0xFF2C2C2C) else Color(0xFFF3E5F5),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = title,
                tint = if (darkMode) Color(0xFFBB86FC) else Color(0xFF8E24AA)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                title,
                fontWeight = FontWeight.Medium,
                color = if (darkMode) Color.White else Color.Black
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = if (darkMode) Color(0xFFBB86FC) else Color(0xFF8E24AA)
            )
        )
    }
}
