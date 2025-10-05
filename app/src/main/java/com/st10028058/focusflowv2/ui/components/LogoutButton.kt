package com.st10028058.focusflowv2.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LogoutButton(onLogout: () -> Unit) {
    Button(
        onClick = {
            FirebaseAuth.getInstance().signOut()
            onLogout()
        },
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Logout")
    }
}
