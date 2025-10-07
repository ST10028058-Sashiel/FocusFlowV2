package com.st10028058.focusflowv2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ModeEdit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import com.st10028058.focusflowv2.data.Task
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskCard(
    task: Task,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onToggleCompleted: (() -> Unit)? = null // optional: if provided, a toggle icon appears
) {
    val colors = MaterialTheme.colorScheme
    val titleColor = if (task.completed) colors.onSurface.copy(alpha = 0.75f) else colors.onSurface

    // subtle tonal gradient so the card pops on both themes
    val subtle = if (colors.surface.luminance() < 0.5f) {
        listOf(colors.surface, colors.surfaceVariant.copy(alpha = 0.20f))
    } else {
        listOf(colors.surface, colors.surfaceVariant.copy(alpha = 0.12f))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(subtle))
                .padding(16.dp)
        ) {
            Column(Modifier.fillMaxWidth()) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Optional complete toggle
                    if (onToggleCompleted != null) {
                        IconButton(onClick = { onToggleCompleted() }) {
                            Icon(
                                imageVector = if (task.completed) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = if (task.completed) "Mark as pending" else "Mark as completed",
                                tint = if (task.completed) colors.primary else colors.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                    }

                    Text(
                        text = task.title,
                        color = titleColor,
                        style = MaterialTheme.typography.titleMedium.merge(
                            TextStyle(textDecoration = if (task.completed) TextDecoration.LineThrough else null)
                        ),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )

                    PriorityChip(priority = task.priority)
                }

                Spacer(Modifier.height(6.dp))

                InfoRow(Icons.Default.Event, dateRangeLabel(task.startTime, task.endTime))
                task.reminderOffsetMinutes?.let {
                    InfoRow(Icons.Default.AccessTime, "Reminder: $it mins before")
                }
                task.location?.takeIf { it.isNotBlank() }?.let {
                    InfoRow(Icons.Default.LocationOn, "Location: $it")
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatusChip(completed = task.completed)

                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.ModeEdit, contentDescription = "Edit", tint = colors.primary)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = colors.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text = text, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun PriorityChip(priority: String?) {
    val colors = MaterialTheme.colorScheme
    val (bg, fg, label) = when (priority?.lowercase()) {
        "high" -> Triple(colors.error.copy(alpha = 0.14f), colors.error, "High")
        "low"  -> Triple(colors.tertiary.copy(alpha = 0.16f), colors.tertiary, "Low")
        else   -> Triple(colors.primary.copy(alpha = 0.12f), colors.primary, "Normal")
    }

    AssistChip(
        onClick = { },
        label = { Text("Priority: $label", color = fg) },
        colors = AssistChipDefaults.assistChipColors(containerColor = bg, labelColor = fg),
        // Compose M3 on your version expects BorderStroke for 'border'
        border = BorderStroke(1.dp, bg)
    )
}

@Composable
private fun StatusChip(completed: Boolean) {
    val colors = MaterialTheme.colorScheme
    val bg = if (completed) colors.primary.copy(alpha = 0.12f) else colors.secondary.copy(alpha = 0.12f)
    val fg = if (completed) colors.primary else colors.secondary
    val text = if (completed) "Completed" else "Pending"
    SuggestionChip(
        onClick = { },
        label = { Text(text, color = fg, fontWeight = FontWeight.Medium) },
        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = bg)
    )
}

private fun dateRangeLabel(start: Long?, end: Long?): String {
    if (start == null) return "No date"
    val df = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault())
    val startS = df.format(Date(start))
    val endS = end?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it)) }
    return if (endS != null) "$startS → $endS" else startS
}
