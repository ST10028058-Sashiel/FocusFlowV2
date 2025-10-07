package com.st10028058.focusflowv2.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.st10028058.focusflowv2.data.Task
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

    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Normal") }
    var allDay by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var startTime by remember { mutableStateOf<Long?>(null) }
    var endTime by remember { mutableStateOf<Long?>(null) }
    var reminderOffset by remember { mutableStateOf(10) }
    var location by remember { mutableStateOf("") }

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
                                if (savedTask != null && savedTask.startTime != null && savedTask.reminderOffsetMinutes != null) {
                                    TaskReminderScheduler.schedule(context, savedTask)
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
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .padding(8.dp)
                        .size(60.dp)
                        .shadow(10.dp, RoundedCornerShape(20.dp))
                ) {
                    Icon(Icons.Filled.Save, contentDescription = "Save")
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
                            )
                        }

                        if (!allDay) {
                            if (startTime == null) {
                                StyledActionButton("Select Start Time", colors.primary) {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                                            calendar.set(Calendar.MINUTE, minute)
                                            startTime = calendar.timeInMillis
                                        },
                                        calendar.get(Calendar.HOUR_OF_DAY),
                                        calendar.get(Calendar.MINUTE),
                                        true
                                    ).show()
                                }
                            } else {
                                CenteredDateTimeDisplay(
                                    label = "Start Time:",
                                    value = timeFormat.format(Date(startTime!!)),
                                    onChangeClick = {
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                calendar.set(Calendar.HOUR_OF_DAY, hour)
                                                calendar.set(Calendar.MINUTE, minute)
                                                startTime = calendar.timeInMillis
                                            },
                                            calendar.get(Calendar.HOUR_OF_DAY),
                                            calendar.get(Calendar.MINUTE),
                                            true
                                        ).show()
                                    }
                                )
                            }

                            if (endTime == null) {
                                StyledActionButton("Select End Time", colors.primary) {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                                            calendar.set(Calendar.MINUTE, minute)
                                            endTime = calendar.timeInMillis
                                        },
                                        calendar.get(Calendar.HOUR_OF_DAY),
                                        calendar.get(Calendar.MINUTE),
                                        true
                                    ).show()
                                }
                            } else {
                                CenteredDateTimeDisplay(
                                    label = "End Time:",
                                    value = timeFormat.format(Date(endTime!!)),
                                    onChangeClick = {
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                calendar.set(Calendar.HOUR_OF_DAY, hour)
                                                calendar.set(Calendar.MINUTE, minute)
                                                endTime = calendar.timeInMillis
                                            },
                                            calendar.get(Calendar.HOUR_OF_DAY),
                                            calendar.get(Calendar.MINUTE),
                                            true
                                        ).show()
                                    }
                                )
                            }
                        }

                        Divider(color = colors.surfaceVariant)

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            colors = fieldColors
                        )
                    }
                }

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
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
