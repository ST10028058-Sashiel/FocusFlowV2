package com.st10028058.focusflowv2.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.st10028058.focusflowv2.data.Task
import com.st10028058.focusflowv2.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskStatusScreen(
    navController: NavController,
    viewModel: TaskViewModel = viewModel()
) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsState()

    val outstandingTasks = tasks.filter { !it.completed }
    val completedTasks = tasks.filter { it.completed }

    // Refresh tasks when entering screen
    LaunchedEffect(Unit) { viewModel.fetchTasks() }

    val colors = MaterialTheme.colorScheme

    // Theme-aware purple gradient background
    val gradient = if (colors.surface.luminance() < 0.5f) {
        listOf(colors.primary.copy(alpha = 0.95f), colors.surfaceVariant)
    } else {
        listOf(colors.primary, colors.primary.copy(alpha = 0.75f))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradient))
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 🔹 Header Section
            item {
                Text(
                    text = "Task Status 📋",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Manage your completed and pending tasks",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 🔸 Outstanding Tasks Section
            item {
                Text(
                    "⏳ Outstanding Tasks",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (outstandingTasks.isNotEmpty()) {
                items(outstandingTasks) { task ->
                    TaskStatusCard(
                        task = task,
                        dimCompleted = false,
                        onCheckedChange = { isChecked ->
                            val updatedTask = task.copy(completed = isChecked)
                            viewModel.updateTask(task._id ?: "", updatedTask)
                            Toast.makeText(
                                context,
                                if (isChecked) "Marked as completed ✅" else "Marked as pending ⏳",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            } else {
                item {
                    Text(
                        text = "🎉 All caught up! No outstanding tasks.",
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ✅ Completed Tasks Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "✅ Completed Tasks",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (completedTasks.isNotEmpty()) {
                items(completedTasks) { task ->
                    TaskStatusCard(
                        task = task,
                        dimCompleted = true,
                        onCheckedChange = { isChecked ->
                            val updatedTask = task.copy(completed = isChecked)
                            viewModel.updateTask(task._id ?: "", updatedTask)
                            Toast.makeText(
                                context,
                                if (isChecked) "Marked as completed ✅" else "Marked as pending ⏳",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            } else {
                item {
                    Text(
                        text = "No tasks completed yet.",
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Bottom Padding
            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}

@Composable
fun TaskStatusCard(
    task: Task,
    dimCompleted: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val isDark = colors.surface.luminance() < 0.5f

    // Priority tint & overlay strength (clearer in dark mode)
    val priorityTint = when (task.priority.lowercase()) {
        "high" -> Color(0xFFFF4D4D)     // vivid red
        "low" -> Color(0xFF2ECC71)      // green
        else -> Color(0xFFFFB020)       // amber for "Normal"
    }
    val overlayAlpha = if (isDark) 0.22f else 0.10f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 84.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface,
            contentColor = colors.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left priority stripe
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(priorityTint)
            )

            Row(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(priorityTint.copy(alpha = overlayAlpha), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .alpha(if (dimCompleted) 0.78f else 1f)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        color = colors.onSurface,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    // Priority line colored by priority
                    Text(
                        text = "Priority: ${task.priority}",
                        color = priorityTint,
                        style = MaterialTheme.typography.bodySmall
                    )

                    task.location?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = "📍 $it",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                IconButton(onClick = { onCheckedChange(!task.completed) }) {
                    Icon(
                        imageVector = if (task.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle Completed",
                        tint = if (task.completed) Color(0xFF4CAF50) else colors.primary
                    )
                }
            }
        }
    }
}
