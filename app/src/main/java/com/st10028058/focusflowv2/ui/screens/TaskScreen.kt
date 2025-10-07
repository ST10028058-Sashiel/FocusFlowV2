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
import androidx.compose.ui.graphics.luminance
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
    val colors = MaterialTheme.colorScheme
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

    // Theme-aware purple header gradient
    val backdrop = if (colors.surface.luminance() < 0.5f) {
        Brush.verticalGradient(listOf(colors.primary.copy(alpha = 0.95f), colors.surfaceVariant))
    } else {
        Brush.verticalGradient(listOf(colors.primary, colors.primary.copy(alpha = 0.75f)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backdrop)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.AddTask) },
                    containerColor = colors.secondary,
                    contentColor = colors.onSecondary,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(16.dp)
                        .shadow(8.dp, CircleShape)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Task")
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

/* -------------------- Card -------------------- */

@Composable
fun FancyTaskCard(
    task: Task,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isDark = colors.surface.luminance() < 0.5f

    val priorityTint = when (task.priority.lowercase()) {
        "high" -> Color(0xFFFF4D4D)
        "low"  -> Color(0xFF2ECC71)
        else   -> Color(0xFFFFB020)
    }
    val overlayAlpha = if (isDark) 0.24f else 0.10f
    val isCompleted = task.completed == true

    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(priorityTint)
            )

            // 🔁 Change meta text color to black in light mode, white in dark
            val metaColor = if (isDark) Color.White else Color(0xFF111111)

            Column(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(priorityTint.copy(alpha = overlayAlpha), Color.Transparent)
                        )
                    )
                    .padding(16.dp)
                    .alpha(if (isCompleted) 0.75f else 1f)
                    .weight(1f)
            ) {
                // Title stays bold; use black in light mode, white in dark
                Text(
                    text = task.title,
                    color = if (isDark) Color.White else Color(0xFF111111),
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                    )
                )

                Text(
                    text = "Priority: ${task.priority}",
                    color = priorityTint,
                    fontWeight = FontWeight.SemiBold
                )

                if (task.startTime != null) {
                    val start = android.text.format.DateFormat.format("EEE, d MMM yyyy HH:mm", task.startTime)
                    val end = android.text.format.DateFormat.format("HH:mm", task.endTime ?: task.startTime)
                    Text(
                        text = "🕒 $start → $end",
                        color = metaColor,
                        fontWeight = FontWeight.Bold   // ⬅️ bold in light mode now
                    )
                }

                if ((task.reminderOffsetMinutes ?: 0) > 0) {
                    Text(
                        text = "🔔 Reminder: ${task.reminderOffsetMinutes} mins before",
                        color = metaColor,
                        fontWeight = FontWeight.Bold   // ⬅️ bold
                    )
                }

                if (!task.location.isNullOrBlank()) {
                    Text(
                        text = "📍 Location: ${task.location}",
                        color = metaColor,
                        fontWeight = FontWeight.Bold   // ⬅️ bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = colors.primary)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = colors.error)
                    }
                }
            }
        }
    }
}

/* -------------------- Filters -------------------- */

@Composable
fun PriorityFilterDropdown(
    selectedPriority: String,
    onPrioritySelected: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Text("Filter: $selectedPriority", color = Color.White)
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        listOf("All", "High", "Normal", "Low").forEach { option ->
            DropdownMenuItem(
                text = { Text(option, color = colors.onSurface) },
                onClick = {
                    onPrioritySelected(option)
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun SortDropdown(
    selectedSort: String,
    onSortSelected: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Text("Sort: $selectedSort", color = Color.White)
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        listOf("Newest First", "Oldest First", "Priority (High → Low)").forEach { option ->
            DropdownMenuItem(
                text = { Text(option, color = colors.onSurface) },
                onClick = {
                    onSortSelected(option)
                    expanded = false
                }
            )
        }
    }
}

/* -------------------- Stats (white card, black labels, colored numbers) -------------------- */

@Composable
fun TaskStatsBar(tasks: List<Task>) {
    val total = tasks.size
    val high = tasks.count { it.priority.equals("High", true) }
    val normal = tasks.count { it.priority.equals("Normal", true) }
    val low = tasks.count { it.priority.equals("Low", true) }

    val labelColor = Color(0xFF111111)      // black-ish label text
    val highTone   = Color(0xFFFF4D4D)      // red
    val normalTone = Color(0xFFFFB020)      // amber
    val lowTone    = Color(0xFF2ECC71)      // green

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatChip("Total",  total,  labelColor, labelColor, modifier = Modifier.weight(1f))
            StatChip("High",   high,   labelColor, highTone,   modifier = Modifier.weight(1f))
            StatChip("Normal", normal, labelColor, normalTone, modifier = Modifier.weight(1f))
            StatChip("Low",    low,    labelColor, lowTone,    modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatChip(
    label: String,
    count: Int,
    labelColor: Color,
    numberColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = labelColor, fontWeight = FontWeight.SemiBold)
        Text(
            text = count.toString(),
            color = numberColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )
    }
}
