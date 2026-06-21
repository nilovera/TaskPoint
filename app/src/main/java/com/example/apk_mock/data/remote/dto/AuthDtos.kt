package com.example.apk_mock.data.remote.dto

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String
)

data class AuthResponseDto(
    val token: String,
    val user: UserDto
)

data class SendResetCodeRequestDto(
    val email: String
)

data class VerifyResetCodeRequestDto(
    val email: String,
    val code: String
)

data class ChangePasswordRequestDto(
    val email: String,
    val newPassword: String
)

data class ChangeCurrentPasswordRequestDto(
    val currentPassword: String,
    val newPassword: String
)

data class ApiMessageDto(
    val message: String? = null
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String
)
