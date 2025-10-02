package com.st10028058.focusflowv2.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.st10028058.focusflowv2.ui.home.HomeScreen
import com.st10028058.focusflowv2.ui.screens.SettingsScreen
import androidx.compose.material3.*
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AppNavigation(navController: NavHostController) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding) // ✅ fixed
        ) {
            composable("home") { HomeScreen() }
            composable("tasks") { Text("Tasks Screen Coming Soon") }
            composable("updates") { Text("Updates Screen Coming Soon") }
            composable("settings") { SettingsScreen() }
        }
    }
}
