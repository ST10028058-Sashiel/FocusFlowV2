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

    // current destination
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // outstanding tasks for badge
    val tasks by viewModel.tasks.collectAsState()
    val outstandingCount = tasks.count { it.completed != true }

    // keep bar light in both themes
    NavigationBar(containerColor = Color.White) {
        items.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    val iconComposable: @Composable () -> Unit = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = Color.Black // readable on white
                        )
                    }

                    if (item is BottomNavItem.Updates && outstandingCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = Color(0xFFE91E63), // pink badge
                                    contentColor = Color.White
                                ) { Text(outstandingCount.toString()) }
                            }
                        ) { iconComposable() }
                    } else {
                        iconComposable()
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (selected) Color(0xFFE91E63) else Color.Black // pink when selected
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = Color(0xFFE91E63),
                    unselectedIconColor = Color.Black,
                    unselectedTextColor = Color.Black,
                    // purple translucent pill so it never appears black in dark mode
                    indicatorColor = Color(0xFF6A0DAD).copy(alpha = 0.18f)
                )
            )
        }
    }
}
