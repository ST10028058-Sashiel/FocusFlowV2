package com.st10028058.focusflowv2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.st10028058.focusflowv2.ui.nav.AppNavigation
import com.st10028058.focusflowv2.ui.theme.FocusFlowV2Theme
import com.st10028058.focusflowv2.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔔 Request POST_NOTIFICATIONS permission on Android 13+
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                // Optional: Handle permission result
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val isDarkMode by settingsViewModel.darkMode.collectAsState()

            FocusFlowV2Theme(darkTheme = isDarkMode) {
                val navController = rememberNavController()

                // 🔄 Pass settingsViewModel to AppNavigation if needed
                AppNavigation(
                    navController = navController,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
