package com.st10028058.focusflowv2.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.st10028058.focusflowv2.ui.auth.LoginScreen
import com.st10028058.focusflowv2.ui.auth.RegisterScreen
import com.st10028058.focusflowv2.ui.home.HomeScreen
import com.st10028058.focusflowv2.ui.screens.AddTaskScreen
import com.st10028058.focusflowv2.ui.screens.SettingsScreen
import com.st10028058.focusflowv2.ui.screens.TaskScreen
import com.st10028058.focusflowv2.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(navController: NavHostController) {
    // Observe the current route to conditionally show the bottom navigation
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    // ✅ Shared ViewModel for all task-related screens
    val taskViewModel: TaskViewModel = viewModel()

    Scaffold(
        bottomBar = {
            // ✅ Show bottom bar only on main app screens
            if (currentRoute in listOf(
                    Routes.Home,
                    Routes.Tasks,
                    Routes.Updates,
                    Routes.Settings
                )
            ) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Login,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 🔐 Authentication
            composable(Routes.Login) { LoginScreen(navController) }
            composable(Routes.Register) { RegisterScreen(navController) }

            // 🏠 Home
            composable(Routes.Home) { HomeScreen(navController) }

            // ✅ Task management (shared ViewModel)
            composable(Routes.Tasks) { TaskScreen(navController, taskViewModel) }
            composable(Routes.AddTask) { AddTaskScreen(navController, taskViewModel) }



            // ⚙️ Settings
            composable(Routes.Settings) { SettingsScreen(navController) }

            // 🚧 Placeholder for future updates
            composable(Routes.Updates) {
                Text(
                    text = "Updates coming soon...",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
