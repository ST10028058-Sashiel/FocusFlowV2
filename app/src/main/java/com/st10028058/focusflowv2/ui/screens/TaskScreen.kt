package com.st10028058.focusflowv2.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.st10028058.focusflowv2.data.Task
import com.st10028058.focusflowv2.ui.nav.Routes
import com.st10028058.focusflowv2.viewmodel.TaskViewModel
import kotlin.math.max

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    navController: NavController,
    viewModel: TaskViewModel = viewModel()
) {
    val tasks by viewModel.tasks.collectAsState()
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val listState = rememberLazyListState()

    // Load user tasks
    LaunchedEffect(userId) {
        if (userId != null) viewModel.fetchTasks()
    }

    // 🌈 Gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF6A0DAD), Color(0xFF8B2BE2))
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.AddTask) },
                    containerColor = Color(0xFFE91E63),
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(16.dp)
                        .shadow(8.dp, CircleShape)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Task", tint = Color.White)
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // 🌅 Header Section
                    Text(
                        text = "Hey there 👋",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(
                        text = "Here's your focus for today!",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 📊 Stats Bar
                    if (tasks.isNotEmpty()) {
                        TaskStatsBar(tasks)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 📝 Scrollable Task List
                    Box(modifier = Modifier.fillMaxSize()) {
                        this@Column.AnimatedVisibility(
                            visible = tasks.isNotEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Box {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(bottom = 120.dp)
                                ) {
                                    items(tasks) { task ->
                                        FancyTaskCard(
                                            task = task,
                                            onEdit = {
                                                task._id?.let { id ->
                                                    navController.navigate("${Routes.EditTask}/$id")
                                                }
                                            },
                                            onDelete = {
                                                task._id?.let { id ->
                                                    viewModel.deleteTask(id)
                                                    Toast.makeText(
                                                        context,
                                                        "Task deleted",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    viewModel.fetchTasks()
                                                }
                                            }
                                        )
                                    }
                                }

                                // 📍 Scroll indicator (right side)
                                val scrollProgress by derivedStateOf {
                                    val totalItems = listState.layoutInfo.totalItemsCount
                                    val visibleItems = listState.layoutInfo.visibleItemsInfo.size
                                    val firstVisible = listState.firstVisibleItemIndex
                                    if (totalItems <= visibleItems) 0f
                                    else firstVisible.toFloat() / max(totalItems - visibleItems, 1)
                                }

                                val animatedProgress by animateFloatAsState(targetValue = scrollProgress, label = "")

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 6.dp)
                                        .fillMaxHeight()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight(animatedProgress)
                                            .width(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFFE91E63).copy(alpha = 0.7f))
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }
                        }

                        // 🌱 Empty state when no tasks
                        this@Column.AnimatedVisibility(
                            visible = tasks.isEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No tasks yet 🌱\nStart your journey to productivity!",
                                    color = Color.White.copy(alpha = 0.9f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 💫 Stats bar showing quick summary
@Composable
fun TaskStatsBar(tasks: List<Task>) {
    val total = tasks.size
    val high = tasks.count { it.priority.equals("High", ignoreCase = true) }
    val normal = tasks.count { it.priority.equals("Normal", ignoreCase = true) }
    val low = tasks.count { it.priority.equals("Low", ignoreCase = true) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatChip("Total", total, Color.White)
        StatChip("High", high, Color(0xFFFF5252))
        StatChip("Normal", normal, Color(0xFFFFC107))
        StatChip("Low", low, Color(0xFF4CAF50))
    }
}

// 📊 Chip for task stats
@Composable
fun StatChip(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = color.copy(alpha = 0.9f), fontWeight = FontWeight.SemiBold)
        Text(
            text = count.toString(),
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

// 🎨 Task Card Design
@Composable
fun FancyTaskCard(
    task: Task,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val borderColor = when (task.priority.lowercase()) {
        "high" -> Color(0xFFFF5252)
        "normal" -> Color(0xFFFFC107)
        "low" -> Color(0xFF4CAF50)
        else -> Color.LightGray
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this task?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(borderColor.copy(alpha = 0.1f), Color.Transparent)
                    )
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Priority: ${task.priority}",
                    color = borderColor,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(4.dp))
                if (task.location?.isNotBlank() == true) {
                    Text("📍 ${task.location}", style = MaterialTheme.typography.bodySmall)
                }

                if (task.startTime != null) {
                    val start =
                        android.text.format.DateFormat.format("EEE, d MMM HH:mm", task.startTime)
                    val end =
                        android.text.format.DateFormat.format("HH:mm", task.endTime ?: task.startTime)
                    Text("$start - $end", style = MaterialTheme.typography.bodySmall)
                }

                if ((task.reminderOffsetMinutes ?: 0) > 0) {
                    Text(
                        "🔔 Reminder: ${task.reminderOffsetMinutes} mins before",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color(0xFF6A0DAD))
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }
}
