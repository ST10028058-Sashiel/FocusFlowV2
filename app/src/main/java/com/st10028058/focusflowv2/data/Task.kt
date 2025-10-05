package com.st10028058.focusflowv2.data

data class Task(
    val _id: String? = null,              // MongoDB ID
    val title: String,
    val priority: String,                 // "Low" | "Normal" | "High"
    val completed: Boolean = false,

    // Calendar-style fields
    val allDay: Boolean = false,
    val startTime: Long? = null,          // epoch millis (nullable for allDay or unset)
    val endTime: Long? = null,            // epoch millis
    val location: String? = null,
    val reminderOffsetMinutes: Int? = 10, // e.g. 10 = "10 mins before" (nullable = none)

    // 🔐 Firebase UID (server fills this from token; keep nullable on client)
    val userId: String? = null
)
