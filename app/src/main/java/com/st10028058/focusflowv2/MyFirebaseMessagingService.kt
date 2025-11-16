package com.st10028058.focusflowv2

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.st10028058.focusflowv2.notifications.TaskNotificationManager

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "Reminder"
        val body = remoteMessage.notification?.body ?: ""

        // Use the centralized notification manager
        // For FCM messages, we'll show as a reminder notification
        TaskNotificationManager.showTaskReminderNotification(
            context = this,
            taskTitle = title,
            taskLocation = body
        )
    }
}
