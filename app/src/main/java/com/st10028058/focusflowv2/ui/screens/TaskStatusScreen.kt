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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    LaunchedEffect(Unit) {
        viewModel.fetchTasks()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF6A0DAD), Color(0xFF8B2BE2))
                )
            )
            .padding(16.dp)
    ) {
        Column {
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

            // 🔸 Outstanding Tasks
            Text(
                "⏳ Outstanding Tasks",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )

            AnimatedVisibility(
                visible = outstandingTasks.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(outstandingTasks) { task ->
                        TaskStatusCard(
                            task = task,
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
                }
            }

            if (outstandingTasks.isEmpty()) {
                Text(
                    text = "🎉 All caught up! No outstanding tasks.",
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Completed Tasks
            Text(
                "✅ Completed Tasks",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )

            AnimatedVisibility(
                visible = completedTasks.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(completedTasks) { task ->
                        TaskStatusCard(
                            task = task,
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
                }
            }

            if (completedTasks.isEmpty()) {
                Text(
                    text = "No tasks completed yet.",
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun TaskStatusCard(task: Task, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (task.location?.isNotBlank() == true) {
                    Text(
                        text = "📍 ${task.location}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (task.priority.isNotBlank()) {
                    val priorityColor = when (task.priority.lowercase()) {
                        "high" -> Color(0xFFFF5252)
                        "normal" -> Color(0xFFFFC107)
                        "low" -> Color(0xFF4CAF50)
                        else -> Color.Gray
                    }
                    Text(
                        text = "Priority: ${task.priority}",
                        color = priorityColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            IconButton(onClick = { onCheckedChange(!task.completed) }) {
                Icon(
                    imageVector = if (task.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle Completed",
                    tint = if (task.completed) Color(0xFF4CAF50) else Color(0xFF6A0DAD)
                )
            }
        }
    }
}
