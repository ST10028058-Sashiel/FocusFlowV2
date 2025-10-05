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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.st10028058.focusflowv2.data.Task
import com.st10028058.focusflowv2.ui.nav.Routes
import com.st10028058.focusflowv2.viewmodel.TaskViewModel
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
                    onClick = {
                        if (title.isBlank() || selectedDate == null) {
                            Toast.makeText(context, "Please set title and date", Toast.LENGTH_SHORT).show()
                            return@FloatingActionButton
                        }

                        val finalStart = if (allDay) selectedDate else startTime ?: selectedDate
                        val finalEnd = if (allDay) selectedDate else endTime ?: finalStart
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                        val newTask = Task(
                            title = title,
                            priority = priority,
                            allDay = allDay,
                            startTime = finalStart,
                            endTime = finalEnd,
                            location = if (location.isBlank()) null else location,
                            reminderOffsetMinutes = reminderOffset,
                            userId = uid
                        )

                        viewModel.addTask(newTask)
                        Toast.makeText(context, "Task added successfully", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    },
                    containerColor = Color(0xFFE91E63),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .padding(8.dp)
                        .size(60.dp)
                        .shadow(10.dp, RoundedCornerShape(20.dp))
                ) {
                    Icon(Icons.Filled.Save, contentDescription = "Save", tint = Color.White)
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
                // 🔙 Top Row with Back Arrow + Title
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
                    color = Color(0xFFE0C3FC),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // 🧾 Input Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        // 📝 Title Input
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Task Title") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )

                        // 🔽 Priority Dropdown
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                            OutlinedTextField(
                                value = priority,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Priority") },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
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

                        // ⏰ All-day Checkbox
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = allDay, onCheckedChange = { allDay = it })
                            Text("All Day Task", fontSize = 14.sp)
                        }

                        Divider(color = Color(0xFFEEE7FF))

                        // 📅 Date Section
                        if (selectedDate == null) {
                            StyledActionButton("Select Date", Color(0xFF6A0DAD)) {
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

                        // ⏱ Start & End Time
                        if (!allDay) {
                            if (startTime == null) {
                                StyledActionButton("Select Start Time", Color(0xFF8B2BE2)) {
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
                                StyledActionButton("Select End Time", Color(0xFF8B2BE2)) {
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

                        Divider(color = Color(0xFFEEE7FF))

                        // 📍 Location Input
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

// 🎨 Centered display for date/time
@Composable
fun CenteredDateTimeDisplay(label: String, value: String, onChangeClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F3FF)),
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
                color = Color(0xFF3A3A3A),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onChangeClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .width(130.dp)
                    .height(38.dp)
            ) {
                Text("Change", color = Color.White, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// 🎨 Consistent button styling
@Composable
fun StyledActionButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.Medium)
    }
}
