package com.example.apk_mock.data.remote

import com.example.apk_mock.data.remote.dto.DeleteRequestDto
import com.example.apk_mock.data.remote.dto.RoutineDto
import com.example.apk_mock.data.remote.dto.RoutineRequestDto
import com.example.apk_mock.data.remote.dto.RoutineSyncDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RoutineApi {
    @GET("routines")
    suspend fun getRoutines(@Header("Authorization") authorization: String): List<RoutineDto>

    @GET("routines/sync")
    suspend fun getRoutineSyncRecords(@Header("Authorization") authorization: String): List<RoutineSyncDto>

    @POST("routines")
    suspend fun createRoutine(
        @Header("Authorization") authorization: String,
        @Body body: RoutineRequestDto
    ): RoutineDto

    @PUT("routines/{id}")
    suspend fun updateRoutine(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Body body: RoutineRequestDto
    ): RoutineDto

    @HTTP(method = "DELETE", path = "routines/{id}", hasBody = true)
    suspend fun deleteRoutine(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Body body: DeleteRequestDto
    )
}
