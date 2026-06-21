package com.example.apk_mock.data.remote

import com.example.apk_mock.data.remote.dto.DeleteRequestDto
import com.example.apk_mock.data.remote.dto.TaskDto
import com.example.apk_mock.data.remote.dto.TaskRequestDto
import com.example.apk_mock.data.remote.dto.TaskSyncDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TaskApi {
    @GET("tasks")
    suspend fun getTasks(@Header("Authorization") authorization: String): List<TaskDto>

    @GET("tasks/sync")
    suspend fun getTaskSyncRecords(@Header("Authorization") authorization: String): List<TaskSyncDto>

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

    @HTTP(method = "DELETE", path = "tasks/{id}", hasBody = true)
    suspend fun deleteTask(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Body body: DeleteRequestDto
    )
}
