package com.st10028058.focusflowv2.ui.nav

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.st10028058.focusflowv2.viewmodel.TaskViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun BottomNavigationBar(
    navController: NavController,
    viewModel: TaskViewModel = viewModel()
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Tasks,
        BottomNavItem.Updates,
        BottomNavItem.Settings
    )

    // Observe current navigation destination
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 🧠 Get outstanding task count for badge
    val tasks by viewModel.tasks.collectAsState()
    val outstandingCount = tasks.count { it.completed != true }

    NavigationBar(containerColor = Color.White) {
        items.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                icon = {
                    if (item is BottomNavItem.Updates && outstandingCount > 0) {
                        // 🔔 Show badge for Updates tab
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = Color(0xFFE91E63), // Pink badge
                                    contentColor = Color.White
                                ) {
                                    Text(outstandingCount.toString())
                                }
                            }
                        ) {
                            Icon(item.icon, contentDescription = item.label, tint = Color.Black)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.label, tint = Color.Black)
                    }
                },
                label = {
                    Text(
                        item.label,
                        color = if (selected) Color(0xFFE91E63) else Color.Black
                    )
                },
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
