package com.st10028058.focusflowv2.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
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
import com.st10028058.focusflowv2.utils.NetworkUtils
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
    var isOnline by remember { mutableStateOf(NetworkUtils.isNetworkAvailable(context)) }
    
    // Update network status periodically
    LaunchedEffect(Unit) {
        while (true) {
            isOnline = NetworkUtils.isNetworkAvailable(context)
            delay(5000) // Check every 5 seconds
        }
    }

    var selectedPriority by remember { mutableStateOf("All") }
    var selectedSort by remember { mutableStateOf("Newest First") }
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    // Filter by priority and search query
    val filteredTasks = remember(tasks, selectedPriority, searchQuery) {
        var filtered = if (selectedPriority == "All") tasks
        else tasks.filter { it.priority.equals(selectedPriority, ignoreCase = true) }
        
        // Apply search filter
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { 
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.location?.contains(searchQuery, ignoreCase = true) == true
            }
        }
        
        filtered
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

    // Theme-aware header gradient
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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
                    }
                    
                    // Offline indicator / Sync button
                    if (!isOnline) {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = "Offline",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        IconButton(
                            onClick = {
                                isRefreshing = true
                                viewModel.syncTasks()
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(1000)
                                    isRefreshing = false
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = "Sync",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Search Bar with enhanced styling
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search tasks...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.25f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.18f),
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                        focusedLabelColor = Color.White.copy(alpha = 0.95f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.75f),
                        cursorColor = Color.White
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                if (tasks.isNotEmpty()) {
                    TaskStatsBar(tasks)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Share All Outstanding Tasks Button
                    val outstandingTasks = tasks.filter { it.completed != true }
                    if (outstandingTasks.isNotEmpty()) {
                        Button(
                            onClick = {
                                shareTasks(context, outstandingTasks, "All Outstanding Tasks")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = colors.secondary.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 6.dp
                            )
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share All Outstanding Tasks (${outstandingTasks.size})")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
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

                // Task List with Pull to Refresh
                AnimatedVisibility(
                    visible = sortedTasks.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        if (listState.firstVisibleItemIndex == 0 && dragAmount.y > 0) {
                                            isRefreshing = true
                                        }
                                    }
                                },
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                        items(
                            items = sortedTasks,
                            key = { it._id ?: it.title }
                        ) { task ->
                            androidx.compose.animation.AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.9f),
                                exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.9f)
                            ) {
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
                                },
                                onShare = {
                                    if (task.completed != true) {
                                        shareTasks(context, listOf(task), "Task")
                                    }
                                }
                            )
                            }
                        }
                        }
                        
                        // Pull to refresh handler
                        LaunchedEffect(listState.isScrollInProgress) {
                            if (!listState.isScrollInProgress && listState.firstVisibleItemIndex == 0) {
                                if (isRefreshing) {
                                    viewModel.fetchTasks()
                                    kotlinx.coroutines.delay(1000)
                                    isRefreshing = false
                                }
                            }
                        }
                        
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(16.dp),
                                color = Color.White
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "🔍 No tasks found" else "🌱 No tasks yet",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) 
                                    "Try a different search term" 
                                else 
                                    "Tap the + button to add your first task!",
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
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
    onDelete: () -> Unit,
    onShare: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isDark = colors.surface.luminance() < 0.5f
    
    // Animation for card elevation on hover/press
    val elevation by animateFloatAsState(
        targetValue = if (showDeleteDialog) 12f else 6f,
        animationSpec = tween(200), label = "elevation"
    )

    val priorityTint = when (task.priority.lowercase()) {
        "high" -> Color(0xFFFF4D4D)
        "low"  -> Color(0xFF2ECC71)
        else   -> Color(0xFFFFB020)
    }
    val overlayAlpha = if (isDark) 0.24f else 0.10f
    val isCompleted = task.completed == true

    // ⬇️ Brought back from your old code: confirmation dialog
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
                ) { Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation.dp, RoundedCornerShape(20.dp), spotColor = colors.primary.copy(alpha = 0.1f)),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface,
            contentColor = colors.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        border = if (isCompleted) null else BorderStroke(0.5.dp, colors.outline.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(priorityTint)
            )

            val metaColor = if (isDark) Color.White else Color(0xFF111111)

            Column(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(priorityTint.copy(alpha = overlayAlpha), Color.Transparent)
                        )
                    )
                    .padding(18.dp)
                    .alpha(if (isCompleted) 0.75f else 1f)
                    .weight(1f)
            ) {
                Text(
                    text = task.title,
                    color = if (isDark) Color.White else Color(0xFF111111),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium.copy(
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Text(
                    text = "Priority: ${task.priority}",
                    color = priorityTint,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                if (task.startTime != null) {
                    val start = android.text.format.DateFormat.format("EEE, d MMM yyyy HH:mm", task.startTime)
                    val end = android.text.format.DateFormat.format("HH:mm", task.endTime ?: task.startTime)
                    Text(
                        text = "🕒 $start → $end",
                        color = metaColor,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                if ((task.reminderOffsetMinutes ?: 0) > 0) {
                    Text(
                        text = "🔔 Reminder: ${task.reminderOffsetMinutes} mins before",
                        color = metaColor,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                if (!task.location.isNullOrBlank()) {
                    Text(
                        text = "📍 Location: ${task.location}",
                        color = metaColor,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.End, 
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Show share button only for incomplete tasks
                    if (task.completed != true) {
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Filled.Share, 
                                contentDescription = "Share", 
                                tint = colors.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Filled.Edit, 
                            contentDescription = "Edit", 
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete, 
                            contentDescription = "Delete", 
                            tint = colors.error,
                            modifier = Modifier.size(20.dp)
                        )
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
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White,
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
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
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White,
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
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

/* -------------------- Stats -------------------- */

@Composable
fun TaskStatsBar(tasks: List<Task>) {
    val total = tasks.size
    val high = tasks.count { it.priority.equals("High", true) }
    val normal = tasks.count { it.priority.equals("Normal", true) }
    val low = tasks.count { it.priority.equals("Low", true) }

    val labelColor = Color(0xFF111111)
    val highTone   = Color(0xFFFF4D4D)
    val normalTone = Color(0xFFFFB020)
    val lowTone    = Color(0xFF2ECC71)

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

/* -------------------- Share Functions -------------------- */

/**
 * Share tasks as formatted text
 */
fun shareTasks(context: android.content.Context, tasks: List<Task>, title: String) {
    if (tasks.isEmpty()) {
        Toast.makeText(context, "No tasks to share", Toast.LENGTH_SHORT).show()
        return
    }
    
    val shareText = formatTasksForSharing(tasks, title)
    
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "$title - FocusFlow Tasks")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    
    context.startActivity(Intent.createChooser(shareIntent, "Share Tasks via"))
}

/**
 * Format tasks as readable text for sharing
 */
fun formatTasksForSharing(tasks: List<Task>, title: String): String {
    val dateFormat = java.text.SimpleDateFormat("EEE, d MMM yyyy HH:mm", java.util.Locale.getDefault())
    val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    
    val builder = StringBuilder()
    builder.append("📋 $title\n")
    builder.append("=".repeat(50)).append("\n\n")
    
    tasks.forEachIndexed { index, task ->
        builder.append("${index + 1}. ${task.title}\n")
        builder.append("   Priority: ${task.priority}\n")
        
        if (task.startTime != null) {
            val startDate = dateFormat.format(java.util.Date(task.startTime))
            val endTime = task.endTime?.let { timeFormat.format(java.util.Date(it)) }
            if (endTime != null) {
                builder.append("   📅 $startDate → $endTime\n")
            } else {
                builder.append("   📅 $startDate\n")
            }
        }
        
        if (!task.location.isNullOrBlank()) {
            builder.append("   📍 Location: ${task.location}\n")
        }
        
        if ((task.reminderOffsetMinutes ?: 0) > 0) {
            builder.append("   🔔 Reminder: ${task.reminderOffsetMinutes} mins before\n")
        }
        
        builder.append("\n")
    }
    
    builder.append("Generated by FocusFlow App")
    return builder.toString()
}
