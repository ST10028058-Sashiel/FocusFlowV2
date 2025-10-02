package com.st10028058.focusflowv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.navigation.compose.rememberNavController
import com.st10028058.focusflowv2.ui.nav.AppNavigation
import com.st10028058.focusflowv2.ui.theme.FocusFlowV2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusFlowV2Theme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}
