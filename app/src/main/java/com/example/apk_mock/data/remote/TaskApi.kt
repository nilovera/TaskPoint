package com.example.apk_mock.data.remote

import com.example.apk_mock.data.remote.dto.TaskDto
import com.example.apk_mock.data.remote.dto.TaskRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TaskApi {
    @GET("tasks")
    suspend fun getTasks(@Header("Authorization") authorization: String): List<TaskDto>

    @POST("tasks")
    suspend fun createTask(
        @Header("Authorization") authorization: String,
        @Body body: TaskRequestDto
    ): TaskDto

    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Body body: TaskRequestDto
    ): TaskDto

    @DELETE("tasks/{id}")
    suspend fun deleteTask(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    )
}
