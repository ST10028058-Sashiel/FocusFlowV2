package com.st10028058.focusflowv2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.st10028058.focusflowv2.data.Task
import com.st10028058.focusflowv2.data.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository(application)
    
    // Get current user ID - always get fresh value
    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
    
    // Track userId changes reactively
    private val userIdFlow = MutableStateFlow(getCurrentUserId())
    
    // Update userId when it changes
    init {
        // Check for userId changes periodically (when user logs in/out)
        viewModelScope.launch {
            while (true) {
                val currentUserId = getCurrentUserId()
                if (userIdFlow.value != currentUserId) {
                    userIdFlow.value = currentUserId
                }
                kotlinx.coroutines.delay(1000) // Check every second
            }
        }
    }

    // Use Flow from repository for reactive updates - reacts to userId changes
    val tasks: StateFlow<List<Task>> = userIdFlow
        .flatMapLatest { userId ->
            if (userId.isNotBlank()) {
                repository.getTasks(userId)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 🔄 Fetch all tasks from server and sync
    fun fetchTasks() {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            if (userId.isBlank()) {
                return@launch // No user logged in
            }
            // First sync pending tasks
            repository.syncPendingTasks()
            // Then fetch latest from server
            repository.fetchTasksFromServer(userId)
        }
    }

    // ➕ Add new task
    fun addTask(task: Task) {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            if (userId.isBlank()) {
                return@launch // No user logged in
            }
            // Ensure task has current user ID
            val taskWithUserId = task.copy(userId = userId)
            repository.addTask(taskWithUserId)
            // Sync in background
            repository.syncPendingTasks()
        }
    }

    // ➕ Add task and return created instance
    suspend fun addTaskAndReturn(task: Task): Task? {
        val userId = getCurrentUserId()
        if (userId.isBlank()) {
            return null // No user logged in
        }
        // Ensure task has current user ID
        val taskWithUserId = task.copy(userId = userId)
        val res = repository.addTask(taskWithUserId)
        // Sync in background
        viewModelScope.launch {
            repository.syncPendingTasks()
        }
        return res.body()
    }

    // ✏️ Update task
    fun updateTask(id: String, task: Task) {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            if (userId.isBlank()) {
                return@launch // No user logged in
            }
            // Ensure task has current user ID
            val taskWithUserId = task.copy(userId = userId)
            repository.updateTask(id, taskWithUserId)
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
            val userId = getCurrentUserId()
            if (userId.isBlank()) {
                return@launch // No user logged in
            }
            repository.syncPendingTasks()
            repository.fetchTasksFromServer(userId)
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
