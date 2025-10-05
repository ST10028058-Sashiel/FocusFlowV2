// app/src/main/java/com/st10028058/focusflowv2/data/local/TaskEntity.kt
package com.st10028058.focusflowv2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0L,
    val remoteId: String?,           // _id from MongoDB
    val title: String,
    val priority: String,
    val completed: Boolean,
    val allDay: Boolean,
    val startTime: Long?,
    val endTime: Long?,
    val location: String?,
    val reminderOffsetMinutes: Int?,
    val userId: String?              // Firebase UID owner
)
