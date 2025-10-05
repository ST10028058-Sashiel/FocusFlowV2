package com.st10028058.focusflowv2.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Filled.Home)
    object Tasks : BottomNavItem("tasks", "Tasks", Icons.Filled.List)
    object Updates : BottomNavItem("updates", "Updates", Icons.Filled.Edit)
    object Settings : BottomNavItem("settings", "Settings", Icons.Filled.Settings)
}
