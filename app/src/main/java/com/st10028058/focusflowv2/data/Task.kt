package com.st10028058.focusflowv2.data

data class Task(
    val _id: String? = null,
    val title: String,
    val priority: String,
    val completed: Boolean = false,
    val allDay: Boolean = false,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val location: String? = null,
    val reminderOffsetMinutes: Int? = 10,
    val userId: String? = null,

    val fcmToken: String? = null  // <-- new field
)
