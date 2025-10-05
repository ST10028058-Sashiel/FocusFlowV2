// app/src/main/java/com/st10028058/focusflowv2/data/local/TaskDao.kt
package com.st10028058.focusflowv2.data.local

import androidx.room.*

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE userId = :uid ORDER BY startTime ASC, localId DESC")
    suspend fun getTasks(uid: String?): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE remoteId = :remoteId OR localId = :localId")
    suspend fun delete(remoteId: String?, localId: Long = -1L)

    @Query("DELETE FROM tasks WHERE userId = :uid")
    suspend fun clearForUser(uid: String?)
}
