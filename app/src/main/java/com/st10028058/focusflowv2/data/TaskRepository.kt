package com.st10028058.focusflowv2.data

class TaskRepository {
    suspend fun getTasks() = RetrofitInstance.api.getTasks()
    suspend fun addTask(task: Task) = RetrofitInstance.api.addTask(task)
    suspend fun updateTask(id: String, task: Task) = RetrofitInstance.api.updateTask(id, task)
    suspend fun deleteTask(id: String) = RetrofitInstance.api.deleteTask(id)
}
