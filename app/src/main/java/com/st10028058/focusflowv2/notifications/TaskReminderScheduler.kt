package com.st10028058.focusflowv2.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.work.*
import com.st10028058.focusflowv2.data.Task
import java.util.concurrent.TimeUnit

object TaskReminderScheduler {

    fun schedule(context: Context, task: Task) {
        val start = task.startTime ?: return
        val offset = task.reminderOffsetMinutes ?: 10
        val reminderTime = start - offset * 60 * 1000
        val now = System.currentTimeMillis()
        val delay = reminderTime - now

        if (delay <= 0) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // ✅ Check runtime permission for exact alarms on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:" + context.packageName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            }
        }

        // 🔔 Schedule exact alarm
        val alarmIntent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("task_title", task.title)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task._id.hashCode(),
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)

        // 🧩 Fallback with WorkManager
        val data = workDataOf("task_title" to task.title)
        val work = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(task._id ?: "fallback")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "task_${task._id}", ExistingWorkPolicy.REPLACE, work
        )
    }

    fun cancel(context: Context, task: Task) {
        val alarmIntent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task._id.hashCode(),
            alarmIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        pendingIntent?.let { alarmManager.cancel(it) }

        WorkManager.getInstance(context).cancelUniqueWork("task_${task._id}")
    }
}
