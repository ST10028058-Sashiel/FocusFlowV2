package com.st10028058.focusflowv2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.st10028058.focusflowv2.data.Task
import com.st10028058.focusflowv2.data.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository(application)
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Use Flow from repository for reactive updates
    val tasks: StateFlow<List<Task>> = repository.getTasks(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 🔄 Fetch all tasks from server and sync
    fun fetchTasks() {
        viewModelScope.launch {
            // First sync pending tasks
            repository.syncPendingTasks()
            // Then fetch latest from server
            repository.fetchTasksFromServer()
        }
    }

    // ➕ Add new task
    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.addTask(task)
            // Sync in background
            repository.syncPendingTasks()
        }
    }

    // ➕ Add task and return created instance
    suspend fun addTaskAndReturn(task: Task): Task? {
        val res = repository.addTask(task)
        // Sync in background
        viewModelScope.launch {
            repository.syncPendingTasks()
        }
        return res.body()
    }

    // ✏️ Update task
    fun updateTask(id: String, task: Task) {
        viewModelScope.launch {
            repository.updateTask(id, task)
            // Sync in background
            repository.syncPendingTasks()
        }
    }

    // ❌ Delete task
    fun deleteTask(id: String) {
        viewModelScope.launch {
            repository.deleteTask(id)
            // Sync in background
            repository.syncPendingTasks()
        }
    }
    
    // 🔄 Sync pending tasks manually
    fun syncTasks() {
        viewModelScope.launch {
            repository.syncPendingTasks()
            repository.fetchTasksFromServer()
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
