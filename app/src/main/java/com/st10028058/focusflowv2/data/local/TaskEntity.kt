package com.st10028058.focusflowv2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.st10028058.focusflowv2.data.Task

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val localId: String, // Primary key - always non-null, used for offline tasks
    val _id: String? = null, // Server ID - nullable until synced
    val title: String,
    val priority: String,
    val completed: Boolean = false,
    val allDay: Boolean = false,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val location: String? = null,
    val reminderOffsetMinutes: Int? = 10,
    val userId: String? = null,
    val fcmToken: String? = null,
    // Sync fields
    val isSynced: Boolean = false,
    val needsSync: Boolean = false,
    val syncAction: String? = null // "INSERT", "UPDATE", "DELETE"
)

// Convert Task to TaskEntity
fun Task.toEntity(isSynced: Boolean = true, needsSync: Boolean = false, syncAction: String? = null): TaskEntity {
    return TaskEntity(
        localId = this._id ?: java.util.UUID.randomUUID().toString(),
        _id = this._id,
        title = this.title,
        priority = this.priority,
        completed = this.completed,
        allDay = this.allDay,
        startTime = this.startTime,
        endTime = this.endTime,
        location = this.location,
        reminderOffsetMinutes = this.reminderOffsetMinutes,
        userId = this.userId,
        fcmToken = this.fcmToken,
        isSynced = isSynced,
        needsSync = needsSync,
        syncAction = syncAction
    )
}

// Convert TaskEntity to Task
fun TaskEntity.toTask(): Task {
    return Task(
        _id = this._id,
        title = this.title,
        priority = this.priority,
        completed = this.completed,
        allDay = this.allDay,
        startTime = this.startTime,
        endTime = this.endTime,
        location = this.location,
        reminderOffsetMinutes = this.reminderOffsetMinutes,
        userId = this.userId,
        fcmToken = this.fcmToken
    )
}

