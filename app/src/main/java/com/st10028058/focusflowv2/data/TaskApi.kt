package com.st10028058.focusflowv2.data

import retrofit2.Response
import retrofit2.http.*

interface TaskApi {
    @GET("/tasks")
    suspend fun getTasks(): Response<List<Task>>

    @POST("/tasks")
    suspend fun addTask(@Body task: Task): Response<Task>

    @PUT("/tasks/{id}")
    suspend fun updateTask(@Path("id") id: String, @Body task: Task): Response<Task>

    @DELETE("/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): Response<Unit>
}
