package com.st10028058.focusflowv2.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.st10028058.focusflowv2.data.Task
import com.st10028058.focusflowv2.notifications.TaskNotificationManager
import com.st10028058.focusflowv2.notifications.TaskReminderScheduler
import com.st10028058.focusflowv2.ui.nav.Routes
import com.st10028058.focusflowv2.viewmodel.TaskViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    navController: NavController,
    viewModel: TaskViewModel
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Initialize with current date and time
    val now = Calendar.getInstance()
    val todayAtMidnight = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val currentTime = now.timeInMillis
    val oneHourLater = Calendar.getInstance().apply {
        add(Calendar.HOUR_OF_DAY, 1)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Normal") }
    var allDay by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(todayAtMidnight.timeInMillis) }
    var startTime by remember { mutableStateOf<Long?>(currentTime) }
    var endTime by remember { mutableStateOf<Long?>(oneHourLater) }
    var reminderOffset by remember { mutableStateOf(10) }
    var location by remember { mutableStateOf("") }
    var showLocationDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val colors = MaterialTheme.colorScheme

    // Theme-aware gradient behind the screen header
    val backdrop = Brush.verticalGradient(
        if (colors.surface.luminance() < 0.5f)
            listOf(colors.primary.copy(alpha = 0.95f), colors.surfaceVariant)
        else
            listOf(colors.primary, colors.primary.copy(alpha = 0.75f))
    )

    // Consistent, high-contrast field colors for both themes
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = colors.surface,
        unfocusedContainerColor = colors.surface,
        disabledContainerColor = colors.surface.copy(alpha = 0.75f),

        focusedTextColor = colors.onSurface,
        unfocusedTextColor = colors.onSurface,
        disabledTextColor = colors.onSurface.copy(alpha = 0.6f),

        focusedBorderColor = colors.primary,
        unfocusedBorderColor = colors.outline,
        disabledBorderColor = colors.outline,

        focusedLabelColor = colors.onSurfaceVariant,
        unfocusedLabelColor = colors.onSurfaceVariant,
        disabledLabelColor = colors.onSurfaceVariant.copy(alpha = 0.6f),

        cursorColor = colors.primary,
        selectionColors = TextSelectionColors(
            handleColor = colors.primary,
            backgroundColor = colors.primary.copy(alpha = 0.25f)
        ),

        focusedPlaceholderColor = colors.onSurfaceVariant,
        unfocusedPlaceholderColor = colors.onSurfaceVariant
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backdrop)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        if (title.isBlank() || selectedDate == null) {
                            Toast.makeText(context, "Please set title and date", Toast.LENGTH_SHORT).show()
                            return@FloatingActionButton
                        }

                        val finalStart = if (allDay) selectedDate else startTime ?: selectedDate
                        val finalEnd = if (allDay) selectedDate else endTime ?: finalStart
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                        // First get FCM token, then send task including it
                        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                            val newTask = Task(
                                title = title,
                                priority = priority,
                                allDay = allDay,
                                startTime = finalStart,
                                endTime = finalEnd,
                                location = if (location.isBlank()) null else location,
                                reminderOffsetMinutes = reminderOffset,
                                userId = uid,
                                fcmToken = token
                            )

                            CoroutineScope(Dispatchers.Main).launch {
                                val savedTask = viewModel.addTaskAndReturn(newTask)
                                if (savedTask != null) {
                                    // Show real-time notification that task was added
                                    TaskNotificationManager.showTaskAddedNotification(
                                        context = context,
                                        taskTitle = savedTask.title,
                                        taskPriority = savedTask.priority
                                    )
                                    
                                    // Schedule reminder if task has start time and reminder
                                    if (savedTask.startTime != null && savedTask.reminderOffsetMinutes != null) {
                                        TaskReminderScheduler.schedule(context, savedTask)
                                    }
                                    
                                    Toast.makeText(context, "Task added successfully", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(context, "Failed to save task", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    containerColor = colors.secondary,
                    contentColor = colors.onSecondary,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    ),
                    modifier = Modifier
                        .padding(8.dp)
                        .size(60.dp)
                        .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = colors.secondary.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Filled.Save, contentDescription = "Save", modifier = Modifier.size(28.dp))
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { navController.navigate(Routes.Tasks) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add New Task",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Stay organized and productive 💪",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Form card – uses theme surface/onSurface for contrast
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surface,
                        contentColor = colors.onSurface
                    ),
                    elevation = CardDefaults.cardElevation(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Task Title") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            colors = fieldColors
                        )

                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                            OutlinedTextField(
                                value = priority,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Priority") },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = fieldColors
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                listOf("Low", "Normal", "High").forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text(p) },
                                        onClick = {
                                            priority = p
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        var expandedReminder by remember { mutableStateOf(false) }
                        val reminderOptions = listOf(5, 10, 15, 30, 60)
                        ExposedDropdownMenuBox(
                            expanded = expandedReminder,
                            onExpandedChange = { expandedReminder = it }
                        ) {
                            OutlinedTextField(
                                value = "Remind me $reminderOffset min before",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Reminder Time") },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = fieldColors
                            )
                            ExposedDropdownMenu(
                                expanded = expandedReminder,
                                onDismissRequest = { expandedReminder = false }
                            ) {
                                reminderOptions.forEach { minutes ->
                                    DropdownMenuItem(
                                        text = { Text("$minutes minutes before") },
                                        onClick = {
                                            reminderOffset = minutes
                                            expandedReminder = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = allDay,
                                onCheckedChange = { allDay = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = colors.primary,
                                    uncheckedColor = colors.outline
                                )
                            )
                            Text("All Day Task", fontSize = 14.sp, color = colors.onSurface)
                        }

                        Divider(color = colors.surfaceVariant)

                        if (selectedDate == null) {
                            StyledActionButton("Select Date", colors.primary) {
                                val now = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        calendar.set(year, month, day, 0, 0)
                                        selectedDate = calendar.timeInMillis
                                    },
                                    now.get(Calendar.YEAR),
                                    now.get(Calendar.MONTH),
                                    now.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                        } else {
                            CenteredDateTimeDisplay(
                                label = "Date Chosen:",
                                value = dateFormat.format(Date(selectedDate!!)),
                                onChangeClick = {
                                    val selectedCal = Calendar.getInstance().apply {
                                        timeInMillis = selectedDate!!
                                    }
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, day ->
                                            val updatedCal = Calendar.getInstance().apply {
                                                set(year, month, day, 0, 0, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }
                                            selectedDate = updatedCal.timeInMillis
                                        },
                                        selectedCal.get(Calendar.YEAR),
                                        selectedCal.get(Calendar.MONTH),
                                        selectedCal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                            )
                        }

                        if (!allDay) {
                            if (startTime == null) {
                                StyledActionButton("Select Start Time", colors.primary) {
                                    val now = Calendar.getInstance()
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            val updatedCal = Calendar.getInstance().apply {
                                                timeInMillis = selectedDate ?: System.currentTimeMillis()
                                                set(Calendar.HOUR_OF_DAY, hour)
                                                set(Calendar.MINUTE, minute)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }
                                            startTime = updatedCal.timeInMillis
                                        },
                                        now.get(Calendar.HOUR_OF_DAY),
                                        now.get(Calendar.MINUTE),
                                        true
                                    ).show()
                                }
                            } else {
                                CenteredDateTimeDisplay(
                                    label = "Start Time:",
                                    value = timeFormat.format(Date(startTime!!)),
                                    onChangeClick = {
                                        val startCal = Calendar.getInstance().apply {
                                            timeInMillis = startTime!!
                                        }
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                val updatedCal = Calendar.getInstance().apply {
                                                    timeInMillis = selectedDate ?: System.currentTimeMillis()
                                                    set(Calendar.HOUR_OF_DAY, hour)
                                                    set(Calendar.MINUTE, minute)
                                                    set(Calendar.SECOND, 0)
                                                    set(Calendar.MILLISECOND, 0)
                                                }
                                                startTime = updatedCal.timeInMillis
                                            },
                                            startCal.get(Calendar.HOUR_OF_DAY),
                                            startCal.get(Calendar.MINUTE),
                                            true
                                        ).show()
                                    }
                                )
                            }

                            if (endTime == null) {
                                StyledActionButton("Select End Time", colors.primary) {
                                    val now = Calendar.getInstance()
                                    val defaultHour = if (startTime != null) {
                                        Calendar.getInstance().apply { timeInMillis = startTime!! }.get(Calendar.HOUR_OF_DAY) + 1
                                    } else {
                                        now.get(Calendar.HOUR_OF_DAY) + 1
                                    }
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            val updatedCal = Calendar.getInstance().apply {
                                                timeInMillis = selectedDate ?: System.currentTimeMillis()
                                                set(Calendar.HOUR_OF_DAY, hour)
                                                set(Calendar.MINUTE, minute)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }
                                            endTime = updatedCal.timeInMillis
                                        },
                                        defaultHour,
                                        now.get(Calendar.MINUTE),
                                        true
                                    ).show()
                                }
                            } else {
                                CenteredDateTimeDisplay(
                                    label = "End Time:",
                                    value = timeFormat.format(Date(endTime!!)),
                                    onChangeClick = {
                                        val endCal = Calendar.getInstance().apply {
                                            timeInMillis = endTime!!
                                        }
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                val updatedCal = Calendar.getInstance().apply {
                                                    timeInMillis = selectedDate ?: System.currentTimeMillis()
                                                    set(Calendar.HOUR_OF_DAY, hour)
                                                    set(Calendar.MINUTE, minute)
                                                    set(Calendar.SECOND, 0)
                                                    set(Calendar.MILLISECOND, 0)
                                                }
                                                endTime = updatedCal.timeInMillis
                                            },
                                            endCal.get(Calendar.HOUR_OF_DAY),
                                            endCal.get(Calendar.MINUTE),
                                            true
                                        ).show()
                                    }
                                )
                            }
                        }

                        Divider(color = colors.surfaceVariant)

                        // Location picker
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                label = { Text("Location (optional)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                colors = fieldColors,
                                trailingIcon = {
                                    if (location.isNotBlank()) {
                                        IconButton(onClick = { location = "" }) {
                                            Icon(
                                                Icons.Default.ArrowBack,
                                                contentDescription = "Clear",
                                                modifier = Modifier.rotate(180f)
                                            )
                                        }
                                    }
                                }
                            )
                            
                            // Location picker button
                            Button(
                                onClick = {
                                    // Try to open Maps, if it fails show dialog
                                    try {
                                        val mapsIntent = Intent(Intent.ACTION_VIEW).apply {
                                            data = android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=")
                                            setPackage("com.google.android.apps.maps")
                                        }
                                        if (mapsIntent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(mapsIntent)
                                            Toast.makeText(
                                                context,
                                                "Search for a location in Maps, then enter it in the location field",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            // Fallback: Show dialog
                                            showLocationDialog = true
                                        }
                                    } catch (e: Exception) {
                                        showLocationDialog = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.secondary,
                                    contentColor = colors.onSecondary
                                ),
                                modifier = Modifier.height(56.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = "Pick Location",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        // Location picker dialog
                        if (showLocationDialog) {
                            LocationPickerDialog(
                                currentLocation = location,
                                onLocationSelected = { selectedLocation ->
                                    location = selectedLocation
                                    showLocationDialog = false
                                },
                                onDismiss = { showLocationDialog = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

/**
 * Location Picker Dialog - allows manual entry with common location suggestions
 */
@Composable
fun LocationPickerDialog(
    currentLocation: String,
    onLocationSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchText by remember { mutableStateOf(currentLocation) }
    val colors = MaterialTheme.colorScheme
    
    val commonLocations = listOf(
        "Home", "Office", "Gym", "Restaurant", "Park", "School", "Hospital",
        "Airport", "Hotel", "Shopping Mall", "Library", "Coffee Shop"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pick Location", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Enter location name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface
                    )
                )
                
                Text(
                    "Or select a common location:",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
                
                // Common locations chips
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    commonLocations.chunked(3).forEach { rowLocations ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowLocations.forEach { loc ->
                                FilterChip(
                                    selected = searchText.equals(loc, ignoreCase = true),
                                    onClick = { searchText = loc },
                                    label = { Text(loc) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill remaining space if row has less than 3 items
                            repeat(3 - rowLocations.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (searchText.isNotBlank()) {
                        onLocationSelected(searchText.trim())
                    }
                },
                enabled = searchText.isNotBlank()
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CenteredDateTimeDisplay(label: String, value: String, onChangeClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface,
            contentColor = colors.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$label $value",
                color = colors.onSurface,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onChangeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.secondary,
                    contentColor = colors.onSecondary
                ),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .width(130.dp)
                    .height(38.dp)
            ) {
                Text("Change", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun StyledActionButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(label, fontWeight = FontWeight.Medium)
    }
}

/**
 * Helper function to pick location - opens Maps with search functionality
 */
fun pickLocationFromMap(context: android.content.Context, onLocationSelected: (String) -> Unit) {
    try {
        // Try multiple approaches to open a map/location picker
        
        // Approach 1: Try Google Maps with search intent
        try {
            val mapsIntent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=")
                setPackage("com.google.android.apps.maps")
            }
            if (mapsIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapsIntent)
                Toast.makeText(
                    context,
                    "Search for a location in Maps, then copy the name to the location field",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        } catch (e: Exception) {
            // Continue to next approach
        }
        
        // Approach 2: Try generic maps search
        try {
            val geoIntent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("geo:0,0?q=")
            }
            if (geoIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(geoIntent)
                Toast.makeText(
                    context,
                    "Search for a location, then enter it in the location field",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        } catch (e: Exception) {
            // Continue to next approach
        }
        
        // Approach 3: Try opening browser with Google Maps
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("https://www.google.com/maps")
            }
            if (browserIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(browserIntent)
                Toast.makeText(
                    context,
                    "Search for a location in Google Maps, then enter it in the location field",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        } catch (e: Exception) {
            // Fall through
        }
        
        // If all else fails, show message
        Toast.makeText(
            context,
            "No map app found. Please enter location manually in the text field.",
            Toast.LENGTH_LONG
        ).show()
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Could not open maps. Please enter location manually.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
