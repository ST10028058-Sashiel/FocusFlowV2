package com.st10028058.focusflowv2.data

import android.content.Context
import com.st10028058.focusflowv2.data.local.AppDatabase
import com.st10028058.focusflowv2.data.local.TaskEntity
import com.st10028058.focusflowv2.data.local.toEntity
import com.st10028058.focusflowv2.data.local.toTask
import com.st10028058.focusflowv2.utils.NetworkUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepository(private val context: Context) {
    private val api = RetrofitInstance.api
    private val database = AppDatabase.getDatabase(context)
    private val taskDao = database.taskDao()
    
    /**
     * Get all tasks - returns local data immediately, syncs in background if online
     */
    fun getTasks(userId: String): Flow<List<Task>> {
        // Return local data as Flow
        return taskDao.getAllTasks(userId).map { entities ->
            entities.map { it.toTask() }
        }
    }
    
    /**
     * Add task - saves locally first, syncs to server if online
     */
    suspend fun addTask(task: Task): retrofit2.Response<Task> {
        val isOnline = NetworkUtils.isNetworkAvailable(context)
        
        if (isOnline) {
            // Try to sync immediately
            try {
                val response = api.addTask(task)
                if (response.isSuccessful) {
                    // Save synced task to local DB
                    response.body()?.let { syncedTask ->
                        taskDao.insertTask(syncedTask.toEntity(isSynced = true))
                    }
                    return response
                }
            } catch (e: Exception) {
                // Network error - save offline
            }
        }
        
        // Save offline (needs sync)
        val taskEntity = task.toEntity(
            isSynced = false,
            needsSync = true,
            syncAction = "INSERT"
        )
        taskDao.insertTask(taskEntity)
        
        // Return a mock response for offline mode
        return retrofit2.Response.success(
            task.copy(_id = taskEntity.localId)
        )
    }
    
    /**
     * Update task - updates locally first, syncs to server if online
     */
    suspend fun updateTask(id: String, task: Task): retrofit2.Response<Task> {
        val isOnline = NetworkUtils.isNetworkAvailable(context)
        
        if (isOnline) {
            // Try to sync immediately
            try {
                val response = api.updateTask(id, task)
                if (response.isSuccessful) {
                    // Update local DB
                    response.body()?.let { updatedTask ->
                        taskDao.insertTask(updatedTask.toEntity(isSynced = true))
                    }
                    return response
                }
            } catch (e: Exception) {
                // Network error - save offline
            }
        }
        
        // Update offline (needs sync)
        val existingEntity = taskDao.getTaskById(id)
        if (existingEntity != null) {
            val updatedEntity = task.toEntity(
                isSynced = false,
                needsSync = true,
                syncAction = "UPDATE"
            ).copy(localId = existingEntity.localId)
            taskDao.updateTask(updatedEntity)
        } else {
            // If not found locally, insert as new
            taskDao.insertTask(task.toEntity(isSynced = false, needsSync = true, syncAction = "UPDATE"))
        }
        
        // Return a mock response for offline mode
        return retrofit2.Response.success(task)
    }
    
    /**
     * Delete task - deletes locally first, syncs to server if online
     */
    suspend fun deleteTask(id: String): retrofit2.Response<Unit> {
        val isOnline = NetworkUtils.isNetworkAvailable(context)
        
        // Get task before deleting
        val taskEntity = taskDao.getTaskById(id)
        
        if (isOnline && taskEntity?.isSynced == true) {
            // Try to sync deletion immediately
            try {
                val response = api.deleteTask(id)
                if (response.isSuccessful) {
                    // Delete from local DB
                    taskDao.deleteTaskById(id)
                    return response
                }
            } catch (e: Exception) {
                // Network error - mark for sync
                taskEntity?.let {
                    taskDao.updateTask(it.copy(needsSync = true, syncAction = "DELETE"))
                }
            }
        } else {
            // Mark for deletion sync
            taskEntity?.let {
                if (it.isSynced) {
                    // Task exists on server - mark for deletion
                    taskDao.updateTask(it.copy(needsSync = true, syncAction = "DELETE"))
                } else {
                    // Task only exists locally - delete immediately
                    taskDao.deleteTaskById(id)
                }
            }
        }
        
        // Return success for offline mode
        return retrofit2.Response.success(Unit)
    }
    
    /**
     * Sync pending tasks with server
     * Only syncs tasks for the current user
     */
    suspend fun syncPendingTasks() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            return
        }
        
        // Get current user ID
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId.isNullOrBlank()) {
            return // No user logged in
        }
        
        val tasksToSync = taskDao.getTasksNeedingSync()
            .filter { it.userId == currentUserId } // Only sync current user's tasks
        
        for (taskEntity in tasksToSync) {
            try {
                when (taskEntity.syncAction) {
                    "INSERT" -> {
                        val task = taskEntity.toTask()
                        // Ensure userId is set
                        val taskWithUserId = task.copy(userId = currentUserId)
                        val response = api.addTask(taskWithUserId)
                        if (response.isSuccessful) {
                            response.body()?.let { syncedTask ->
                                // Update with server ID, keep same localId, ensure userId
                                val updatedEntity = syncedTask.toEntity(isSynced = true)
                                    .copy(
                                        localId = taskEntity.localId, 
                                        _id = syncedTask._id,
                                        userId = currentUserId
                                    )
                                taskDao.insertTask(updatedEntity)
                            }
                        }
                    }
                    "UPDATE" -> {
                        val task = taskEntity.toTask()
                        // Ensure userId is set
                        val taskWithUserId = task.copy(userId = currentUserId)
                        task._id?.let { id ->
                            val response = api.updateTask(id, taskWithUserId)
                            if (response.isSuccessful) {
                                response.body()?.let { syncedTask ->
                                    val syncedEntity = syncedTask.toEntity(isSynced = true)
                                        .copy(userId = currentUserId)
                                    taskDao.insertTask(syncedEntity)
                                }
                            }
                        }
                    }
                    "DELETE" -> {
                        taskEntity._id?.let { id ->
                            val response = api.deleteTask(id)
                            if (response.isSuccessful) {
                                taskDao.deleteTaskById(id)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Continue with next task if one fails
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Fetch tasks from server and update local database
     * Only fetches tasks for the specified user
     */
    suspend fun fetchTasksFromServer(userId: String) {
        if (!NetworkUtils.isNetworkAvailable(context) || userId.isBlank()) {
            return
        }
        
        try {
            // Pass userId as query parameter to backend
            val response = api.getTasks(userId)
            if (response.isSuccessful) {
                response.body()?.let { tasks ->
                    // Backend should already filter by userId, but double-check client-side
                    val userTasks = tasks.filter { it.userId == userId }
                    // Ensure all tasks have userId set
                    val tasksWithUserId = userTasks.map { task ->
                        if (task.userId != userId) {
                            task.copy(userId = userId)
                        } else {
                            task
                        }
                    }
                    val entities = tasksWithUserId.map { it.toEntity(isSynced = true) }
                    if (entities.isNotEmpty()) {
                        taskDao.insertTasks(entities)
                    }
                }
            } else {
                // Log error response
                android.util.Log.e("TaskRepository", "Failed to fetch tasks: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("TaskRepository", "Error fetching tasks from server", e)
            e.printStackTrace()
        }
    }
}
