package com.example.apk_mock.data.remote

import com.example.apk_mock.data.remote.dto.AuthResponseDto
import com.example.apk_mock.data.remote.dto.ApiMessageDto
import com.example.apk_mock.data.remote.dto.ChangeCurrentPasswordRequestDto
import com.example.apk_mock.data.remote.dto.ChangePasswordRequestDto
import com.example.apk_mock.data.remote.dto.LoginRequestDto
import com.example.apk_mock.data.remote.dto.RegisterRequestDto
import com.example.apk_mock.data.remote.dto.SendResetCodeRequestDto
import com.example.apk_mock.data.remote.dto.UserDto
import com.example.apk_mock.data.remote.dto.VerifyResetCodeRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestDto): AuthResponseDto

    @GET("auth/me")
    suspend fun me(@Header("Authorization") authorization: String): UserDto

    @POST("auth/password/reset-code")
    suspend fun sendResetCode(@Body body: SendResetCodeRequestDto): ApiMessageDto

    @POST("auth/password/verify-code")
    suspend fun verifyResetCode(@Body body: VerifyResetCodeRequestDto): ApiMessageDto

    @POST("auth/password/reset")
    suspend fun changePassword(@Body body: ChangePasswordRequestDto): ApiMessageDto

    @PUT("auth/me/password")
    suspend fun changeCurrentPassword(
        @Header("Authorization") authorization: String,
        @Body body: ChangeCurrentPasswordRequestDto
    ): ApiMessageDto

    @DELETE("auth/me")
    suspend fun deleteCurrentUser(@Header("Authorization") authorization: String): ApiMessageDto
}
