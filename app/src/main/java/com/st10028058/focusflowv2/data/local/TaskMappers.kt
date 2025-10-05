// app/src/main/java/com/st10028058/focusflowv2/data/local/TaskMappers.kt
package com.st10028058.focusflowv2.data.local

import com.st10028058.focusflowv2.data.Task

fun Task.toEntity(): TaskEntity = TaskEntity(
    remoteId = _id,
    title = title,
    priority = priority,
    completed = completed,
    allDay = allDay,
    startTime = startTime,
    endTime = endTime,
    location = location,
    reminderOffsetMinutes = reminderOffsetMinutes,
    userId = userId
)

fun TaskEntity.toDomain(): Task = Task(
    _id = remoteId,
    title = title,
    priority = priority,
    completed = completed,
    allDay = allDay,
    startTime = startTime,
    endTime = endTime,
    location = location,
    reminderOffsetMinutes = reminderOffsetMinutes,
    userId = userId
)
