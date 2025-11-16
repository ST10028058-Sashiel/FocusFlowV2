package com.st10028058.focusflowv2.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("task_title") ?: "Task Reminder"
        val location = intent.getStringExtra("task_location")
        
        Toast.makeText(context, "Reminder for: $title", Toast.LENGTH_LONG).show()

        // Use the centralized notification manager
        TaskNotificationManager.showTaskReminderNotification(
            context = context,
            taskTitle = title,
            taskLocation = location
        )
    }
}
