package com.st10028058.focusflowv2.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(navController: NavHostController) {
    val colors = MaterialTheme.colorScheme

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
                text = "FocusFlow",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Stay organized, focused, and in control.",
                color = Color.White.copy(alpha = 0.90f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            GlanceRow()

            Spacer(Modifier.height(16.dp))
            AboutCard()
            Spacer(Modifier.height(14.dp))
            MotivationCard()
            Spacer(Modifier.height(10.dp))
            TipsCard()

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Made with ♥ by FocusFlow",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/* ---------- Sections ---------- */

@Composable
private fun GlanceRow() {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ✅ Pass weight from the Row (RowScope) to each pill
        StatPill(
            label = "Upcoming",
            value = "Today",
            icon = Icons.Default.Schedule,
            container = colors.surface.copy(alpha = 0.9f),
            content = colors.onSurface,
            modifier = Modifier.weight(1f)
        )
        StatPill(
            label = "In Progress",
            value = "Keep it up",
            icon = Icons.Default.Lightbulb,
            container = colors.surface.copy(alpha = 0.9f),
            content = colors.onSurface,
            modifier = Modifier.weight(1f)
        )
        StatPill(
            label = "Completed",
            value = "Nice!",
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
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
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
    val colors = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = colors.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "About FocusFlow",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSurface
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "FocusFlow helps you plan your day, track progress, and keep momentum. Create tasks, set reminders, and see what needs your attention at a glance.",
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MotivationCard() {
    val colors = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Motivation",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "“Small progress each day adds up to big results.”",
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TipsCard() {
    val colors = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                "Pro Tips",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.onSurface
            )
            Spacer(Modifier.height(8.dp))
            TipRow("Use reminders for time-sensitive tasks.")
            TipRow("Group tasks by priority to focus better.")
            TipRow("Review your status in the Updates tab.")
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
