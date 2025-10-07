package com.st10028058.focusflowv2.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.st10028058.focusflowv2.ui.screens.TaskStatusScreen
import com.st10028058.focusflowv2.ui.screens.UpdateTaskScreen
import com.st10028058.focusflowv2.viewmodel.SettingsViewModel
import com.st10028058.focusflowv2.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(navController: NavHostController, settingsViewModel: SettingsViewModel) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    val taskViewModel: TaskViewModel = viewModel()

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf(
                    Routes.Home,
                    Routes.Tasks,
                    Routes.TaskStatus,
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
            // Authentication
            composable(Routes.Login) { LoginScreen(navController) }
            composable(Routes.Register) { RegisterScreen(navController) }

            // Home
            composable(Routes.Home) { HomeScreen(navController) }

            // Tasks
            composable(Routes.Tasks) { TaskScreen(navController, taskViewModel) }
            composable(Routes.AddTask) { AddTaskScreen(navController, taskViewModel) }

            // ✅ Edit Task (NEW — FIXED)
            composable("edit_task/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                UpdateTaskScreen(taskId = taskId, navController = navController, viewModel = taskViewModel)
            }

            // Task Status
            composable(Routes.TaskStatus) { TaskStatusScreen(navController, taskViewModel) }

            // Settings
            composable(Routes.Settings) {
                SettingsScreen(
                    navController = navController,
                    taskViewModel = taskViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
