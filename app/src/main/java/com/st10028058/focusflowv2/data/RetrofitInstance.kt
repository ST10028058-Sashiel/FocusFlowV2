package com.st10028058.focusflowv2.data

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BASE_URL = "https://focusflow-api-ts06.onrender.com"

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()

        val requestBuilder = original.newBuilder()
        
        // Get token asynchronously and wait for it
        try {
            val user = FirebaseAuth.getInstance().currentUser
            val token = user?.let {
                val task = it.getIdToken(false)
                try {
                    // Wait for the task to complete (with timeout)
                    Tasks.await(task, 5, TimeUnit.SECONDS)
                    task.result?.token
                } catch (e: Exception) {
                    // If token retrieval fails, proceed without auth header
                    null
                }
            }
            
            token?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }
        } catch (e: Exception) {
            // If any error occurs, proceed without auth header
            e.printStackTrace()
        }

        val request = requestBuilder.build()
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val api: TaskApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TaskApi::class.java)
    }
}
