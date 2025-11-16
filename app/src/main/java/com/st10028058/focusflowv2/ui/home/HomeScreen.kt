package com.st10028058.focusflowv2.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.st10028058.focusflowv2.R
import com.st10028058.focusflowv2.viewmodel.TaskViewModel

@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val viewModel: TaskViewModel = viewModel()
    val tasks by viewModel.tasks.collectAsState()
    
    // Calculate real statistics
    val totalTasks = tasks.size
    val completedTasks = tasks.count { task -> task.completed == true }
    val pendingTasks = totalTasks - completedTasks
    val completionRate = if (totalTasks > 0) (completedTasks * 100 / totalTasks) else 0
    
    // Fetch tasks on load
    LaunchedEffect(Unit) {
        viewModel.fetchTasks()
    }

    val hero = if (colors.surface.luminance() < 0.5f) {
        Brush.verticalGradient(listOf(colors.primary.copy(alpha = 0.98f), colors.primary.copy(alpha = 0.75f)))
    } else {
        Brush.verticalGradient(listOf(colors.primary, colors.primary.copy(alpha = 0.80f)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(hero)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = context.getString(R.string.home_title),
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = context.getString(R.string.home_subtitle),
                color = Color.White.copy(alpha = 0.90f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            GlanceRow(
                totalTasks = totalTasks,
                completedTasks = completedTasks,
                pendingTasks = pendingTasks,
                completionRate = completionRate
            )

            Spacer(Modifier.height(16.dp))
            
            // Progress Card
            if (totalTasks > 0) {
                ProgressCard(completedTasks, totalTasks, completionRate)
                Spacer(Modifier.height(14.dp))
            }
            
            AboutCard()
            Spacer(Modifier.height(14.dp))
            MotivationCard()
            Spacer(Modifier.height(10.dp))
            TipsCard()

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = context.getString(R.string.made_with_love),
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/* ---------- Sections ---------- */

@Composable
private fun GlanceRow(
    totalTasks: Int,
    completedTasks: Int,
    pendingTasks: Int,
    completionRate: Int
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatPill(
            label = context.getString(R.string.total_tasks),
            value = "$totalTasks",
            icon = Icons.Default.Schedule,
            container = colors.surface.copy(alpha = 0.9f),
            content = colors.onSurface,
            modifier = Modifier.weight(1f)
        )
        StatPill(
            label = context.getString(R.string.pending_tasks),
            value = "$pendingTasks",
            icon = Icons.Default.Lightbulb,
            container = colors.surface.copy(alpha = 0.9f),
            content = colors.onSurface,
            modifier = Modifier.weight(1f)
        )
        StatPill(
            label = context.getString(R.string.completed_tasks),
            value = "$completedTasks ($completionRate%)",
            icon = Icons.Default.CheckCircle,
            container = colors.surface.copy(alpha = 0.9f),
            content = colors.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier // ✅ accept modifier from caller
) {
    Surface(
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 4.dp,
        shadowElevation = 2.dp,
        modifier = modifier // ✅ use the RowScope.weight passed in
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Surface(color = content.copy(alpha = 0.10f), shape = CircleShape) {
                Icon(icon, contentDescription = null, tint = content, modifier = Modifier.padding(6.dp))
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = content.copy(alpha = 0.8f))
                Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            }
        }
    }
}

@Composable
private fun AboutCard() {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = colors.primary.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = colors.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    context.getString(R.string.about_focusflow),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSurface
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = context.getString(R.string.about_description),
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MotivationCard() {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(24.dp), spotColor = colors.primary.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                context.getString(R.string.motivation),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = context.getString(R.string.motivation_quote),
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TipsCard() {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(24.dp), spotColor = colors.primary.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                context.getString(R.string.pro_tips),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.onSurface
            )
            Spacer(Modifier.height(8.dp))
            TipRow(context.getString(R.string.tip_reminders))
            TipRow(context.getString(R.string.tip_priority))
            TipRow(context.getString(R.string.tip_review))
        }
    }
}

@Composable
private fun TipRow(text: String) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Surface(color = colors.primary.copy(alpha = 0.12f), shape = CircleShape) {
            Icon(Icons.Default.Lightbulb, null, tint = colors.primary, modifier = Modifier.padding(6.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(text, color = colors.onSurfaceVariant)
    }
}

@Composable
private fun ProgressCard(completed: Int, total: Int, percentage: Int) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = colors.primary.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    context.getString(R.string.progress_overview),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSurface
                )
                Text(
                    "$percentage%",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary
                )
            }
            Spacer(Modifier.height(12.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = colors.primary,
                trackColor = colors.surfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                context.getString(R.string.tasks_completed, completed, total),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}
