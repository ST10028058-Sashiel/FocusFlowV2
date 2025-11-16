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
     */
    suspend fun syncPendingTasks() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            return
        }
        
        val tasksToSync = taskDao.getTasksNeedingSync()
        
        for (taskEntity in tasksToSync) {
            try {
                when (taskEntity.syncAction) {
                    "INSERT" -> {
                        val task = taskEntity.toTask()
                        val response = api.addTask(task)
                        if (response.isSuccessful) {
                            response.body()?.let { syncedTask ->
                                // Update with server ID, keep same localId
                                val updatedEntity = syncedTask.toEntity(isSynced = true)
                                    .copy(localId = taskEntity.localId, _id = syncedTask._id)
                                taskDao.insertTask(updatedEntity)
                            }
                        }
                    }
                    "UPDATE" -> {
                        val task = taskEntity.toTask()
                        task._id?.let { id ->
                            val response = api.updateTask(id, task)
                            if (response.isSuccessful) {
                                response.body()?.let { syncedTask ->
                                    taskDao.insertTask(syncedTask.toEntity(isSynced = true))
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
     */
    suspend fun fetchTasksFromServer() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            return
        }
        
        try {
            val response = api.getTasks()
            if (response.isSuccessful) {
                response.body()?.let { tasks ->
                    // Convert to entities and save
                    val entities = tasks.map { it.toEntity(isSynced = true) }
                    taskDao.insertTasks(entities)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
