package com.st10028058.focusflowv2.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.st10028058.focusflowv2.MainActivity
import com.st10028058.focusflowv2.R

object TaskNotificationManager {
    
    private const val CHANNEL_ID_TASK_ADDED = "task_added_channel"
    private const val CHANNEL_ID_TASK_REMINDERS = "task_reminders_channel"
    private const val CHANNEL_NAME_TASK_ADDED = "Task Added Notifications"
    private const val CHANNEL_NAME_TASK_REMINDERS = "Task Reminders"
    
    /**
     * Initialize notification channels
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Channel for task added notifications
            val taskAddedChannel = NotificationChannel(
                CHANNEL_ID_TASK_ADDED,
                CHANNEL_NAME_TASK_ADDED,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when tasks are added"
                enableVibration(true)
                enableLights(true)
            }
            
            // Channel for task reminders
            val taskRemindersChannel = NotificationChannel(
                CHANNEL_ID_TASK_REMINDERS,
                CHANNEL_NAME_TASK_REMINDERS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Task reminder notifications"
                enableVibration(true)
                enableLights(true)
            }
            
            notificationManager.createNotificationChannel(taskAddedChannel)
            notificationManager.createNotificationChannel(taskRemindersChannel)
        }
    }
    
    /**
     * Show notification when a task is successfully added
     */
    fun showTaskAddedNotification(context: Context, taskTitle: String, taskPriority: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create intent to open app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TASK_ADDED)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("✅ Task Added Successfully!")
            .setContentText("$taskTitle (Priority: $taskPriority)")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your task \"$taskTitle\" has been added successfully.\nPriority: $taskPriority")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        // Show notification with unique ID
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
    
    /**
     * Show task reminder notification
     */
    fun showTaskReminderNotification(context: Context, taskTitle: String, taskLocation: String? = null) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationText = if (taskLocation != null) {
            "$taskTitle\n📍 Location: $taskLocation"
        } else {
            taskTitle
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TASK_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🔔 Task Reminder")
            .setContentText(taskTitle)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(notificationText)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

