package com.st10028058.focusflowv2.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.st10028058.focusflowv2.R

@Composable
fun HomeScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF6A0DAD), Color(0xFF8B2BE2))
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 👋 Greeting
            Text(
                text = "Hi there 👋",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 30.dp)
            )

            Text(
                text = "Here’s your day at a glance!",
                color = Color(0xFFE0C3FC),
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // 🏅 Badge Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "Badge Earned This Week",
                        color = Color(0xFF6A0DAD),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Image(
                        painter = painterResource(id = R.drawable.focusflow_logo),
                        contentDescription = "Badge",
                        modifier = Modifier.size(60.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = 0.6f,
                        color = Color(0xFF6A0DAD),
                        trackColor = Color(0xFFE0C3FC),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        "300 points earned so far!",
                        color = Color.DarkGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { /* Navigate to next task */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Next Task", color = Color.White)
                    }
                }
            }

            // 📅 Task Summary Section
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "Tasks Today",
                        color = Color(0xFF6A0DAD),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TaskIndicator("Urgent", 5, Color.Red)
                    TaskIndicator("Needs to be done", 2, Color(0xFFFFC107))
                    TaskIndicator("Can be rescheduled", 1, Color(0xFF4CAF50))

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { /* Navigate to tasks */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A0DAD)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("View Tasks", color = Color.White)
                    }
                }
            }

            // 💪 Motivation Section
            Spacer(modifier = Modifier.height(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.progress_icon),
                    contentDescription = "Motivation Icon",
                    modifier = Modifier.size(100.dp)
                )
                Text(
                    "Keep going and stay focused!",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }
        }
    }
}

// ✅ Reusable Task Indicator component
@Composable
fun TaskIndicator(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, shape = RoundedCornerShape(50))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("$label - $count tasks", color = Color.DarkGray, fontSize = 14.sp)
    }
}
