package com.st10028058.focusflowv2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st10028058.focusflowv2.data.Task
import com.st10028058.focusflowv2.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {
    private val repository = TaskRepository()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> get() = _tasks

    // 🔄 Fetch all tasks
    fun fetchTasks() {
        viewModelScope.launch {
            val res = repository.getTasks()
            if (res.isSuccessful) _tasks.value = res.body().orEmpty()
        }
    }

    // ➕ Add new task
    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.addTask(task)
            fetchTasks()
        }
    }

    // ➕ Add task and return created instance
    suspend fun addTaskAndReturn(task: Task): Task? {
        val res = repository.addTask(task)
        if (res.isSuccessful) {
            fetchTasks()
            return res.body()
        }
        return null
    }

    // ✏️ Update task
    fun updateTask(id: String, task: Task) {
        viewModelScope.launch {
            val res = repository.updateTask(id, task)
            if (res.isSuccessful) fetchTasks()
        }
    }

    // ❌ Delete task
    fun deleteTask(id: String) {
        viewModelScope.launch {
            val res = repository.deleteTask(id)
            if (res.isSuccessful) fetchTasks()
        }
    }

    // ✅ Toggle completion (for TaskStatusScreen)
    fun toggleCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(completed = !(task.completed ?: false))
            if (task._id != null) {
                val res = repository.updateTask(task._id, updatedTask)
                if (res.isSuccessful) {
                    fetchTasks() // Refresh list
                }
            }
        }
    }
}
