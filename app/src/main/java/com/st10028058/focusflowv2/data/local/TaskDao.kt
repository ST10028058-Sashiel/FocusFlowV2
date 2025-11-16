package com.st10028058.focusflowv2.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY startTime DESC")
    fun getAllTasks(userId: String): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE _id = :id OR localId = :id")
    suspend fun getTaskById(id: String): TaskEntity?
    
    @Query("SELECT * FROM tasks WHERE needsSync = 1")
    suspend fun getTasksNeedingSync(): List<TaskEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)
    
    @Update
    suspend fun updateTask(task: TaskEntity)
    
    @Delete
    suspend fun deleteTask(task: TaskEntity)
    
    @Query("DELETE FROM tasks WHERE _id = :id OR localId = :id")
    suspend fun deleteTaskById(id: String)
    
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
    
    @Query("UPDATE tasks SET isSynced = 1, needsSync = 0, syncAction = NULL WHERE _id = :id OR localId = :id")
    suspend fun markAsSynced(id: String)
    
    @Query("SELECT * FROM tasks WHERE localId = :localId")
    suspend fun getTaskByLocalId(localId: String): TaskEntity?
}

