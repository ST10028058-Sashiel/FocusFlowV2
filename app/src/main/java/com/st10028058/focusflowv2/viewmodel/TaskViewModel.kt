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

    fun fetchTasks() {
        viewModelScope.launch {
            val res = repository.getTasks()
            if (res.isSuccessful) _tasks.value = res.body().orEmpty()
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.addTask(task)
            fetchTasks()
        }
    }

    suspend fun addTaskAndReturn(task: Task): Task? {
        val res = repository.addTask(task)
        if (res.isSuccessful) {
            fetchTasks()
            return res.body()
        }
        return null
    }

    fun updateTask(id: String, task: Task) {
        viewModelScope.launch {
            val res = repository.updateTask(id, task)
            if (res.isSuccessful) fetchTasks()
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            val res = repository.deleteTask(id)
            if (res.isSuccessful) fetchTasks()
        }
    }
}
