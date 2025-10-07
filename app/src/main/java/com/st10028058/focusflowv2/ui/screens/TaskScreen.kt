package com.st10028058.focusflowv2.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.st10028058.focusflowv2.data.Task
import com.st10028058.focusflowv2.ui.nav.Routes
import com.st10028058.focusflowv2.viewmodel.TaskViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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

    var selectedPriority by remember { mutableStateOf("All") }
    var selectedSort by remember { mutableStateOf("Newest First") }

    // Filter by priority
    val filteredTasks = remember(tasks, selectedPriority) {
        if (selectedPriority == "All") tasks
        else tasks.filter { it.priority.equals(selectedPriority, ignoreCase = true) }
    }

    // Sort tasks
    val sortedTasks = remember(filteredTasks, selectedSort) {
        when (selectedSort) {
            "Newest First" -> filteredTasks.sortedByDescending { it.startTime ?: 0 }
            "Oldest First" -> filteredTasks.sortedBy { it.startTime ?: 0 }
            "Priority (High → Low)" -> filteredTasks.sortedBy {
                when (it.priority.lowercase()) {
                    "high" -> 1
                    "normal" -> 2
                    "low" -> 3
                    else -> 4
                }
            }
            else -> filteredTasks
        }
    }

    // Load tasks
    LaunchedEffect(userId) {
        if (userId != null) viewModel.fetchTasks()
    }

    // Background gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF6A0DAD), Color(0xFF8B2BE2))))
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
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
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

                if (tasks.isNotEmpty()) {
                    TaskStatsBar(tasks)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PriorityFilterDropdown(
                            selectedPriority = selectedPriority,
                            onPrioritySelected = { selectedPriority = it }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SortDropdown(
                            selectedSort = selectedSort,
                            onSortSelected = { selectedSort = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Task List
                AnimatedVisibility(
                    visible = sortedTasks.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(sortedTasks) { task ->
                            FancyTaskCard(
                                task = task,
                                onEdit = {
                                    task._id?.let { id ->
                                        // ✅ Fixed navigation crash
                                        navController.navigate("edit_task/$id")
                                    }
                                },
                                onDelete = {
                                    task._id?.let { id ->
                                        viewModel.deleteTask(id)
                                        Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
                                        viewModel.fetchTasks()
                                    }
                                }
                            )
                        }
                    }
                }

                // Empty State
                AnimatedVisibility(
                    visible = sortedTasks.isEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No tasks for $selectedPriority priority 🌱",
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// 🎨 Fancy Task Card (with all details)
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

    val isCompleted = task.completed == true

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this task?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
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
        Column(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(borderColor.copy(alpha = 0.1f), Color.Transparent)
                    )
                )
                .padding(16.dp)
                .alpha(if (isCompleted) 0.6f else 1f)
        ) {
            Text(
                text = task.title,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                )
            )

            Text("Priority: ${task.priority}", color = borderColor)

            if (task.startTime != null) {
                val start = android.text.format.DateFormat.format("EEE, d MMM yyyy HH:mm", task.startTime)
                val end = android.text.format.DateFormat.format("HH:mm", task.endTime ?: task.startTime)
                Text("🕒 $start → $end", color = Color.DarkGray)
            }

            if ((task.reminderOffsetMinutes ?: 0) > 0) {
                Text("🔔 Reminder: ${task.reminderOffsetMinutes} mins before", color = Color.Gray)
            }

            if (!task.location.isNullOrBlank()) {
                Text("📍 Location: ${task.location}", color = Color.Gray)
            }

            Text(
                text = if (isCompleted) "✅ Completed" else "⌛ Pending",
                color = if (isCompleted) Color(0xFF4CAF50) else Color(0xFFFF9800),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
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

// 🔽 Priority Filter Dropdown
@Composable
fun PriorityFilterDropdown(
    selectedPriority: String,
    onPrioritySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
    ) {
        Text("Filter: $selectedPriority", color = Color.White)
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        listOf("All", "High", "Normal", "Low").forEach { option ->
            DropdownMenuItem(
                text = { Text(option) },
                onClick = {
                    onPrioritySelected(option)
                    expanded = false
                }
            )
        }
    }
}

// 🔽 Sort Dropdown
@Composable
fun SortDropdown(
    selectedSort: String,
    onSortSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
    ) {
        Text("Sort: $selectedSort", color = Color.White)
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        listOf("Newest First", "Oldest First", "Priority (High → Low)").forEach { option ->
            DropdownMenuItem(
                text = { Text(option) },
                onClick = {
                    onSortSelected(option)
                    expanded = false
                }
            )
        }
    }
}

// 📊 Task Stats Bar
@Composable
fun TaskStatsBar(tasks: List<Task>) {
    val total = tasks.size
    val high = tasks.count { it.priority.equals("High", true) }
    val normal = tasks.count { it.priority.equals("Normal", true) }
    val low = tasks.count { it.priority.equals("Low", true) }

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

// 🔹 Stat Chip
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
