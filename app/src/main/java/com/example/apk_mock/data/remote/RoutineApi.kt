package com.example.apk_mock.data.remote

import com.example.apk_mock.data.remote.dto.RoutineDto
import com.example.apk_mock.data.remote.dto.RoutineRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RoutineApi {
    @GET("routines")
    suspend fun getRoutines(@Header("Authorization") authorization: String): List<RoutineDto>

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

    @DELETE("routines/{id}")
    suspend fun deleteRoutine(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    )
}
